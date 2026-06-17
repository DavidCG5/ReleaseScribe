package com.releasescribe.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class GenerateResponse {

    private UUID id;
    private String title;
    private String markdown;
    private LocalDateTime createdAt;

    public GenerateResponse(UUID id, String title, String markdown, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.markdown = markdown;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getMarkdown() { return markdown; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
