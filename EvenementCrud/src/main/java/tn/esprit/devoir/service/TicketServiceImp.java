package tn.esprit.devoir.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.entite.Ticket;
import tn.esprit.devoir.repository.TicketRepo;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TicketServiceImp  implements ITicketService {
    TicketRepo ticketRepo;



    @Override
    public Ticket ajouterTicket(Ticket ticket) {return  ticketRepo.save(ticket);}


    @Override
    public Ticket updateTicket(Ticket ticket) {return ticketRepo.save(ticket);}


    @Override
    public Ticket getTicketById(int id) {return ticketRepo.findById(id).orElse(null);}

    @Override
    public void deleteTicket(int id) {ticketRepo.deleteById(id);}
    @Override
    public List <Ticket> getAllTickets() {return ticketRepo.findAll();}




}
