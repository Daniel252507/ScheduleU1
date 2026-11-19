package com.scheduleu.domain.port.out;

/**
 * Puerto de salida hacia un modelo de lenguaje (IA generativa).
 *
 * La implementacion concreta decide el proveedor (por ejemplo Google Gemini) y
 * el protocolo de comunicacion. La capa de aplicacion solo conoce este
 * contrato.
 */
public interface AsistenteIAPort {

    /**
     * Envia una consulta al modelo y devuelve el texto generado.
     *
     * @param instruccionSistema contexto y reglas que debe seguir el modelo.
     * @param consulta           texto de entrada (contexto + pregunta del usuario).
     * @return respuesta en texto plano del modelo.
     */
    String responder(String instruccionSistema, String consulta);
}
