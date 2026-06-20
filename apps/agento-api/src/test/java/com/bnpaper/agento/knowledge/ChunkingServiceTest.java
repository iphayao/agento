package com.bnpaper.agento.knowledge;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkingServiceTest {

    private final ChunkingService service = new ChunkingService();

    @Test
    void nullOrBlankReturnsEmpty() {
        assertThat(service.chunk(null)).isEmpty();
        assertThat(service.chunk("")).isEmpty();
        assertThat(service.chunk("   ")).isEmpty();
    }

    @Test
    void shortTextReturnsSingleChunk() {
        String text = "SoClean is a soft, low-dust facial tissue.";
        List<String> chunks = service.chunk(text);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEqualTo(text);
    }

    @Test
    void longTextProducesMultipleChunks() {
        // Build a text clearly longer than 500 chars
        String paragraph = "SoClean facial tissue is 2-ply, 180 sheets, soft and low-dust. ";
        String text = paragraph.repeat(10); // ~640 chars
        List<String> chunks = service.chunk(text);
        assertThat(chunks.size()).isGreaterThan(1);
    }

    @Test
    void chunksOverlapByApproximatelyOverlapSize() {
        String word = "abcdefghij "; // 11 chars
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append(word);
        List<String> chunks = service.chunk(sb.toString(), 100, 20);
        assertThat(chunks.size()).isGreaterThan(1);
        // First chunk should end somewhere inside the overlap region of the second
        String first = chunks.get(0);
        String second = chunks.get(1);
        assertThat(second).contains(first.substring(Math.max(0, first.length() - 25)));
    }

    @Test
    void noChunkExceedsConfiguredMaxSize() {
        String text = "A".repeat(2000);
        List<String> chunks = service.chunk(text, 200, 30);
        for (String c : chunks) {
            assertThat(c.length()).isLessThanOrEqualTo(200 + 30);
        }
    }

    @Test
    void chunksOnParagraphBoundary() {
        String text = "First paragraph with some content about brand voice.\n\nSecond paragraph about product benefits.";
        List<String> chunks = service.chunk(text, 60, 10);
        assertThat(chunks.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void toVectorStringFormat() {
        float[] v = {0.1f, 0.2f, 0.3f};
        String result = KnowledgeDocumentService.toVectorString(v);
        assertThat(result).startsWith("[");
        assertThat(result).endsWith("]");
        assertThat(result).contains("0.1");
        assertThat(result).contains("0.2");
        assertThat(result).contains("0.3");
    }
}
