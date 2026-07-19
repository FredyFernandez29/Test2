package com.fsat.fsatdesk_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String by;    // nombre de quien realizó la acción
    private String text;
    private String time;  // HH:mm
    private LocalDate date;
}