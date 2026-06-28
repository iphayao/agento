package com.bnpaper.agento.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.security.SecurityConfig;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KnowledgeController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class KnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KnowledgeDocumentService service;
    @MockBean
    private com.bnpaper.agento.audit.AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private KnowledgeDocumentDto.Response sampleDoc() {
        return KnowledgeDocumentDto.Response.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .title("Brand Voice Guide")
                .type(DocumentType.BRAND_GUIDELINE)
                .content("SoClean brand voice: warm, honest.")
                .status(DocumentStatus.ACTIVE)
                .chunkCount(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findAll_returns200() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleDoc()));

        mockMvc.perform(get("/knowledge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Brand Voice Guide"))
                .andExpect(jsonPath("$.data[0].chunkCount").value(3));
    }

    @Test
    void findById_returns200() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        when(service.findById(id)).thenReturn(sampleDoc());

        mockMvc.perform(get("/knowledge/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("BRAND_GUIDELINE"));
    }

    @Test
    void findById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenThrow(new ResourceNotFoundException("KnowledgeDocument", id));

        mockMvc.perform(get("/knowledge/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void findChunks_returns200() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        KnowledgeDocumentDto.ChunkResponse chunk = KnowledgeDocumentDto.ChunkResponse.builder()
                .id(UUID.randomUUID())
                .documentId(id)
                .chunkIndex(0)
                .chunkText("SoClean brand voice: warm.")
                .hasEmbedding(true)
                .createdAt(LocalDateTime.now())
                .build();
        when(service.findChunks(id)).thenReturn(List.of(chunk));

        mockMvc.perform(get("/knowledge/{id}/chunks", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].chunkIndex").value(0))
                .andExpect(jsonPath("$.data[0].hasEmbedding").value(true));
    }

    @Test
    void create_returns201() throws Exception {
        KnowledgeDocumentDto.Request request = new KnowledgeDocumentDto.Request();
        request.setTitle("Brand Voice Guide");
        request.setType(DocumentType.BRAND_GUIDELINE);
        request.setContent("SoClean brand voice: warm, honest, approachable for everyday use.");

        when(service.create(any())).thenReturn(sampleDoc());

        mockMvc.perform(post("/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Brand Voice Guide"));
    }

    @Test
    void create_returns400WhenTitleBlank() throws Exception {
        KnowledgeDocumentDto.Request request = new KnowledgeDocumentDto.Request();
        request.setTitle("");
        request.setType(DocumentType.BRAND_GUIDELINE);
        request.setContent("Some content");

        mockMvc.perform(post("/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_returns400WhenContentBlank() throws Exception {
        KnowledgeDocumentDto.Request request = new KnowledgeDocumentDto.Request();
        request.setTitle("Valid Title");
        request.setType(DocumentType.APPROVED_CLAIM);
        request.setContent("");

        mockMvc.perform(post("/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenTypeNull() throws Exception {
        KnowledgeDocumentDto.Request request = new KnowledgeDocumentDto.Request();
        request.setTitle("Valid Title");
        request.setContent("Some content");
        // type is null

        mockMvc.perform(post("/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns200() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        KnowledgeDocumentDto.Request request = new KnowledgeDocumentDto.Request();
        request.setTitle("Updated Guide");
        request.setType(DocumentType.BRAND_GUIDELINE);
        request.setContent("Updated content about SoClean brand voice.");

        when(service.update(eq(id), any())).thenReturn(sampleDoc());

        mockMvc.perform(put("/knowledge/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void archive_returns200() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        doNothing().when(service).archive(id);

        mockMvc.perform(put("/knowledge/{id}/archive", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_returns200() throws Exception {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/knowledge/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void search_returns200() throws Exception {
        KnowledgeSearchRequest req = new KnowledgeSearchRequest();
        req.setQuery("brand voice tone");
        req.setTopK(3);

        KnowledgeSearchResult result = KnowledgeSearchResult.builder()
                .query("brand voice tone")
                .results(List.of(
                        KnowledgeSearchResult.ChunkMatch.builder()
                                .chunkId(UUID.randomUUID())
                                .documentId(UUID.randomUUID())
                                .documentTitle("Brand Voice Guide")
                                .documentType("BRAND_GUIDELINE")
                                .chunkText("SoClean brand voice: warm, honest.")
                                .score(0.92)
                                .chunkIndex(0)
                                .build()
                ))
                .build();

        when(service.search(any())).thenReturn(result);

        mockMvc.perform(post("/knowledge/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.query").value("brand voice tone"))
                .andExpect(jsonPath("$.data.results[0].score").value(0.92))
                .andExpect(jsonPath("$.data.results[0].documentType").value("BRAND_GUIDELINE"));
    }

    @Test
    void search_returns400WhenQueryBlank() throws Exception {
        KnowledgeSearchRequest req = new KnowledgeSearchRequest();
        req.setQuery("");

        mockMvc.perform(post("/knowledge/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
