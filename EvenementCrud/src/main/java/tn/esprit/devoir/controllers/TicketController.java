package tn.esprit.devoir.controllers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.entite.Ticket;
import tn.esprit.devoir.service.ITicketService;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level= AccessLevel.PRIVATE)
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    ITicketService iTicketService;

    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE");
        }
    }



    @GetMapping("/allTickets")
    public List<Ticket> gettingallTickets() {
        return iTicketService.getAllTickets();}

    @GetMapping("/ticketById/{idticket}")
    public Ticket gettingTicket(@RequestParam("idticket") int idticket) {
        return iTicketService.getTicketById(idticket);}



    @PostMapping("/add-ticket")
    public void addTicket(@RequestBody Ticket t) {iTicketService.ajouterTicket(t);}

    @PutMapping("/updateTicket")
    public Ticket updateTicket(@RequestBody Ticket t) {
        return iTicketService.updateTicket(t);
    }

    @DeleteMapping("/deleteTicket/{id}")
    public void deleteTicket(@PathVariable("id") int id) {
        iTicketService.deleteTicket(id);
    }
}
