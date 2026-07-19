package com.fsat.fsatdesk_api.controller;

import com.fsat.fsatdesk_api.dto.CommentRequest;
import com.fsat.fsatdesk_api.dto.TicketRequest;
import com.fsat.fsatdesk_api.model.Ticket;
import com.fsat.fsatdesk_api.security.UserPrincipal;
import com.fsat.fsatdesk_api.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Ticket>> getAll(@AuthenticationPrincipal UserPrincipal currentUser) {
        // Si es admin, ve todos; si es técnico, ve todos (pero con permisos de modificación restringidos)
        // Si es usuario, solo ve sus propios tickets
        if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok(ticketService.findAll());
        } else if (currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO"))) {
            return ResponseEntity.ok(ticketService.findAll());
        } else {
            // Usuario normal
            return ResponseEntity.ok(ticketService.findByUserId(currentUser.getId()));
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getOne(@PathVariable String ticketId) {
        return ResponseEntity.ok(ticketService.findByTicketId(ticketId).orElseThrow());
    }

    @PostMapping
    public ResponseEntity<Ticket> create(@Valid @RequestBody TicketRequest request,
                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .priority(request.getPriority())
                .desc(request.getDesc())
                .device(request.getDevice())
                .location(request.getLocation())
                .build();
        return ResponseEntity.ok(ticketService.createTicket(ticket, currentUser.getId()));
    }

    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TECNICO') and hasAuthority('PERM_STS'))")
    public ResponseEntity<Ticket> changeStatus(@PathVariable String ticketId, @RequestBody String status) {
        return ResponseEntity.ok(ticketService.changeStatus(ticketId, status));
    }

    @PatchMapping("/{ticketId}/assign")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TECNICO') and hasAuthority('PERM_ASG'))")
    public ResponseEntity<Ticket> assignTecnico(@PathVariable String ticketId, @RequestBody String tecnicoName) {
        return ResponseEntity.ok(ticketService.assignTecnico(ticketId, tecnicoName));
    }

    @PatchMapping("/{ticketId}/priority")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TECNICO') and hasAuthority('PERM_PRI'))")
    public ResponseEntity<Ticket> changePriority(@PathVariable String ticketId, @RequestBody String priority) {
        return ResponseEntity.ok(ticketService.changePriority(ticketId, priority));
    }

    @PostMapping("/{ticketId}/comment")
    public ResponseEntity<Ticket> addComment(@PathVariable String ticketId,
                                             @Valid @RequestBody CommentRequest commentRequest,
                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ticketService.addComment(ticketId, currentUser.getUsername(), commentRequest.getComment()));
    }

    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TECNICO') and hasAuthority('PERM_DEL'))")
    public ResponseEntity<Void> delete(@PathVariable String ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }
}