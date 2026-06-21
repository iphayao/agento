package com.bnpaper.agento.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bnpaper.agento.common.exception.AiProviderException;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.security.SecurityConfig;
import com.bnpaper.agento.security.PasswordEncoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeneratedContentController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
@WithMockUser(roles = "ADMIN")
class GeneratedContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeneratedContentService service;
    @MockBean
    private com.bnpaper.agento.audit.AuditService auditService;
    @MockBean
    private com.bnpaper.agento.common.ratelimit.AiRateLimiter rateLimiter;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID campaignId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID contentId  = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private GeneratedContentDto.Response sampleDraft() {
        return GeneratedContentDto.Response.builder()
                .id(contentId)
                .campaignId(campaignId)
                .contentType("TIKTOK_CAPTION")
                .channel("tiktok")
                .title("SoClean Caption")
                .body("เนียนนุ่ม ฝุ่นน้อย ใช้ได้ทุกวัน")
                .hook("ทิชชู่ดีๆ ที่คุณต้องลอง")
                .callToAction("สั่งซื้อเลย")
                .hashtags(List.of("#SoClean", "#ทิชชู่SoClean"))
                .status(ContentStatus.DRAFT)
                .aiModel("gpt-4o-mini")
                .promptVersion("v1")
                .complianceNotes("Claim-safe — no prohibited terms detected")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generate_returns201WithDraftContent() throws Exception {
        GeneratedContentDto.GenerateRequest request = new GeneratedContentDto.GenerateRequest();
        request.setContentType("TIKTOK_CAPTION");

        when(service.generate(eq(campaignId), any())).thenReturn(sampleDraft());

        mockMvc.perform(post("/campaigns/{campaignId}/content/generate", campaignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.contentType").value("TIKTOK_CAPTION"));
    }

    @Test
    void generate_returns400WhenContentTypeBlank() throws Exception {
        GeneratedContentDto.GenerateRequest request = new GeneratedContentDto.GenerateRequest();
        request.setContentType(""); // invalid

        mockMvc.perform(post("/campaigns/{campaignId}/content/generate", campaignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void generate_returns500WhenAiProviderFails() throws Exception {
        GeneratedContentDto.GenerateRequest request = new GeneratedContentDto.GenerateRequest();
        request.setContentType("TIKTOK_CAPTION");

        when(service.generate(eq(campaignId), any()))
                .thenThrow(new AiProviderException("AI API call failed: timeout"));

        mockMvc.perform(post("/campaigns/{campaignId}/content/generate", campaignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("AI generation failed. Please try again."));
    }

    @Test
    void findAll_returns200WithList() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleDraft()));

        mockMvc.perform(get("/content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].contentType").value("TIKTOK_CAPTION"));
    }

    @Test
    void findById_returns404WhenNotFound() throws Exception {
        UUID missing = UUID.randomUUID();
        when(service.findById(missing)).thenThrow(new ResourceNotFoundException("GeneratedContent", missing));

        mockMvc.perform(get("/content/{id}", missing))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void approve_returns200WithApprovedContent() throws Exception {
        GeneratedContentDto.Response approved = GeneratedContentDto.Response.builder()
                .id(contentId)
                .campaignId(campaignId)
                .status(ContentStatus.APPROVED)
                .hashtags(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(service.approve(contentId)).thenReturn(approved);

        mockMvc.perform(put("/content/{id}/approve", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void reject_returns200WithRejectedContent() throws Exception {
        GeneratedContentDto.Response rejected = GeneratedContentDto.Response.builder()
                .id(contentId)
                .campaignId(campaignId)
                .status(ContentStatus.REJECTED)
                .hashtags(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(service.reject(contentId)).thenReturn(rejected);

        mockMvc.perform(put("/content/{id}/reject", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void listByCampaign_returns200() throws Exception {
        when(service.findByCampaignId(campaignId)).thenReturn(List.of(sampleDraft()));

        mockMvc.perform(get("/campaigns/{campaignId}/content", campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
