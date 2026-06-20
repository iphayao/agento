package com.bnpaper.agento.brand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandProfileController.class)
class BrandProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrandProfileService service;

    @Autowired
    private ObjectMapper objectMapper;

    private BrandProfileDto.Response sampleResponse() {
        return BrandProfileDto.Response.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .brandName("SoClean")
                .slogan("สะอาด เนียนนุ่ม ฝุ่นน้อย")
                .toneOfVoice("Warm, honest")
                .keyMessages(List.of("ฝุ่นน้อย", "เนียนนุ่ม"))
                .prohibitedClaims(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findAll_returns200WithList() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].brandName").value("SoClean"));
    }

    @Test
    void findAll_returnsEmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void findById_returns200WhenFound() throws Exception {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(service.findById(id)).thenReturn(sampleResponse());

        mockMvc.perform(get("/brands/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.brandName").value("SoClean"));
    }

    @Test
    void findById_returns404WhenNotFound() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(service.findById(missingId)).thenThrow(new ResourceNotFoundException("BrandProfile", missingId));

        mockMvc.perform(get("/brands/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_returns201WithCreatedBrand() throws Exception {
        BrandProfileDto.Request request = new BrandProfileDto.Request();
        request.setBrandName("SoClean");
        request.setSlogan("สะอาด เนียนนุ่ม ฝุ่นน้อย");
        request.setKeyMessages(List.of("ฝุ่นน้อย"));

        when(service.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.brandName").value("SoClean"));
    }

    @Test
    void create_returns400WhenBrandNameBlank() throws Exception {
        BrandProfileDto.Request request = new BrandProfileDto.Request();
        request.setBrandName(""); // invalid

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_returns400WhenBrandAlreadyExists() throws Exception {
        BrandProfileDto.Request request = new BrandProfileDto.Request();
        request.setBrandName("AnotherBrand");

        when(service.create(any())).thenThrow(
                new IllegalArgumentException("A brand profile already exists."));

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("A brand profile already exists."));
    }

    @Test
    void update_returns200() throws Exception {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        BrandProfileDto.Request request = new BrandProfileDto.Request();
        request.setBrandName("SoClean Updated");

        when(service.update(eq(id), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/brands/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_returns200() throws Exception {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/brands/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
