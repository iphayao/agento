package com.bnpaper.agento.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComplianceCheckerTest {

    private final ComplianceChecker checker = new ComplianceChecker();

    @Test
    void check_noFlagsForCleanContent() {
        AiContentResponse response = AiContentResponse.builder()
                .title("เนียนนุ่ม ฝุ่นน้อย เหมาะทุกวัน")
                .body("SoClean ทิชชู่ 2ชั้น 180แผ่น ฝุ่นน้อย สัมผัสนุ่ม เหมาะสำหรับบ้านและออฟฟิศ")
                .callToAction("สั่งได้เลยที่ TikTok Shop")
                .hashtags("#SoClean #ทิชชู่นุ่ม #ฝุ่นน้อย")
                .build();

        List<String> flags = checker.check(response);
        assertThat(flags).isEmpty();
    }

    @Test
    void check_flagsThaiProhibitedTerms() {
        AiContentResponse response = AiContentResponse.builder()
                .body("ทิชชู่ที่สะอาดที่สุด นุ่มที่สุด ในตลาด")
                .build();

        List<String> flags = checker.check(response);
        assertThat(flags).isNotEmpty();
        assertThat(flags.stream().anyMatch(f -> f.contains("สะอาดที่สุด"))).isTrue();
        assertThat(flags.stream().anyMatch(f -> f.contains("นุ่มที่สุด"))).isTrue();
    }

    @Test
    void check_flagsEnglishProhibitedTerms() {
        AiContentResponse response = AiContentResponse.builder()
                .body("100% dust-free tissue, the softest and cleanest in Thailand")
                .build();

        List<String> flags = checker.check(response);
        assertThat(flags).isNotEmpty();
        assertThat(flags.stream().anyMatch(f -> f.contains("dust-free"))).isTrue();
        assertThat(flags.stream().anyMatch(f -> f.contains("softest"))).isTrue();
    }

    @Test
    void check_flagsHypoallergenicClaim() {
        AiContentResponse response = AiContentResponse.builder()
                .body("hypoallergenic tissue safe for sensitive skin")
                .build();

        List<String> flags = checker.check(response);
        assertThat(flags.stream().anyMatch(f -> f.contains("hypoallergenic"))).isTrue();
    }

    @Test
    void check_scansTitleHookAndHashtags() {
        AiContentResponse response = AiContentResponse.builder()
                .title("อันดับ 1 ทิชชู่ไทย")
                .hook("ปลอดภัยที่สุด")
                .body("เนียนนุ่ม ฝุ่นน้อย")
                .hashtags("#dust-free #SoClean")
                .build();

        List<String> flags = checker.check(response);
        assertThat(flags).hasSize(3);
    }
}
