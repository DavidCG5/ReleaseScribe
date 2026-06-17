package com.releasescribe.service;

import com.releasescribe.dto.GenerateRequest;
import com.releasescribe.dto.GenerateResponse;
import com.releasescribe.model.ReleaseNote;
import com.releasescribe.repository.ReleaseNoteRepository;
import org.springframework.stereotype.Service;

@Service
public class ReleaseNoteService {

    private final AiClient aiClient;
    private final MarkdownBuilder markdownBuilder;
    private final ReleaseNoteRepository repository;

    public ReleaseNoteService(AiClient aiClient, MarkdownBuilder markdownBuilder, ReleaseNoteRepository repository) {
        this.aiClient = aiClient;
        this.markdownBuilder = markdownBuilder;
        this.repository = repository;
    }

    public GenerateResponse generate(GenerateRequest request) {
        AiResult result = aiClient.classifyAndSummarize(request.getRawCommits());

        String markdown = markdownBuilder.build(result, request.getVersion());

        String title = (request.getVersion() != null && !request.getVersion().isBlank())
                ? "Release v" + request.getVersion()
                : "Release Notes";

        ReleaseNote note = new ReleaseNote(title, request.getVersion(), request.getRawCommits(), markdown);
        note = repository.save(note);

        return new GenerateResponse(note.getId(), note.getTitle(), note.getGeneratedMarkdown(), note.getCreatedAt());
    }
}
