package com.fsat.fsatdesk_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ticketId;   // TK-001, TK-002, ...

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String priority;   // Alta, Media, Baja

    @Column(nullable = false)
    private String status;     // Abierto, En progreso, Cerrado

   @Column(name = "\"desc\"", length = 2000)
    private String desc;

    private String device;
    private String location;

    @Column(nullable = false)
    private String userId;

    @Column(name = "\"user\"", nullable = false)
    private String user;      // nombre del creador (denormalizado)

    @Column(nullable = false)
    private String email;      // email del creador

    private String tecnico;    // nombre del técnico asignado

    private LocalDate created;
    private LocalDate updated;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ticket_id")
    @OrderBy("date ASC, time ASC")
    @Builder.Default
    private List<Activity> activity = new ArrayList<>();
}