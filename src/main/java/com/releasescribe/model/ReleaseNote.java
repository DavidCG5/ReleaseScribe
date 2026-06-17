package com.releasescribe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "release_notes")
public class ReleaseNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String version;

    @Column(name = "raw_commits", columnDefinition = "TEXT", nullable = false)
    private String rawCommits;

    @Column(name = "generated_markdown", columnDefinition = "TEXT", nullable = false)
    private String generatedMarkdown;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ReleaseNote() {}

    public ReleaseNote(String title, String version, String rawCommits, String generatedMarkdown) {
        this.title = title;
        this.version = version;
        this.rawCommits = rawCommits;
        this.generatedMarkdown = generatedMarkdown;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getRawCommits() { return rawCommits; }
    public void setRawCommits(String rawCommits) { this.rawCommits = rawCommits; }

    public String getGeneratedMarkdown() { return generatedMarkdown; }
    public void setGeneratedMarkdown(String generatedMarkdown) { this.generatedMarkdown = generatedMarkdown; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
