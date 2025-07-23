package tn.esprit.devoir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.service.SearchService;
import tn.esprit.devoir.dto.DocumentDTO;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private SearchService searchService;

    public static class SearchRequest {
        private String query;
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
    }

    @PostMapping(value = "/search", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<DocumentDTO>> search(@RequestBody SearchRequest request) {
        // Par exemple, 30 résultats max
        List<DocumentDTO> results = searchService.searchDocuments(request.getQuery(), 30);
        return ResponseEntity.ok(results);
    }

}
