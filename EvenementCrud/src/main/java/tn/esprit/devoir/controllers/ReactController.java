package tn.esprit.devoir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.ReactDto;
import tn.esprit.devoir.service.ReactService;

import java.util.Map;

@RestController
@RequestMapping("/api/react")
@CrossOrigin(origins = "*")
public class ReactController {

    @Autowired
    private ReactService reactService;

    @PostMapping("/add")
    public ResponseEntity<?> addReaction(@RequestBody ReactDto dto) {
        if (dto == null || dto.getActualiteId() == null || dto.getUsername() == null || dto.getStatus() == null) {
            return ResponseEntity.badRequest().body("Données incomplètes pour la réaction.");
        }
        reactService.add(dto.getActualiteId(), dto.getUsername(), dto.getStatus());
        return ResponseEntity.ok().build();
    }




    @GetMapping("/count/{idAct}")
    public ResponseEntity<Map<String, Long>> getReactionCounts(@PathVariable Long idAct) {
        if (idAct == null) {
            return ResponseEntity.badRequest().build();
        }
        Map<String, Long> counts = reactService.getReactionCounts(idAct);
        return ResponseEntity.ok(counts);
    }


    @GetMapping("/user-reaction/{idAct}/{username}")
    public ResponseEntity<Boolean> getUserReaction(@PathVariable Long idAct, @PathVariable String username) {
        if (idAct == null || username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Boolean reaction = reactService.getUserReaction(idAct, username);
        return ResponseEntity.ok(reaction); // true / false / null
    }


}
