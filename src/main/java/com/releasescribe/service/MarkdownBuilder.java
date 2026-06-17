package com.releasescribe.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class MarkdownBuilder {

    private static final Map<String, String> HEADERS = Map.of(
            "features",             "## ✨ Nuevas funcionalidades",
            "fixes",                "## 🐛 Correcciones",
            "breaking_changes",     "## ⚠️ Cambios disruptivos",
            "chores",               "## 🔧 Mantenimiento",
            "docs",                 "## 📚 Documentación"
    );

    private static final List<String> ORDER = List.of(
            "breaking_changes", "features", "fixes", "docs", "chores"
    );

    public String build(AiResult aiResult, String version) {
        StringBuilder md = new StringBuilder();

        if (version != null && !version.isBlank()) {
            md.append("# Release v").append(version).append("\n\n");
        } else {
            md.append("# Release Notes\n\n");
        }

        md.append("> **Fecha:** ").append(LocalDate.now()).append("\n\n");

        md.append("## Resumen\n\n");
        md.append(aiResult.getSummary()).append("\n\n");

        for (String type : ORDER) {
            AiResult.Group group = findGroup(aiResult.getGroups(), type);
            if (group == null) continue;

            String header = HEADERS.getOrDefault(type, "## " + type);
            md.append(header).append("\n\n");

            for (String item : group.getItems()) {
                md.append("- ").append(item).append("\n");
            }
            md.append("\n");
        }

        return md.toString();
    }

    private AiResult.Group findGroup(List<AiResult.Group> groups, String type) {
        if (groups == null) return null;
        return groups.stream()
                .filter(g -> type.equals(g.getType()))
                .findFirst()
                .orElse(null);
    }
}
