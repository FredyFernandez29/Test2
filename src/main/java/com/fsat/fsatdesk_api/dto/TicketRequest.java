package com.fsat.fsatdesk_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRequest {
    @NotBlank(message = "Título requerido")
    private String title;

    @NotBlank(message = "Categoría requerida")
    private String category;

    @NotBlank(message = "Prioridad requerida")
    private String priority;

    @NotBlank(message = "Descripción requerida")
    private String desc;

    @NotBlank(message = "Dispositivo requerido")
    private String device;

    @NotBlank(message = "Ubicación requerida")
    private String location;
}