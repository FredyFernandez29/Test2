package com.fsat.fsatdesk_api.service;

import com.fsat.fsatdesk_api.model.Activity;
import com.fsat.fsatdesk_api.model.Ticket;
import com.fsat.fsatdesk_api.model.User;
import com.fsat.fsatdesk_api.repository.TicketRepository;
import com.fsat.fsatdesk_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${mail.extra.recipient:fredyfernandezrd@gmail.com}")
    private String extraRecipient;

    private String generateTicketId() {
        // Obtener el número más alto de ticketId existente
        Optional<String> maxTicketId = ticketRepository.findMaxTicketId();
        int nextNum = 1;
        if (maxTicketId.isPresent()) {
            String id = maxTicketId.get();
            // Extraer el número de TK-XXX
            String numPart = id.substring(3);
            try {
                nextNum = Integer.parseInt(numPart) + 1;
            } catch (NumberFormatException e) {
                nextNum = 1;
            }
        }
        return "TK-" + String.format("%03d", nextNum);
    }

    @Transactional
    public Ticket createTicket(Ticket ticket, String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ticket.setTicketId(generateTicketId());
        ticket.setUserId(user.getId());
        ticket.setUser(user.getName());
        ticket.setEmail(user.getEmail());
        ticket.setStatus("Abierto");
        ticket.setCreated(LocalDate.now());
        ticket.setUpdated(LocalDate.now());

        Activity act = Activity.builder()
                .by("Sistema")
                .text("Ticket creado por " + user.getName())
                .time(LocalTime.now().toString().substring(0, 5))
                .date(LocalDate.now())
                .build();
        ticket.getActivity().add(act);

        Ticket savedTicket = ticketRepository.save(ticket);

        // --- LOG: Ticket creado ---
        log.info("Ticket creado exitosamente: {}", savedTicket.getTicketId());
        log.info("Iniciando envío de correos para ticket: {}", savedTicket.getTicketId());

        // --- ENVÍO DE CORREOS A TÉCNICOS Y DESTINATARIO EXTRA ---
        sendEmailNotifications(savedTicket, user);

        return savedTicket;
    }

    private void sendEmailNotifications(Ticket ticket, User creator) {
        log.info("Buscando técnicos activos para notificar...");
        List<User> tecnicos = userRepository.findByRolAndActivoTrue("tecnico");
        log.info("Técnicos encontrados: {}", tecnicos.size());

        String subject = "Nuevo ticket " + ticket.getTicketId() + ": " + ticket.getTitle();
        String body = buildEmailBody(ticket, creator);

        // Enviar a todos los técnicos activos
        for (User tech : tecnicos) {
            log.info("Enviando correo a técnico: {}", tech.getEmail());
            emailService.sendHtmlEmail(tech.getEmail(), subject, body);
        }

        // Enviar también al destinatario fijo (administrador o supervisor)
        if (extraRecipient != null && !extraRecipient.isEmpty()) {
            log.info("Enviando correo a destinatario extra: {}", extraRecipient);
            emailService.sendHtmlEmail(extraRecipient, subject, body);
        } else {
            log.warn("Destinatario extra no definido o vacío.");
        }
    }

    private String buildEmailBody(Ticket ticket, User creator) {
        return "<html><body>"
                + "<h2>Nuevo ticket de soporte</h2>"
                + "<p><strong>ID:</strong> " + escapeHtml(ticket.getTicketId()) + "</p>"
                + "<p><strong>Título:</strong> " + escapeHtml(ticket.getTitle()) + "</p>"
                + "<p><strong>Categoría:</strong> " + escapeHtml(ticket.getCategory()) + "</p>"
                + "<p><strong>Prioridad:</strong> " + escapeHtml(ticket.getPriority()) + "</p>"
                + "<p><strong>Solicitante:</strong> " + escapeHtml(creator.getName()) + " (" + escapeHtml(creator.getEmail()) + ")</p>"
                + "<p><strong>Descripción:</strong><br/>" + escapeHtml(ticket.getDesc()).replace("\n", "<br/>") + "</p>"
                + "<p><strong>Dispositivo:</strong> " + escapeHtml(ticket.getDevice()) + "</p>"
                + "<p><strong>Ubicación:</strong> " + escapeHtml(ticket.getLocation()) + "</p>"
                + "<br/>"
                + "<p><a href='https://fsatdesk-api.onrender.com'>Ver en FSATDesk</a></p>"
                + "</body></html>";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> findByTicketId(String ticketId) {
        return ticketRepository.findByTicketId(ticketId);
    }

    public List<Ticket> findByUserId(String userId) {
        return ticketRepository.findByUserId(userId);
    }

    public List<Ticket> findByTecnico(String tecnico) {
        return ticketRepository.findByTecnico(tecnico);
    }

    @Transactional
    public Ticket addComment(String ticketId, String userName, String commentText) {
        Ticket ticket = findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Activity act = Activity.builder()
                .by(userName)
                .text(commentText)
                .time(LocalTime.now().toString().substring(0, 5))
                .date(LocalDate.now())
                .build();
        ticket.getActivity().add(act);
        ticket.setUpdated(LocalDate.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket changeStatus(String ticketId, String status) {
        Ticket ticket = findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        ticket.setStatus(status);
        ticket.getActivity().add(Activity.builder()
                .by("Sistema")
                .text("Estado cambiado a: " + status)
                .time(LocalTime.now().toString().substring(0, 5))
                .date(LocalDate.now())
                .build());
        ticket.setUpdated(LocalDate.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket assignTecnico(String ticketId, String tecnicoName) {
        Ticket ticket = findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        ticket.setTecnico(tecnicoName);
        ticket.getActivity().add(Activity.builder()
                .by("Sistema")
                .text("Asignado a: " + (tecnicoName == null ? "Sin asignar" : tecnicoName))
                .time(LocalTime.now().toString().substring(0, 5))
                .date(LocalDate.now())
                .build());
        ticket.setUpdated(LocalDate.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket changePriority(String ticketId, String priority) {
        Ticket ticket = findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        ticket.setPriority(priority);
        ticket.getActivity().add(Activity.builder()
                .by("Sistema")
                .text("Prioridad cambiada a: " + priority)
                .time(LocalTime.now().toString().substring(0, 5))
                .date(LocalDate.now())
                .build());
        ticket.setUpdated(LocalDate.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public void deleteTicket(String ticketId) {
        Ticket ticket = findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        ticketRepository.delete(ticket);
    }

    public List<Ticket> filterTickets(LocalDate desde, LocalDate hasta, String status, String priority, String category) {
        return ticketRepository.findAll().stream()
                .filter(t -> desde == null || !t.getCreated().isBefore(desde))
                .filter(t -> hasta == null || !t.getCreated().isAfter(hasta))
                .filter(t -> status == null || status.isEmpty() || t.getStatus().equals(status))
                .filter(t -> priority == null || priority.isEmpty() || t.getPriority().equals(priority))
                .filter(t -> category == null || category.isEmpty() || t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
}
