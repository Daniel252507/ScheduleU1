package com.scheduleu.domain.port.in;

import com.scheduleu.domain.model.Usuario;

public interface EnlaceContrasenaUseCase {
    void enviarEnlaceEstablecimiento(Usuario usuario);
}
