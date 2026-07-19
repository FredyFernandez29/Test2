package com.fsat.fsatdesk_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(unique = true, nullable = false, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;  // almacenado con BCrypt

    @Column(nullable = false)
    private String rol;       // "admin", "tecnico", "usuario"

    private String dept;      // departamento

    private boolean activo = true;

    private LocalDate creado;

    private LocalDate passChanged;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "permission_key")
    @Column(name = "permission_value")
    @Builder.Default
    private Map<String, Boolean> perms = new HashMap<>();
}