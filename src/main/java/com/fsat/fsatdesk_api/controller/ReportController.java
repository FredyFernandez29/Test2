package com.fsat.fsatdesk_api.controller;

import com.fsat.fsatdesk_api.model.Ticket;
import com.fsat.fsatdesk_api.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TicketService ticketService;

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_REPORT')")
    public List<Ticket> filterReports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category
    ) {
        return ticketService.filterTickets(desde, hasta, status, priority, category);
    }
}