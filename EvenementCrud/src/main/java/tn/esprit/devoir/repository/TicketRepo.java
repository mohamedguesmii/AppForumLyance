package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.devoir.entite.Ticket;

public interface TicketRepo extends JpaRepository<Ticket,Integer> {
}

