package tn.esprit.devoir.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.dto.DocumentDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    // Chemin complet vers le script Python
    private static final String PYTHON_SCRIPT_PATH = "C:/Users/gasmi/OneDrive/Desktop/RAG/rag_advanced.py";

    // Dossier de travail (working directory)
    private static final String WORKING_DIR = "C:/Users/gasmi/OneDrive/Desktop/RAG";

    // Limite maximale taille sortie (en caractères) pour éviter OutOfMemory (arbitraire)
    private static final int MAX_OUTPUT_LENGTH = 5_000_000;

    /**
     * Recherche des documents via script Python RAG local.
     *
     * @param query      la requête
     * @param maxResults nombre max de résultats (k)
     * @return liste de DocumentDTO
     */
    public List<DocumentDTO> searchDocuments(String query, int maxResults) {
        ProcessBuilder pb = new ProcessBuilder(
            "python",
            PYTHON_SCRIPT_PATH,
            query,
            String.valueOf(maxResults)
        );
        pb.directory(new File(WORKING_DIR));
        pb.redirectErrorStream(false); // On gère séparément stdout et stderr

        try {
            final Process process = pb.start();

            // Lecture stdout dans un thread séparé
            BufferedReader stdOutReader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            // Lecture stderr dans un thread séparé
            BufferedReader stdErrReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // Récupérer stdout en parallèle
            String stdout = stdOutReader.lines()
                .collect(Collectors.joining("\n"));

            // Récupérer stderr en parallèle
            String stderr = stdErrReader.lines()
                .collect(Collectors.joining("\n"));

            // Attente max 60s, sinon kill process
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                logger.error("Le script Python a dépassé le temps limite de 60 secondes");
                return Collections.emptyList();
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                logger.error("Erreur script Python, code: {}", exitCode);
                logger.error("Sortie erreur (stderr): {}", stderr);
                logger.error("Sortie standard (stdout): {}", stdout);
                return Collections.emptyList();
            }

            // Protection contre sortie trop volumineuse
            if (stdout.length() > MAX_OUTPUT_LENGTH) {
                logger.error("Sortie du script Python trop volumineuse ({} caractères)", stdout.length());
                return Collections.emptyList();
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(stdout, new TypeReference<List<DocumentDTO>>() {});

        } catch (Exception e) {
            logger.error("Exception lors de l'exécution du script Python", e);
            return Collections.emptyList();
        }
    }
}
