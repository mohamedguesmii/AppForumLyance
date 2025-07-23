package tn.esprit.devoir.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    static class ChatRequest {
        public String userId;
        public String message;
    }

    static class ChatResponse {
        public List<Response> responses = new ArrayList<>();

        static class Response {
            public String type = "text";
            public String text;

            public Response(String text) {
                this.text = text;
            }
        }

        public void addTextResponse(String text) {
            responses.add(new Response(text));
        }
    }

    // Intents avec mots clés et réponses
    private static final Map<String, List<String>> intents = new HashMap<>() {{
        put("forum", Arrays.asList(
            "Les forums ont lieu chaque trimestre, tu peux consulter le calendrier dans la section Forums.",
            "Pour participer à un forum, inscris-toi via la page dédiée."
        ));
        put("offre", Arrays.asList(
            "Les offres sont visibles dans la section Offres, n'hésite pas à postuler directement en ligne.",
            "Tu peux filtrer les offres par domaine, type et durée."
        ));
        put("evenement", Arrays.asList(
            "Les événements sont affichés dans la section Événements, pense à t'inscrire tôt !",
            "Tu trouveras tous les détails et les dates dans l’agenda."
        ));
        put("aide", Arrays.asList(
            "Je suis là pour t’aider ! Pose-moi ta question sur les forums, offres ou événements.",
            "Si tu as besoin d’assistance spécifique, contacte le support via la page Contact."
        ));
    }};

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> chatbot(@RequestBody ChatRequest request) {
        String message = request.message.toLowerCase();

        String intentDetected = null;
        int maxMatches = 0;

        for (var intent : intents.entrySet()) {
            int matches = 0;
            for (String kw : intent.getKey().split(",")) {
                if (message.contains(kw)) {
                    matches++;
                }
            }
            if (matches > maxMatches) {
                maxMatches = matches;
                intentDetected = intent.getKey();
            }
        }

        ChatResponse response = new ChatResponse();

        if (intentDetected == null) {
            response.addTextResponse("Désolé, je n'ai pas compris ta question. Essaie avec des mots comme 'forum', 'offre' ou 'événement'.");
        } else {
            List<String> responses = intents.get(intentDetected);
            // Choisir une réponse aléatoire
            String selected = responses.get(new Random().nextInt(responses.size()));
            response.addTextResponse(selected);
        }

        return ResponseEntity.ok(response);
    }
}
