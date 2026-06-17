package com.releasescribe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public OpenAiClient(RestTemplate openAiRestTemplate,
                        ObjectMapper objectMapper,
                        @Value("${OPENAI_API_KEY:}") String apiKey,
                        @Value("${OPENAI_MODEL:gpt-4o-mini}") String model) {
        this.restTemplate = openAiRestTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @PostConstruct
    public void checkConfig() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OPENAI_API_KEY no configurada. Solo funcionará el fallback por keywords.");
        }
    }

    @Override
    public AiResult classifyAndSummarize(String rawCommits) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("Sin API key, usando fallback directamente");
            return fallback(rawCommits);
        }
        try {
            return callOpenAI(rawCommits);
        } catch (Exception e) {
            log.warn("Error llamando a OpenAI (intento 1): {}", e.getMessage());
            try {
                Thread.sleep(1000);
                return callOpenAI(rawCommits);
            } catch (Exception e2) {
                log.error("Error llamando a OpenAI (intento 2, usando fallback): {}", e2.getMessage());
                return fallback(rawCommits);
            }
        }
    }

    private AiResult callOpenAI(String rawCommits) {
        HttpEntity<String> requestEntity = buildRequest(rawCommits);

        ResponseEntity<String> response = restTemplate.exchange(
                API_URL, HttpMethod.POST, requestEntity, String.class);

        String content = extractContent(response.getBody());
        return parseResult(content);
    }

    private HttpEntity<String> buildRequest(String rawCommits) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0.2);
        body.set("response_format", objectMapper.createObjectNode().put("type", "json_object"));

        ArrayNode messages = body.putArray("messages");

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", String.join("\n",
                "Eres un asistente que clasifica commits de git y genera resúmenes de release notes.",
                "Recibes una lista de commits y debes devolver un objeto JSON con esta estructura EXACTA:",
                "{",
                "  \"groups\": [",
                "    { \"type\": \"features\", \"items\": [\"resumen del commit 1\", \"resumen del commit 2\"] },",
                "    { \"type\": \"fixes\", \"items\": [\"...\"] },",
                "    { \"type\": \"breaking_changes\", \"items\": [\"...\"] },",
                "    { \"type\": \"chores\", \"items\": [\"...\"] },",
                "    { \"type\": \"docs\", \"items\": [\"...\"] }",
                "  ],",
                "  \"summary\": \"Resumen general de 2-3 oraciones en español\"",
                "}",
                "",
                "Reglas:",
                "- Cada commit va en UNA sola categoría.",
                "- Si un commit es breaking change, tiene prioridad sobre otras categorías.",
                "- Los items deben ser resúmenes cortos (máximo 15 palabras) en español, en infinitivo.",
                "- Si no hay commits de una categoría, no incluyas ese grupo.",
                "- No incluyas markdown ni texto adicional fuera del JSON.",
                "- Los commits pueden no tener prefijo convencional (feat:, fix:, etc.). Clasificalos por su contenido semántico."
        ));

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", "Clasifica y resume estos commits:\n\n" + rawCommits);

        return new HttpEntity<>(body.toString(), headers);
    }

    private String extractContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo contenido de respuesta OpenAI", e);
        }
    }

    private AiResult parseResult(String content) {
        try {
            return objectMapper.readValue(content, AiResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parseando JSON de respuesta de IA", e);
        }
    }

    private static final List<String> GARBAGE_PREFIXES = List.of("~", "(END)");
    private static final List<String> FEAT_KEYWORDS = List.of(
            "feat", "feature", "agregar", "añadir", "nuev", "crear", "implement", "soporte", "integracion",
            "add", "new", "support", "integration"
    );
    private static final List<String> FIX_KEYWORDS = List.of(
            "fix", "bug", "corregir", "arreglar", "solucionar", "hotfix", "patch"
    );
    private static final List<String> CHORE_KEYWORDS = List.of(
            "chore", "refactor", "actualizar", "mejorar", "cambiar", "upgrade", "update",
            "bump", "dependencia", "migrar", "limpiar", "renombrar"
    );
    private static final List<String> DOCS_KEYWORDS = List.of(
            "docs", "doc", "documentacion", "readme"
    );
    private static final List<String> BREAKING_KEYWORDS = List.of(
            "breaking", "rompe", "cambio disruptivo", "major"
    );

    // Fallback: clasificación por palabras clave en toda la línea
    private AiResult fallback(String rawCommits) {
        List<String> lines = Arrays.stream(rawCommits.split("\n"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .filter(l -> GARBAGE_PREFIXES.stream().noneMatch(l::startsWith))
                .collect(Collectors.toList());

        List<String> features = new ArrayList<>();
        List<String> fixes = new ArrayList<>();
        List<String> breaking = new ArrayList<>();
        List<String> chores = new ArrayList<>();
        List<String> docs = new ArrayList<>();

        for (String line : lines) {
            String lower = line.toLowerCase();

            String category = classifyByKeywords(lower);
            String msg = stripGitMetadata(line);

            switch (category) {
                case "breaking" -> breaking.add(msg);
                case "fixes" -> fixes.add(msg);
                case "docs" -> docs.add(msg);
                case "chores" -> chores.add(msg);
                default -> features.add(msg);
            }
        }

        AiResult result = new AiResult();
        List<AiResult.Group> groups = new ArrayList<>();

        addGroup(groups, "breaking_changes", breaking);
        addGroup(groups, "features", features);
        addGroup(groups, "fixes", fixes);
        addGroup(groups, "docs", docs);
        addGroup(groups, "chores", chores);

        result.setGroups(groups);
        result.setSummary("Notas de versión generadas automáticamente a partir de " + lines.size() + " commits.");
        return result;
    }

    private String stripGitMetadata(String line) {
        // Remueve hash de commit al inicio (ej: "945a2e6 (HEAD -> master) feat: msg")
        line = line.replaceAll("^[0-9a-f]{7,40}(\\s+\\([^)]*\\))?[:\\s]*", "")
                .trim();
        // Remueve prefijo convencional (feat:, fix:, etc.)
        line = line.replaceAll("^(feat|fix|chore|docs|refactor|breaking)(\\([^)]*\\))?[:\\s]*", "")
                .trim();
        // Remueve guiones iniciales
        line = line.replaceAll("^[-*]\\s*", "")
                .trim();
        return line;
    }

    private String classifyByKeywords(String lower) {
        if (containsAny(lower, BREAKING_KEYWORDS)) return "breaking";
        if (containsAny(lower, FIX_KEYWORDS)) return "fixes";
        if (containsAny(lower, DOCS_KEYWORDS)) return "docs";
        if (containsAny(lower, CHORE_KEYWORDS)) return "chores";
        if (containsAny(lower, FEAT_KEYWORDS)) return "features";
        return "features";
    }

    private boolean containsAny(String text, List<String> keywords) {
        String lower = text.toLowerCase();
        return keywords.stream().anyMatch(lower::contains);
    }

    private void addGroup(List<AiResult.Group> groups, String type, List<String> items) {
        if (items.isEmpty()) return;
        AiResult.Group g = new AiResult.Group();
        g.setType(type);
        g.setItems(items);
        groups.add(g);
    }
}
