package com.bnpaper.agento.knowledge;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeDocumentService {

    private final KnowledgeDocumentRepository docRepo;
    private final KnowledgeChunkRepository chunkRepo;
    private final ChunkingService chunkingService;
    private final EmbeddingProvider embeddingProvider;

    public List<KnowledgeDocumentDto.Response> findAll() {
        return docRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(doc -> KnowledgeDocumentDto.toResponse(doc, chunkRepo.countByDocumentId(doc.getId())))
                .toList();
    }

    public KnowledgeDocumentDto.Response findById(UUID id) {
        KnowledgeDocument doc = requireDocument(id);
        return KnowledgeDocumentDto.toResponse(doc, chunkRepo.countByDocumentId(id));
    }

    public List<KnowledgeDocumentDto.ChunkResponse> findChunks(UUID documentId) {
        if (!docRepo.existsById(documentId)) {
            throw new ResourceNotFoundException("KnowledgeDocument", documentId);
        }
        return chunkRepo.findByDocumentIdOrderByChunkIndex(documentId).stream()
                .map(KnowledgeDocumentDto::toChunkResponse)
                .toList();
    }

    @Transactional
    public KnowledgeDocumentDto.Response create(KnowledgeDocumentDto.Request request) {
        KnowledgeDocument doc = KnowledgeDocument.builder()
                .title(request.getTitle())
                .type(request.getType())
                .content(request.getContent())
                .source(request.getSource())
                .tags(request.getTags())
                .status(DocumentStatus.ACTIVE)
                .build();
        doc = docRepo.save(doc);
        embedAndSaveChunks(doc);
        return KnowledgeDocumentDto.toResponse(doc, chunkRepo.countByDocumentId(doc.getId()));
    }

    @Transactional
    public KnowledgeDocumentDto.Response update(UUID id, KnowledgeDocumentDto.Request request) {
        KnowledgeDocument doc = requireDocument(id);
        doc.setTitle(request.getTitle());
        doc.setType(request.getType());
        doc.setContent(request.getContent());
        doc.setSource(request.getSource());
        doc.setTags(request.getTags());
        doc = docRepo.save(doc);

        // Re-chunk and re-embed on content change
        chunkRepo.deleteByDocumentId(id);
        embedAndSaveChunks(doc);

        return KnowledgeDocumentDto.toResponse(doc, chunkRepo.countByDocumentId(id));
    }

    @Transactional
    public void archive(UUID id) {
        KnowledgeDocument doc = requireDocument(id);
        doc.setStatus(DocumentStatus.ARCHIVED);
        docRepo.save(doc);
    }

    @Transactional
    public void delete(UUID id) {
        if (!docRepo.existsById(id)) {
            throw new ResourceNotFoundException("KnowledgeDocument", id);
        }
        docRepo.deleteById(id);
    }

    public KnowledgeSearchResult search(KnowledgeSearchRequest request) {
        float[] queryEmbedding = embeddingProvider.embed(request.getQuery());
        if (queryEmbedding == null) {
            log.warn("Could not embed search query — returning empty results");
            return KnowledgeSearchResult.builder()
                    .query(request.getQuery())
                    .results(List.of())
                    .build();
        }

        String queryVector = toVectorString(queryEmbedding);
        String docTypeFilter = request.getDocumentType() != null
                ? request.getDocumentType().name() : null;

        List<Object[]> rows = chunkRepo.searchSimilar(queryVector, docTypeFilter, request.getTopK());
        List<KnowledgeSearchResult.ChunkMatch> matches = new ArrayList<>();

        for (Object[] row : rows) {
            double score = row[7] != null ? ((Number) row[7]).doubleValue() : 0.0;
            if (score < request.getMinScore()) {
                continue;
            }
            matches.add(KnowledgeSearchResult.ChunkMatch.builder()
                    .chunkId(toUUID(row[0]))
                    .documentId(toUUID(row[1]))
                    .chunkIndex(row[2] != null ? ((Number) row[2]).intValue() : 0)
                    .chunkText(row[3] != null ? row[3].toString() : "")
                    .documentTitle(row[5] != null ? row[5].toString() : "")
                    .documentType(row[6] != null ? row[6].toString() : "")
                    .score(Math.round(score * 10000.0) / 10000.0)
                    .build());
        }

        return KnowledgeSearchResult.builder()
                .query(request.getQuery())
                .results(matches)
                .build();
    }

    // --- Helpers ---

    private void embedAndSaveChunks(KnowledgeDocument doc) {
        List<String> chunkTexts = chunkingService.chunk(doc.getContent());
        List<KnowledgeChunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkTexts.size(); i++) {
            String text = chunkTexts.get(i);
            float[] embedding = embeddingProvider.embed(text);
            chunks.add(KnowledgeChunk.builder()
                    .documentId(doc.getId())
                    .chunkIndex(i)
                    .chunkText(text)
                    .embedding(embedding != null ? toVectorString(embedding) : null)
                    .build());
        }
        chunkRepo.saveAll(chunks);
        log.info("Saved {} chunks for document '{}' (id={})", chunks.size(), doc.getTitle(), doc.getId());
    }

    /** Format float[] as pgvector bracket literal: [0.1,0.2,...] */
    static String toVectorString(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(v[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private KnowledgeDocument requireDocument(UUID id) {
        return docRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeDocument", id));
    }

    private UUID toUUID(Object obj) {
        if (obj == null) return null;
        if (obj instanceof UUID u) return u;
        return UUID.fromString(obj.toString());
    }
}
