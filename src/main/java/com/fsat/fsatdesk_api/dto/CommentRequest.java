package com.fsat.fsatdesk_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "El comentario no puede estar vacío")
    private String comment;
}