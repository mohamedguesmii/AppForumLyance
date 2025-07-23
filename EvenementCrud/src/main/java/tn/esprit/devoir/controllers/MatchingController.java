package tn.esprit.devoir.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.ForumMatchDTO;
import tn.esprit.devoir.service.MatchingService;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchingController {

    private final MatchingService matchingService;

    // 🔥 CORRECT mapping ici
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ForumMatchDTO>> getRecommendedForums(@PathVariable Integer userId) {
        List<ForumMatchDTO> matchedForums = matchingService.getBestForumsForUser(userId);
        return ResponseEntity.ok(matchedForums);
    }
}
