package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.IdentidadExterna;

/**
 * Puerto de salida hacia un proveedor de identidad externo (OpenID Connect).
 *
 * La capa de aplicacion depende de esta abstraccion, nunca de la libreria
 * concreta del proveedor ni de su protocolo de red.
 */
public interface ProveedorIdentidadPort {

    /**
     * Verifica la firma y las claims de un ID token emitido por el proveedor.
     *
     * @param idToken Token compacto (JWT) entregado por el proveedor.
     * @return La identidad acreditada por el token.
     * @throws RuntimeException Si el token es invalido, esta expirado, tiene
     *                          una firma no verificable o no fue emitido para
     *                          esta aplicacion.
     */
    IdentidadExterna verificarIdToken(String idToken);
}
