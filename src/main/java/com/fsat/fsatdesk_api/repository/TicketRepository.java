package com.fsat.fsatdesk_api.repository;

import com.fsat.fsatdesk_api.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Optional<Ticket> findByTicketId(String ticketId);

    List<Ticket> findByUserId(String userId);

    List<Ticket> findByTecnico(String tecnico);

    // Método para filtros en reportes (con JPQL)
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:desde IS NULL OR t.created >= :desde) AND " +
           "(:hasta IS NULL OR t.created <= :hasta) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:category IS NULL OR t.category = :category)")
    List<Ticket> findFiltered(@Param("desde") LocalDate desde,
                              @Param("hasta") LocalDate hasta,
                              @Param("status") String status,
                              @Param("priority") String priority,
                              @Param("category") String category);
}