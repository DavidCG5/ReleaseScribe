package com.releasescribe.controller;

import com.releasescribe.dto.GenerateRequest;
import com.releasescribe.dto.GenerateResponse;
import com.releasescribe.service.ReleaseNoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/release-notes")
public class ReleaseNoteController {

    private final ReleaseNoteService releaseNoteService;

    public ReleaseNoteController(ReleaseNoteService releaseNoteService) {
        this.releaseNoteService = releaseNoteService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generate(@RequestBody GenerateRequest request) {
        if (request.getRawCommits() == null || request.getRawCommits().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        GenerateResponse response = releaseNoteService.generate(request);
        return ResponseEntity.ok(response);
    }
}
