package com.releasescribe.dto;

import com.releasescribe.model.ReleaseNote;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReleaseNoteResponse {

    private UUID id;
    private String title;
    private String version;
    private String rawCommits;
    private String generatedMarkdown;
    private LocalDateTime createdAt;

    public ReleaseNoteResponse(ReleaseNote note, boolean includeContent) {
        this.id = note.getId();
        this.title = note.getTitle();
        this.version = note.getVersion();
        this.createdAt = note.getCreatedAt();
        if (includeContent) {
            this.rawCommits = note.getRawCommits();
            this.generatedMarkdown = note.getGeneratedMarkdown();
        }
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getVersion() { return version; }
    public String getRawCommits() { return rawCommits; }
    public String getGeneratedMarkdown() { return generatedMarkdown; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
