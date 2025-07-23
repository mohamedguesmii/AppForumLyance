package tn.esprit.devoir.service;

import tn.esprit.devoir.entite.Ticket;
import java.util.List;

public interface ITicketService {




    public Ticket  ajouterTicket (Ticket ticket);
    public Ticket updateTicket(Ticket ticket);
    Ticket getTicketById(int id);
    void deleteTicket(int id);
    public List<Ticket> getAllTickets();


}

