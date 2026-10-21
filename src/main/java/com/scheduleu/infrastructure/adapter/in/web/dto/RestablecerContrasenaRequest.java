package com.scheduleu.infrastructure.adapter.in.web.dto;

import lombok.Data;

@Data
public class RestablecerContrasenaRequest {
    private String token;
    private String contrasena;
}
