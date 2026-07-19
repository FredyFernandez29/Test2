package com.fsat.fsatdesk_api.config;

import com.fsat.fsatdesk_api.model.Activity;
import com.fsat.fsatdesk_api.model.Ticket;
import com.fsat.fsatdesk_api.model.User;
import com.fsat.fsatdesk_api.service.EmailService;
import com.fsat.fsatdesk_api.service.TicketService;
import com.fsat.fsatdesk_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;





@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserService userService;
    private final TicketService ticketService;
    private final EmailService emailService; 
    

    @Override
    public void run(String... args) throws Exception {
        // 1. Crear usuarios solo si no existen
        User admin = userService.findByEmail("Administrator").orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .name("Administrator")
                    .email("Administrator")
                    .rol("admin")
                    .dept("TI")
                    .activo(true)
                    .creado(LocalDate.now())
                    .passChanged(LocalDate.now())
                    .perms(Map.of())
                    .build();
            userService.createUser(admin, "Glotrans2022");
        }

        User pedro = userService.findByEmail("pedro@empresa.com").orElse(null);
        if (pedro == null) {
            pedro = User.builder()
                    .name("Pedro Sanchez")
                    .email("pedro@empresa.com")
                    .rol("tecnico")
                    .dept("Soporte TI")
                    .activo(true)
                    .creado(LocalDate.now())
                    .passChanged(LocalDate.now())
                    .perms(Map.of("del", true, "sts", true, "asg", true, "pri", false, "rep", true))
                    .build();
            userService.createUser(pedro, "Soporte@2026");
        }

        User maria = userService.findByEmail("maria@empresa.com").orElse(null);
        if (maria == null) {
            maria = User.builder()
                    .name("Maria Garcia")
                    .email("maria@empresa.com")
                    .rol("usuario")
                    .dept("Contabilidad")
                    .activo(true)
                    .creado(LocalDate.now())
                    .passChanged(LocalDate.now())
                    .perms(Map.of())
                    .build();
            userService.createUser(maria, "Conta@2026!");
        }

        User carlos = userService.findByEmail("carlos@empresa.com").orElse(null);
        if (carlos == null) {
            carlos = User.builder()
                    .name("Carlos Rodriguez")
                    .email("carlos@empresa.com")
                    .rol("usuario")
                    .dept("Recursos Humanos")
                    .activo(true)
                    .creado(LocalDate.now())
                    .passChanged(LocalDate.now())
                    .perms(Map.of())
                    .build();
            userService.createUser(carlos, "RRHH@2026!");
        }

        User ana = userService.findByEmail("ana@empresa.com").orElse(null);
        if (ana == null) {
            ana = User.builder()
                    .name("Ana Martinez")
                    .email("ana@empresa.com")
                    .rol("usuario")
                    .dept("Ventas")
                    .activo(true)
                    .creado(LocalDate.now())
                    .passChanged(LocalDate.now())
                    .perms(Map.of())
                    .build();
            userService.createUser(ana, "Ventas@2026");
        }

        // 2. Crear tickets de ejemplo (solo si no existen)
        if (ticketService.findByTicketId("TK-001").isEmpty()) {
            Ticket t1 = Ticket.builder()
                    .title("Computadora no enciende")
                    .category("Hardware")
                    .priority("Alta")
                    .status("Abierto")
                    .desc("Al presionar el boton de encendido no sucede nada. La luz del cargador si enciende pero la pantalla permanece apagada.")
                    .device("Dell Latitude 5420")
                    .location("Contabilidad piso 2")
                    .userId(maria.getId())
                    .user(maria.getName())
                    .email(maria.getEmail())
                    .created(LocalDate.of(2026, 4, 10))
                    .updated(LocalDate.of(2026, 4, 10))
                    .build();
            t1.getActivity().add(Activity.builder()
                    .by("Sistema")
                    .text("Ticket creado.")
                    .time("09:15")
                    .date(LocalDate.of(2026, 4, 10))
                    .build());
            ticketService.createTicket(t1, maria.getId());
        }

        // Puedes agregar más tickets según tu HTML original
        // ... (TK-002, TK-003, etc.)
    }

}