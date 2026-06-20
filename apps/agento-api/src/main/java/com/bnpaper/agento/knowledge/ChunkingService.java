package com.bnpaper.agento.knowledge;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits document text into overlapping chunks for embedding.
 * Strategy: break at paragraph/sentence boundaries near the target size.
 */
@Service
public class ChunkingService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 50;

    public List<String> chunk(String text) {
        return chunk(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public List<String> chunk(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.strip().replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        if (normalized.length() <= chunkSize) {
            return List.of(normalized);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        int len = normalized.length();

        while (start < len) {
            int end = Math.min(start + chunkSize, len);
            if (end < len) {
                // Prefer to break at a paragraph boundary
                int paraBreak = normalized.lastIndexOf("\n\n", end);
                if (paraBreak > start + overlap) {
                    end = paraBreak;
                } else {
                    // Fall back to sentence boundary (period/question/exclamation + space)
                    int sentEnd = lastSentenceBreak(normalized, start + overlap, end);
                    if (sentEnd > start + overlap) {
                        end = sentEnd;
                    }
                    // Otherwise fall back to whitespace
                    else {
                        int wsBreak = normalized.lastIndexOf(' ', end);
                        if (wsBreak > start + overlap) {
                            end = wsBreak;
                        }
                    }
                }
            }

            String chunk = normalized.substring(start, end).strip();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // Advance with overlap: next start overlaps the current end
            start = Math.max(start + 1, end - overlap);
        }
        return chunks;
    }

    private int lastSentenceBreak(String text, int from, int to) {
        for (int i = Math.min(to, text.length() - 1); i >= from; i--) {
            char c = text.charAt(i);
            if ((c == '.' || c == '?' || c == '!' || c == '。' || c == '？' || c == '！')
                    && i + 1 < text.length() && text.charAt(i + 1) == ' ') {
                return i + 1;
            }
        }
        return -1;
    }
}
