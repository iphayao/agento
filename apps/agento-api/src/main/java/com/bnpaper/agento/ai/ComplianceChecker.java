package com.bnpaper.agento.ai;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Component
public class ComplianceChecker {

    private static final List<String> PROHIBITED_TERMS_TH = List.of(
            "ไร้ฝุ่น 100%",
            "ไม่มีฝุ่นเลย",
            "ปลอดฝุ่นสมบูรณ์แบบ",
            "antibacterial",
            "ฆ่าเชื้อโรค",
            "medically safe",
            "ปลอดภัยทางการแพทย์",
            "hypoallergenic",
            "dermatologist tested",
            "dermatologist approved",
            "ทดสอบโดยแพทย์",
            "สะอาดที่สุด",
            "นุ่มที่สุด",
            "ปลอดภัยที่สุด",
            "ดีที่สุด",
            "อันดับ 1",
            "ขายดีที่สุด"
    );

    private static final List<String> PROHIBITED_TERMS_EN = List.of(
            "dust-free",
            "zero dust",
            "100% dust",
            "antibacterial",
            "medically safe",
            "hypoallergenic",
            "dermatologist",
            "safest",
            "cleanest",
            "softest",
            "best tissue"
    );

    // Pre-normalize all Thai terms to NFC once at class-load time
    private static final List<String> PROHIBITED_TERMS_TH_NORMALIZED = PROHIBITED_TERMS_TH.stream()
            .map(t -> Normalizer.normalize(t, Normalizer.Form.NFC))
            .toList();

    public List<String> findProhibitedTerms(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        // Normalize input to NFC so Thai characters with different Unicode representations match
        String normalized = Normalizer.normalize(content, Normalizer.Form.NFC);
        String lower = normalized.toLowerCase();
        List<String> found = new ArrayList<>();

        for (int i = 0; i < PROHIBITED_TERMS_TH.size(); i++) {
            if (normalized.contains(PROHIBITED_TERMS_TH_NORMALIZED.get(i))) {
                found.add(PROHIBITED_TERMS_TH.get(i));
            }
        }
        for (String term : PROHIBITED_TERMS_EN) {
            if (lower.contains(term.toLowerCase())) {
                found.add(term);
            }
        }
        return found;
    }

    public boolean isSafe(String content) {
        return findProhibitedTerms(content).isEmpty();
    }
}
