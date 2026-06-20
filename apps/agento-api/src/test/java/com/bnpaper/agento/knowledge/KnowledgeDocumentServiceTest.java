package com.bnpaper.agento.knowledge;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeDocumentServiceTest {

    @Mock private KnowledgeDocumentRepository docRepo;
    @Mock private KnowledgeChunkRepository chunkRepo;
    @Mock private ChunkingService chunkingService;
    @Mock private EmbeddingProvider embeddingProvider;

    @InjectMocks
    private KnowledgeDocumentService service;

    private final UUID docId = UUID.randomUUID();

    private KnowledgeDocument sampleDoc() {
        return KnowledgeDocument.builder()
                .id(docId)
                .title("Brand Voice Guide")
                .type(DocumentType.BRAND_GUIDELINE)
                .content("SoClean brand voice: warm, honest, approachable.")
                .status(DocumentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setup() {
        when(embeddingProvider.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f, 0.3f});
        when(chunkingService.chunk(anyString())).thenReturn(List.of("SoClean brand voice: warm, honest."));
    }

    @Test
    void findAll_returnsListWithChunkCounts() {
        when(docRepo.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(sampleDoc()));
        when(chunkRepo.countByDocumentId(docId)).thenReturn(3);

        List<KnowledgeDocumentDto.Response> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Brand Voice Guide");
        assertThat(result.get(0).getChunkCount()).isEqualTo(3);
    }

    @Test
    void findById_returnsDocument() {
        when(docRepo.findById(docId)).thenReturn(Optional.of(sampleDoc()));
        when(chunkRepo.countByDocumentId(docId)).thenReturn(2);

        KnowledgeDocumentDto.Response result = service.findById(docId);

        assertThat(result.getId()).isEqualTo(docId);
        assertThat(result.getType()).isEqualTo(DocumentType.BRAND_GUIDELINE);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(docRepo.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesDocumentAndChunks() {
        KnowledgeDocumentDto.Request request = new KnowledgeDocumentDto.Request();
        request.setTitle("Brand Voice Guide");
        request.setType(DocumentType.BRAND_GUIDELINE);
        request.setContent("SoClean brand voice: warm, honest, approachable.");

        when(docRepo.save(any())).thenReturn(sampleDoc());
        when(chunkRepo.countByDocumentId(docId)).thenReturn(1);

        KnowledgeDocumentDto.Response result = service.create(request);

        assertThat(result.getTitle()).isEqualTo("Brand Voice Guide");
        verify(chunkRepo).saveAll(anyList());
        verify(embeddingProvider, atLeastOnce()).embed(anyString());
    }

    @Test
    void create_savesChunkWithoutEmbeddingWhenProviderReturnsNull() {
        when(embeddingProvider.embed(anyString())).thenReturn(null);
        KnowledgeDocumentDto.Request request = new KnowledgeDocumentDto.Request();
        request.setTitle("Test");
        request.setType(DocumentType.APPROVED_CLAIM);
        request.setContent("ฝุ่นน้อย ใช้งานทุกวัน");

        when(docRepo.save(any())).thenReturn(sampleDoc());
        when(chunkRepo.countByDocumentId(docId)).thenReturn(1);

        service.create(request);

        verify(chunkRepo).saveAll(argThat((List<KnowledgeChunk> chunks) ->
                chunks.stream().allMatch(c -> c.getEmbedding() == null)));
    }

    @Test
    void update_deletesOldChunksAndReEmbeds() {
        KnowledgeDocumentDto.Request request = new KnowledgeDocumentDto.Request();
        request.setTitle("Updated");
        request.setType(DocumentType.BRAND_GUIDELINE);
        request.setContent("Updated content.");

        when(docRepo.findById(docId)).thenReturn(Optional.of(sampleDoc()));
        when(docRepo.save(any())).thenReturn(sampleDoc());
        when(chunkRepo.countByDocumentId(docId)).thenReturn(1);

        service.update(docId, request);

        verify(chunkRepo).deleteByDocumentId(docId);
        verify(chunkRepo).saveAll(anyList());
    }

    @Test
    void delete_removesDocument() {
        when(docRepo.existsById(docId)).thenReturn(true);
        service.delete(docId);
        verify(docRepo).deleteById(docId);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(docRepo.existsById(any())).thenReturn(false);
        assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void search_returnsEmptyWhenEmbeddingFails() {
        when(embeddingProvider.embed(anyString())).thenReturn(null);

        KnowledgeSearchRequest req = new KnowledgeSearchRequest();
        req.setQuery("brand voice");

        KnowledgeSearchResult result = service.search(req);

        assertThat(result.getResults()).isEmpty();
        verify(chunkRepo, never()).searchSimilar(any(), any(), anyInt());
    }

    @Test
    void search_filtersResultsBelowMinScore() {
        when(embeddingProvider.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        // Row: id, documentId, chunkIndex, chunkText, metadata, docTitle, docType, score
        Object[] lowScoreRow = {UUID.randomUUID(), UUID.randomUUID(), 0, "chunk", null,
                "Brand Guide", "BRAND_GUIDELINE", 0.3};
        List<Object[]> lowRows = Collections.singletonList(lowScoreRow);
        when(chunkRepo.searchSimilar(anyString(), any(), anyInt())).thenReturn(lowRows);

        KnowledgeSearchRequest req = new KnowledgeSearchRequest();
        req.setQuery("brand voice");
        req.setMinScore(0.7);

        KnowledgeSearchResult result = service.search(req);

        assertThat(result.getResults()).isEmpty();
    }

    @Test
    void search_returnsMatchesAboveThreshold() {
        when(embeddingProvider.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        UUID chunkId = UUID.randomUUID();
        UUID dId = UUID.randomUUID();
        Object[] row = {chunkId, dId, 0, "SoClean brand voice text", null,
                "Brand Guide", "BRAND_GUIDELINE", 0.87};
        List<Object[]> rows = Collections.singletonList(row);
        when(chunkRepo.searchSimilar(anyString(), any(), anyInt())).thenReturn(rows);

        KnowledgeSearchRequest req = new KnowledgeSearchRequest();
        req.setQuery("brand voice");
        req.setMinScore(0.5);

        KnowledgeSearchResult result = service.search(req);

        assertThat(result.getResults()).hasSize(1);
        assertThat(result.getResults().get(0).getScore()).isGreaterThan(0.5);
        assertThat(result.getResults().get(0).getChunkText()).isEqualTo("SoClean brand voice text");
    }
}
