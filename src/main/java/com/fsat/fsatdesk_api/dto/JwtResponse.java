package com.fsat.fsatdesk_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String id;
    private String name;
    private String email;
    private String rol;
    private Map<String, Boolean> perms;
}