package com.bnpaper.agento.knowledge;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for pgvector cosine-similarity search.
 *
 * Requires Docker. Uses a real PostgreSQL + pgvector container so that the
 * native SQL in KnowledgeChunkRepository.searchSimilar() can be exercised
 * with the actual <=> operator and ::vector cast — neither of which H2 supports.
 *
 * Run with: mvn test -Dgroups=integration
 * Skip with: mvn test -Dgroups=!integration
 */
@Tag("integration")
@SpringBootTest
@Testcontainers
@Transactional
class KnowledgeSearchIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16")
                    .withDatabaseName("agento_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private KnowledgeDocumentRepository docRepo;

    @Autowired
    private KnowledgeChunkRepository chunkRepo;

    // ── helpers ──────────────────────────────────────────────────────────────

    private KnowledgeDocument activeDoc(String title, DocumentType type) {
        return docRepo.save(KnowledgeDocument.builder()
                .title(title)
                .type(type)
                .content("content")
                .status(DocumentStatus.ACTIVE)
                .build());
    }

    private KnowledgeChunk chunk(KnowledgeDocument doc, int idx, String text, String embedding) {
        return chunkRepo.save(KnowledgeChunk.builder()
                .documentId(doc.getId())
                .chunkIndex(idx)
                .chunkText(text)
                .embedding(embedding)
                .build());
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void searchSimilar_returnsMostSimilarChunkFirst() {
        KnowledgeDocument brand = activeDoc("Brand Voice", DocumentType.BRAND_GUIDELINE);
        KnowledgeDocument product = activeDoc("Product Facts", DocumentType.PRODUCT_FACT);

        // 3-dimensional test vectors — dimension is consistent within the test.
        chunk(brand, 0, "SoClean is warm and honest", "[1.0,0.0,0.0]");
        chunk(product, 0, "2-ply 180 sheets per pack", "[0.0,1.0,0.0]");

        // Query vector identical to chunk1 → perfect cosine similarity
        List<Object[]> results = chunkRepo.searchSimilar("[1.0,0.0,0.0]", null, 10);

        assertThat(results).hasSize(2);

        String topChunkText = (String) results.get(0)[3];
        assertThat(topChunkText).isEqualTo("SoClean is warm and honest");

        double topScore = ((Number) results.get(0)[7]).doubleValue();
        assertThat(topScore).isGreaterThan(0.99);

        double secondScore = ((Number) results.get(1)[7]).doubleValue();
        assertThat(topScore).isGreaterThan(secondScore);
    }

    @Test
    void searchSimilar_excludesArchivedDocuments() {
        KnowledgeDocument archived = docRepo.save(KnowledgeDocument.builder()
                .title("Archived")
                .type(DocumentType.BRAND_GUIDELINE)
                .content("Old content")
                .status(DocumentStatus.ARCHIVED)
                .build());
        chunk(archived, 0, "Archived text", "[1.0,0.0,0.0]");

        List<Object[]> results = chunkRepo.searchSimilar("[1.0,0.0,0.0]", null, 10);

        assertThat(results).isEmpty();
    }

    @Test
    void searchSimilar_filtersByDocumentType() {
        KnowledgeDocument brand = activeDoc("Brand", DocumentType.BRAND_GUIDELINE);
        KnowledgeDocument product = activeDoc("Product", DocumentType.PRODUCT_FACT);

        chunk(brand, 0, "Brand text", "[1.0,0.0,0.0]");
        chunk(product, 0, "Product text", "[1.0,0.0,0.0]");

        List<Object[]> results = chunkRepo.searchSimilar("[1.0,0.0,0.0]", "BRAND_GUIDELINE", 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0)[6]).isEqualTo("BRAND_GUIDELINE");
    }

    @Test
    void searchSimilar_excludesChunksWithNullEmbedding() {
        KnowledgeDocument doc = activeDoc("No Embedding", DocumentType.BRAND_GUIDELINE);
        chunk(doc, 0, "No embedding text", null);

        List<Object[]> results = chunkRepo.searchSimilar("[1.0,0.0,0.0]", null, 10);

        assertThat(results).isEmpty();
    }

    @Test
    void searchSimilar_respectsTopKLimit() {
        KnowledgeDocument doc = activeDoc("Multi-chunk", DocumentType.CUSTOMER_REVIEW);
        for (int i = 0; i < 5; i++) {
            chunk(doc, i, "Review text " + i, "[1.0,0.0,0.0]");
        }

        List<Object[]> results = chunkRepo.searchSimilar("[1.0,0.0,0.0]", null, 3);

        assertThat(results).hasSize(3);
    }

    @Test
    void searchSimilar_returnsAllExpectedColumns() {
        KnowledgeDocument doc = activeDoc("Column Test", DocumentType.APPROVED_CLAIM);
        chunk(doc, 0, "Safe claim text", "[0.5,0.5,0.0]");

        List<Object[]> rows = chunkRepo.searchSimilar("[0.5,0.5,0.0]", null, 1);

        assertThat(rows).hasSize(1);
        Object[] row = rows.get(0);
        assertThat(row).hasSize(8);
        // col[0] = chunk id
        assertThat(row[0]).isNotNull();
        // col[1] = document id
        assertThat(row[1]).isNotNull();
        // col[2] = chunk_index
        assertThat(((Number) row[2]).intValue()).isEqualTo(0);
        // col[3] = chunk_text
        assertThat(row[3]).isEqualTo("Safe claim text");
        // col[5] = doc title
        assertThat(row[5]).isEqualTo("Column Test");
        // col[6] = doc type
        assertThat(row[6]).isEqualTo("APPROVED_CLAIM");
        // col[7] = score (cosine similarity)
        double score = ((Number) row[7]).doubleValue();
        assertThat(score).isGreaterThan(0.99);
    }
}
