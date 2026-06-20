package com.bnpaper.agento.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ComplianceChecker {

    private static final List<String> PROHIBITED_TH = List.of(
            "ไร้ฝุ่น 100%", "ไม่มีฝุ่นเลย", "ปลอดฝุ่นสมบูรณ์แบบ",
            "antibacterial", "ฆ่าเชื้อโรค", "medically safe", "ปลอดภัยทางการแพทย์",
            "hypoallergenic", "dermatologist", "ทดสอบโดยแพทย์",
            "สะอาดที่สุด", "นุ่มที่สุด", "ปลอดภัยที่สุด", "ดีที่สุด",
            "อันดับ 1", "ขายดีที่สุด"
    );

    private static final List<String> PROHIBITED_EN = List.of(
            "dust-free", "zero dust", "antibacterial",
            "medically safe", "hypoallergenic", "dermatologist",
            "safest", "cleanest", "softest", "best tissue", "100% dust"
    );

    public List<String> check(AiContentResponse response) {
        List<String> flags = new ArrayList<>();
        String combined = buildCombined(response);
        String combinedLower = combined.toLowerCase(Locale.ROOT);

        for (String term : PROHIBITED_TH) {
            if (combined.contains(term)) {
                flags.add("Prohibited term: " + term);
            }
        }
        for (String term : PROHIBITED_EN) {
            if (combinedLower.contains(term.toLowerCase(Locale.ROOT))) {
                flags.add("Prohibited term: " + term);
            }
        }
        return flags;
    }

    private String buildCombined(AiContentResponse r) {
        StringBuilder sb = new StringBuilder();
        if (r.getTitle() != null) sb.append(r.getTitle()).append(" ");
        if (r.getHook() != null) sb.append(r.getHook()).append(" ");
        if (r.getBody() != null) sb.append(r.getBody()).append(" ");
        if (r.getCallToAction() != null) sb.append(r.getCallToAction()).append(" ");
        if (r.getHashtags() != null) sb.append(r.getHashtags());
        return sb.toString();
    }
}
