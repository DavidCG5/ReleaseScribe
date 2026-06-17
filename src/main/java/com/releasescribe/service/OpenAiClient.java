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
            throw new IllegalStateException("OPENAI_API_KEY no está configurada. " +
                    "Definí la variable de entorno OPENAI_API_KEY antes de arrancar.");
        }
    }

    @Override
    public AiResult classifyAndSummarize(String rawCommits) {
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
                "- No incluyas markdown ni texto adicional fuera del JSON."
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

    // Fallback: clasificación básica por palabras clave en los mensajes de commit
    private AiResult fallback(String rawCommits) {
        List<String> lines = Arrays.stream(rawCommits.split("\n"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toList());

        List<String> features = new ArrayList<>();
        List<String> fixes = new ArrayList<>();
        List<String> breaking = new ArrayList<>();
        List<String> chores = new ArrayList<>();
        List<String> docs = new ArrayList<>();

        for (String line : lines) {
            String lower = line.toLowerCase();
            if (lower.contains("breaking")) {
                breaking.add(clean(line));
            } else if (lower.startsWith("feat") || lower.startsWith("feature")) {
                features.add(clean(line));
            } else if (lower.startsWith("fix")) {
                fixes.add(clean(line));
            } else if (lower.startsWith("chore") || lower.startsWith("refactor")) {
                chores.add(clean(line));
            } else if (lower.startsWith("docs") || lower.startsWith("doc")) {
                docs.add(clean(line));
            } else {
                features.add(clean(line));
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

    private void addGroup(List<AiResult.Group> groups, String type, List<String> items) {
        if (items.isEmpty()) return;
        AiResult.Group g = new AiResult.Group();
        g.setType(type);
        g.setItems(items);
        groups.add(g);
    }

    private String clean(String line) {
        return line.replaceAll("^(feat|fix|chore|docs|refactor|breaking)(\\([^)]*\\))?[:\\s]*", "")
                .replaceAll("^[-*]\\s*", "")
                .trim();
    }
}
