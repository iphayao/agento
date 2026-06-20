package com.bnpaper.agento.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComplianceCheckerTest {

    private ComplianceChecker checker;

    @BeforeEach
    void setUp() {
        checker = new ComplianceChecker();
    }

    @Test
    void isSafe_returnsTrueForCleanContent() {
        String content = "SoClean ทิชชู่ 2 ชั้น เนียนนุ่ม ฝุ่นน้อย เหมาะกับการใช้งานทุกวัน คุ้มค่า";

        assertThat(checker.isSafe(content)).isTrue();
        assertThat(checker.findProhibitedTerms(content)).isEmpty();
    }

    @Test
    void detectsThaiProhibitedTerm_dustFree() {
        String content = "ทิชชู่ ไร้ฝุ่น 100% สำหรับออฟฟิศ";

        List<String> found = checker.findProhibitedTerms(content);

        assertThat(found).contains("ไร้ฝุ่น 100%");
        assertThat(checker.isSafe(content)).isFalse();
    }

    @Test
    void detectsThaiProhibitedTerm_cleanest() {
        String content = "ทิชชู่ สะอาดที่สุด ในตลาด";

        List<String> found = checker.findProhibitedTerms(content);

        assertThat(found).contains("สะอาดที่สุด");
    }

    @Test
    void detectsEnglishProhibitedTerm_dustFree() {
        String content = "SoClean — the dust-free tissue for your office";

        List<String> found = checker.findProhibitedTerms(content);

        assertThat(found).contains("dust-free");
        assertThat(checker.isSafe(content)).isFalse();
    }

    @Test
    void detectsEnglishProhibitedTerm_softest() {
        String content = "Experience the softest tissue in Thailand";

        List<String> found = checker.findProhibitedTerms(content);

        assertThat(found).contains("softest");
    }

    @Test
    void detectsEnglishProhibitedTerm_antibacterial() {
        String content = "antibacterial properties protect your family";

        List<String> found = checker.findProhibitedTerms(content);

        assertThat(found).contains("antibacterial");
    }

    @Test
    void detectsMultipleProhibitedTerms() {
        String content = "SoClean softest dust-free tissue — สะอาดที่สุด นุ่มที่สุด";

        List<String> found = checker.findProhibitedTerms(content);

        assertThat(found).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void isSafe_returnsTrueForNull() {
        assertThat(checker.isSafe(null)).isTrue();
        assertThat(checker.findProhibitedTerms(null)).isEmpty();
    }

    @Test
    void isSafe_returnsTrueForBlank() {
        assertThat(checker.isSafe("   ")).isTrue();
    }

    @Test
    void caseInsensitiveEnglishCheck() {
        String content = "The SOFTEST tissue you will ever use";

        List<String> found = checker.findProhibitedTerms(content);

        assertThat(found).contains("softest");
    }

    @Test
    void detectsThaiTermWhenInputIsNFDEncoded() {
        // สะอาดที่สุด decomposed into NFD (separate base + combining characters)
        String nfcTerm = "สะอาดที่สุด";
        String nfdContent = java.text.Normalizer.normalize(
                "ทิชชู่ " + nfcTerm + " ที่ดีที่สุด", java.text.Normalizer.Form.NFD);

        List<String> found = checker.findProhibitedTerms(nfdContent);

        assertThat(found).contains("สะอาดที่สุด");
        assertThat(checker.isSafe(nfdContent)).isFalse();
    }

    @Test
    void approvedTermsPassSafely() {
        String content = """
                SoClean ทิชชู่ เนียนนุ่ม ฝุ่นน้อย ให้สัมผัสสะอาด
                เหมาะกับการใช้งานทุกวัน คุ้มค่า 180 แผ่นต่อกล่อง
                low-dust tissue, soft and smooth, good value
                """;

        assertThat(checker.isSafe(content)).isTrue();
    }
}
