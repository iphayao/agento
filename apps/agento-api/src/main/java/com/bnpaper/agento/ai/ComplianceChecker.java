package com.bnpaper.agento.ai;

import org.springframework.stereotype.Component;

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

    public List<String> findProhibitedTerms(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        String lower = content.toLowerCase();
        List<String> found = new ArrayList<>();

        for (String term : PROHIBITED_TERMS_TH) {
            if (content.contains(term)) {
                found.add(term);
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
