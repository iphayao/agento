package com.bnpaper.agento.performance;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentPerformanceService {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ContentPerformanceRepository repo;

    public List<ContentPerformanceDto.Response> findAll() {
        return repo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponseWithComputed)
                .toList();
    }

    public ContentPerformanceDto.Response findById(UUID id) {
        return toResponseWithComputed(requirePerformance(id));
    }

    public List<ContentPerformanceDto.Response> findByContentId(UUID contentId) {
        return repo.findByGeneratedContentId(contentId).stream()
                .map(this::toResponseWithComputed)
                .toList();
    }

    @Transactional
    public ContentPerformanceDto.Response create(ContentPerformanceDto.Request request) {
        ContentPerformance p = buildFromRequest(new ContentPerformance(), request);
        computeMetrics(p);
        p = repo.save(p);
        log.info("Created ContentPerformance id={} channel={}", p.getId(), p.getChannel());
        return toResponseWithComputed(p);
    }

    @Transactional
    public ContentPerformanceDto.Response update(UUID id, ContentPerformanceDto.Request request) {
        ContentPerformance p = requirePerformance(id);
        buildFromRequest(p, request);
        computeMetrics(p);
        p = repo.save(p);
        return toResponseWithComputed(p);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("ContentPerformance", id);
        }
        repo.deleteById(id);
    }

    /** Import CSV rows. Returns count of rows imported. */
    @Transactional
    public int importCsv(MultipartFile file) throws Exception {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) return 0;

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isBlank()) continue;
                try {
                    ContentPerformance p = parseCsvRow(line);
                    computeMetrics(p);
                    repo.save(p);
                    count++;
                } catch (Exception e) {
                    log.warn("CSV import: skipping line {} — {}", lineNum, e.getMessage());
                }
            }
        }
        log.info("CSV import complete: {} rows imported", count);
        return count;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private ContentPerformance buildFromRequest(ContentPerformance p, ContentPerformanceDto.Request r) {
        p.setGeneratedContentId(r.getGeneratedContentId());
        p.setChannel(r.getChannel());
        p.setPublishedAt(r.getPublishedAt());
        p.setImpressions(r.getImpressions());
        p.setViews(r.getViews());
        p.setClicks(r.getClicks());
        p.setLikes(r.getLikes());
        p.setComments(r.getComments());
        p.setShares(r.getShares());
        p.setOrders(r.getOrders());
        p.setRevenue(nvl(r.getRevenue()));
        p.setCost(nvl(r.getCost()));
        p.setNotes(r.getNotes());
        return p;
    }

    void computeMetrics(ContentPerformance p) {
        long reach = p.getImpressions() > 0 ? p.getImpressions() : p.getViews();

        // engagementRate = (likes + comments + shares + clicks) / impressions
        if (reach > 0) {
            long engagements = p.getLikes() + p.getComments() + p.getShares() + p.getClicks();
            p.setEngagementRate(BigDecimal.valueOf(engagements)
                    .divide(BigDecimal.valueOf(reach), 6, RoundingMode.HALF_UP));
        } else {
            p.setEngagementRate(BigDecimal.ZERO);
        }

        // conversionRate = orders / clicks
        if (p.getClicks() > 0) {
            p.setConversionRate(BigDecimal.valueOf(p.getOrders())
                    .divide(BigDecimal.valueOf(p.getClicks()), 6, RoundingMode.HALF_UP));
        } else {
            p.setConversionRate(BigDecimal.ZERO);
        }

        // roas = revenue / cost
        BigDecimal cost = p.getCost();
        if (cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            p.setRoas(nvl(p.getRevenue()).divide(cost, 4, RoundingMode.HALF_UP));
        } else {
            p.setRoas(null);
        }
    }

    private ContentPerformance parseCsvRow(String line) {
        String[] cols = line.split(",", -1);
        // Expected CSV columns (0-indexed):
        // 0:generatedContentId 1:channel 2:publishedAt 3:impressions 4:views
        // 5:clicks 6:likes 7:comments 8:shares 9:orders 10:revenue 11:cost 12:notes
        if (cols.length < 11) {
            throw new IllegalArgumentException("Expected at least 11 columns, got " + cols.length);
        }
        ContentPerformance p = new ContentPerformance();
        p.setGeneratedContentId(UUID.fromString(cols[0].trim()));
        p.setChannel(cols[1].trim());
        p.setPublishedAt(parseDateTime(cols[2].trim()));
        p.setImpressions(parseLong(cols[3]));
        p.setViews(parseLong(cols[4]));
        p.setClicks(parseLong(cols[5]));
        p.setLikes(parseLong(cols[6]));
        p.setComments(parseLong(cols[7]));
        p.setShares(parseLong(cols[8]));
        p.setOrders(parseLong(cols[9]));
        p.setRevenue(parseBigDecimal(cols[10]));
        p.setCost(cols.length > 11 ? parseBigDecimal(cols[11]) : BigDecimal.ZERO);
        p.setNotes(cols.length > 12 ? cols[12].trim() : null);
        return p;
    }

    private ContentPerformanceDto.Response toResponseWithComputed(ContentPerformance p) {
        return ContentPerformanceDto.toResponse(p);
    }

    private ContentPerformance requirePerformance(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentPerformance", id));
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static long parseLong(String s) {
        String t = s == null ? "" : s.trim();
        return t.isBlank() ? 0L : Long.parseLong(t);
    }

    private static BigDecimal parseBigDecimal(String s) {
        String t = s == null ? "" : s.trim();
        return t.isBlank() ? BigDecimal.ZERO : new BigDecimal(t);
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s, ISO_FMT);
        } catch (DateTimeParseException e) {
            // Try date-only: 2024-01-15 → midnight
            try {
                return LocalDateTime.parse(s + "T00:00:00", ISO_FMT);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
}
