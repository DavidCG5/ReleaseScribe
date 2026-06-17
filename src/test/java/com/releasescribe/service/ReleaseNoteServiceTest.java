package com.releasescribe.service;

import com.releasescribe.dto.GenerateRequest;
import com.releasescribe.dto.GenerateResponse;
import com.releasescribe.model.ReleaseNote;
import com.releasescribe.repository.ReleaseNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseNoteServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private MarkdownBuilder markdownBuilder;

    @Mock
    private ReleaseNoteRepository repository;

    @Captor
    private ArgumentCaptor<ReleaseNote> noteCaptor;

    private ReleaseNoteService service;

    @BeforeEach
    void setUp() {
        service = new ReleaseNoteService(aiClient, markdownBuilder, repository);
    }

    @Test
    void generate_shouldReturnResponseWithCorrectFields() {
        String rawCommits = "feat: add login\nfix: fix crash";
        String version = "1.0.0";

        AiResult aiResult = new AiResult();
        aiResult.setSummary("Resumen de prueba");
        AiResult.Group group = new AiResult.Group();
        group.setType("features");
        group.setItems(List.of("Agregar login"));
        aiResult.setGroups(List.of(group));

        when(aiClient.classifyAndSummarize(rawCommits)).thenReturn(aiResult);
        when(markdownBuilder.build(aiResult, version)).thenReturn("# Markdown generado");
        when(repository.save(any())).thenAnswer(invocation -> {
            ReleaseNote saved = invocation.getArgument(0);
            return new ReleaseNote(saved.getTitle(), saved.getVersion(),
                    saved.getRawCommits(), saved.getGeneratedMarkdown()) {{
                setId(UUID.randomUUID());
                setCreatedAt(LocalDateTime.now());
            }};
        });

        GenerateResponse response = service.generate(new GenerateRequest() {{
            setRawCommits(rawCommits);
            setVersion(version);
        }});

        assertNotNull(response.getId());
        assertEquals("Release v1.0.0", response.getTitle());
        assertNotNull(response.getMarkdown());
        assertNotNull(response.getCreatedAt());

        verify(repository).save(noteCaptor.capture());
        ReleaseNote captured = noteCaptor.getValue();
        assertEquals("Release v1.0.0", captured.getTitle());
        assertEquals("1.0.0", captured.getVersion());
        assertEquals(rawCommits, captured.getRawCommits());
    }

    @Test
    void generate_withoutVersion_shouldUseDefaultTitle() {
        String rawCommits = "feat: add login";

        AiResult aiResult = new AiResult();
        aiResult.setSummary("Resumen");
        aiResult.setGroups(List.of());

        when(aiClient.classifyAndSummarize(rawCommits)).thenReturn(aiResult);
        when(markdownBuilder.build(aiResult, null)).thenReturn("# Markdown");
        when(repository.save(any())).thenAnswer(invocation -> {
            ReleaseNote saved = invocation.getArgument(0);
            return new ReleaseNote(saved.getTitle(), saved.getVersion(),
                    saved.getRawCommits(), saved.getGeneratedMarkdown()) {{
                setId(UUID.randomUUID());
                setCreatedAt(LocalDateTime.now());
            }};
        });

        GenerateResponse response = service.generate(new GenerateRequest() {{
            setRawCommits(rawCommits);
        }});

        assertEquals("Release Notes", response.getTitle());
    }

    @Test
    void listAll_shouldReturnNotesOrderedByDate() {
        ReleaseNote note1 = new ReleaseNote("v1", "1.0", "a", "md1");
        ReleaseNote note2 = new ReleaseNote("v2", "2.0", "b", "md2");

        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(note2, note1));

        var result = service.listAll();

        assertEquals(2, result.size());
        assertEquals("v2", result.getFirst().getTitle());
        verify(repository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getById_existingNote_shouldReturnFullResponse() {
        UUID id = UUID.randomUUID();
        ReleaseNote note = new ReleaseNote("v1", "1.0", "raw", "md");
        note.setId(id);
        note.setCreatedAt(LocalDateTime.now());

        when(repository.findById(id)).thenReturn(Optional.of(note));

        var result = service.getById(id);

        assertNotNull(result);
        assertEquals("v1", result.getTitle());
        assertEquals("raw", result.getRawCommits());
        assertEquals("md", result.getGeneratedMarkdown());
    }

    @Test
    void getById_nonExistingNote_shouldReturnNull() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertNull(service.getById(id));
    }
}
