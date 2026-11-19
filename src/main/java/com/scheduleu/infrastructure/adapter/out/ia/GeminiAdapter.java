package com.scheduleu.infrastructure.adapter.out.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scheduleu.domain.port.out.AsistenteIAPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Locale;

/**
 * Adaptador del puerto {@link AsistenteIAPort} para Google Gemini
 * (Generative Language API).
 *
 * Resiliencia:
 *  - Errores transitorios (503, 429, 500): se reintenta con espera creciente.
 *  - Si el modelo no responde a tiempo, sigue saturado o ya no existe (404),
 *    se prueba un modelo de respaldo.
 *  - Se limita el "razonamiento" interno del modelo para responder mas rapido.
 */
@Component
public class GeminiAdapter implements AsistenteIAPort {

    private static final Logger log = LoggerFactory.getLogger(GeminiAdapter.class);

    private static final String URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    /** Esperas (ms) antes de cada reintento ante 503/429/500. */
    private static final long[] ESPERAS_MS = {1000, 3000};

    /** Tiempo maximo de espera por cada intento. */
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(25);

    private final ObjectMapper objectMapper;
    private final HttpClient clienteHttp;
    private final String apiKey;
    private final String modelo;
    private final String modeloRespaldo;
    private final String nivelPensamiento;

    public GeminiAdapter(ObjectMapper objectMapper,
                         @Value("${gemini.api-key:}") String apiKey,
                         @Value("${gemini.modelo:gemini-3.8-flash}") String modelo,
                         @Value("${gemini.modelo-respaldo:gemini-3.7-flash}") String modeloRespaldo,
                         @Value("${gemini.pensamiento:low}") String nivelPensamiento) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.modelo = modelo;
        this.modeloRespaldo = modeloRespaldo;
        this.nivelPensamiento = nivelPensamiento;
        this.clienteHttp = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String responder(String instruccionSistema, String consulta) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("El asistente no esta configurado (falta GEMINI_API_KEY)");
        }

        try {
            boolean hayRespaldo = modeloRespaldo != null
                    && !modeloRespaldo.isBlank()
                    && !modeloRespaldo.equals(modelo);

            // 1) Modelo principal
            String modeloUsado = modelo;
            HttpResponse<String> respuesta = null;
            try {
                respuesta = enviarAdaptando(modelo, instruccionSistema, consulta);
            } catch (HttpTimeoutException e) {
                if (e instanceof HttpConnectTimeoutException || !hayRespaldo) {
                    throw e;
                }
                log.warn("El modelo '{}' no respondio a tiempo. Probando respaldo '{}'", modelo, modeloRespaldo);
            }

            // 2) Modelo de respaldo si el principal se colgo, sigue saturado o ya no existe
            if ((respuesta == null
                    || esTransitorio(respuesta.statusCode())
                    || respuesta.statusCode() == 404) && hayRespaldo) {
                if (respuesta != null) {
                    log.warn("El modelo '{}' fallo ({}). Probando respaldo '{}'",
                            modelo, respuesta.statusCode(), modeloRespaldo);
                }
                modeloUsado = modeloRespaldo;
                respuesta = enviarAdaptando(modeloRespaldo, instruccionSistema, consulta);
            }

            int estado = respuesta.statusCode();

            if (estado == 400 || estado == 403) {
                throw new RuntimeException("Gemini rechazo la peticion: " + mensajeDeError(respuesta.body()));
            }
            if (estado == 404) {
                throw new RuntimeException("El modelo '" + modeloUsado + "' no esta disponible para esta API key");
            }
            if (esTransitorio(estado)) {
                throw new RuntimeException(
                        "El asistente de IA esta saturado en este momento. Intenta de nuevo en unos segundos");
            }
            if (estado < 200 || estado >= 300) {
                throw new RuntimeException("Gemini respondio " + estado);
            }

            return extraerTexto(respuesta.body());
        } catch (HttpConnectTimeoutException e) {
            log.error("No hubo conexion con Gemini (tiempo de conexion agotado)", e);
            throw new RuntimeException("No hay conexion con Gemini: revisa tu internet, proxy o firewall");
        } catch (HttpTimeoutException e) {
            log.error("Gemini tardo demasiado en responder", e);
            throw new RuntimeException("Gemini tardo demasiado en responder. Intenta de nuevo en unos segundos");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Se interrumpio la comunicacion con el asistente");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("No se pudo consultar al asistente de IA", e);
            throw new RuntimeException("No fue posible obtener respuesta del asistente");
        }
    }

    /**
     * Envia la consulta limitando el razonamiento del modelo. Si el modelo
     * rechaza esa configuracion (400), reintenta una vez sin ella.
     */
    private HttpResponse<String> enviarAdaptando(String modeloActual, String instruccionSistema, String consulta)
            throws Exception {
        HttpResponse<String> respuesta = enviarConReintentos(modeloActual,
                construirCuerpo(modeloActual, instruccionSistema, consulta, true));

        if (respuesta.statusCode() == 400 && mencionaPensamiento(respuesta.body())) {
            log.warn("El modelo '{}' rechazo la configuracion de razonamiento. Reintentando sin ella", modeloActual);
            respuesta = enviarConReintentos(modeloActual,
                    construirCuerpo(modeloActual, instruccionSistema, consulta, false));
        }
        return respuesta;
    }

    /** Arma el JSON de la peticion para el modelo indicado. */
    private String construirCuerpo(String modeloActual, String instruccionSistema, String consulta,
                                   boolean limitarPensamiento) throws Exception {
        ObjectNode cuerpo = objectMapper.createObjectNode();
        cuerpo.putObject("systemInstruction")
                .putArray("parts").addObject().put("text", instruccionSistema);
        cuerpo.putArray("contents")
                .addObject().putArray("parts").addObject().put("text", consulta);

        ObjectNode config = cuerpo.putObject("generationConfig");
        boolean esGemini3 = modeloActual.contains("gemini-3");

        // En los Gemini 3.x se deja la temperatura por defecto (es lo recomendado).
        if (!esGemini3) {
            config.put("temperature", 0.4);
        }

        if (limitarPensamiento) {
            if (modeloActual.contains("2.5")) {
                config.putObject("thinkingConfig").put("thinkingBudget", 0);
            } else if (esGemini3 && nivelPensamiento != null && !nivelPensamiento.isBlank()) {
                config.putObject("thinkingConfig").put("thinkingLevel", nivelPensamiento.trim());
            }
        }
        return objectMapper.writeValueAsString(cuerpo);
    }

    /**
     * Envia la peticion al modelo indicado. Ante 503/429/500 espera y reintenta.
     * Si el intento supera el tiempo maximo lanza HttpTimeoutException (no se
     * reintenta el mismo modelo). Devuelve la ultima respuesta obtenida.
     */
    private HttpResponse<String> enviarConReintentos(String modeloActual, String cuerpoJson)
            throws Exception {
        HttpResponse<String> respuesta = null;

        for (int intento = 0; intento <= ESPERAS_MS.length; intento++) {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(URL_BASE + modeloActual + ":generateContent"))
                    .timeout(TIEMPO_MAXIMO)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpoJson))
                    .build();

            respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString());

            if (!esTransitorio(respuesta.statusCode())) {
                return respuesta;
            }

            log.warn("Gemini ({}) respondio {} (intento {} de {})",
                    modeloActual, respuesta.statusCode(), intento + 1, ESPERAS_MS.length + 1);

            if (intento < ESPERAS_MS.length) {
                Thread.sleep(ESPERAS_MS[intento]);
            }
        }
        return respuesta;
    }

    /** Errores que suelen resolverse solos al reintentar. */
    private boolean esTransitorio(int estado) {
        return estado == 503 || estado == 429 || estado == 500;
    }

    private boolean mencionaPensamiento(String cuerpoRespuesta) {
        return cuerpoRespuesta != null && cuerpoRespuesta.toLowerCase(Locale.ROOT).contains("thinking");
    }

    /** Extrae el texto de la primera respuesta del modelo. */
    private String extraerTexto(String cuerpoRespuesta) {
        try {
            JsonNode raiz = objectMapper.readTree(cuerpoRespuesta);
            JsonNode candidatos = raiz.path("candidates");
            if (candidatos.isArray() && !candidatos.isEmpty()) {
                JsonNode partes = candidatos.get(0).path("content").path("parts");
                if (partes.isArray()) {
                    StringBuilder texto = new StringBuilder();
                    for (JsonNode parte : partes) {
                        // se omiten las partes de "pensamiento" si el modelo las devuelve
                        if (parte.path("thought").asBoolean(false)) continue;
                        texto.append(parte.path("text").asText(""));
                    }
                    if (!texto.toString().isBlank()) {
                        return texto.toString();
                    }
                }
            }
            throw new RuntimeException("El asistente no devolvio respuesta (" + mensajeDeError(cuerpoRespuesta) + ")");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No fue posible interpretar la respuesta del asistente");
        }
    }

    private String mensajeDeError(String cuerpoRespuesta) {
        if (cuerpoRespuesta == null || cuerpoRespuesta.isBlank()) {
            return "sin detalle";
        }
        try {
            String mensaje = objectMapper.readTree(cuerpoRespuesta).path("error").path("message").asText("");
            return mensaje.isBlank() ? "sin detalle" : mensaje;
        } catch (Exception e) {
            return "sin detalle";
        }
    }
}