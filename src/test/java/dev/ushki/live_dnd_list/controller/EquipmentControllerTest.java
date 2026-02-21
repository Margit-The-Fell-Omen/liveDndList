package dev.ushki.live_dnd_list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.live_dnd_list.dto.request.EquipmentRequest;
import dev.ushki.live_dnd_list.dto.response.EquipmentResponse;
import dev.ushki.live_dnd_list.enums.EquipmentType;
import dev.ushki.live_dnd_list.security.jwt.JwtTokenProvider;
import dev.ushki.live_dnd_list.service.EquipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable security filters
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EquipmentService equipmentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private EquipmentResponse testEquipmentResponse;

    @BeforeEach
    void setUp() {
        testEquipmentResponse = EquipmentResponse.builder()
                .id(1L)
                .name("Longsword")
                .type(EquipmentType.WEAPON)
                .damage("1d8")
                .damageType("slashing")
                .quantity(1)
                .weight(3.0)
                .equipped(true)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/equipment")
    class GetAllEquipmentTests {

        @Test
        @DisplayName("Should return all equipment")
        void shouldReturnAllEquipment() throws Exception {
            when(equipmentService.getAll()).thenReturn(List.of(testEquipmentResponse));

            mockMvc.perform(get("/api/v1/equipment"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].name").value("Longsword"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/equipment/{id}")
    class GetEquipmentByIdTests {

        @Test
        @DisplayName("Should return equipment by ID")
        void shouldReturnEquipmentById() throws Exception {
            when(equipmentService.getById(1L)).thenReturn(testEquipmentResponse);

            mockMvc.perform(get("/api/v1/equipment/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Longsword"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/equipment/type/{type}")
    class GetEquipmentByTypeTests {

        @Test
        @DisplayName("Should return equipment by type")
        void shouldReturnEquipmentByType() throws Exception {
            when(equipmentService.getByType(EquipmentType.WEAPON)).thenReturn(List.of(testEquipmentResponse));

            mockMvc.perform(get("/api/v1/equipment/type/WEAPON"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].type").value("WEAPON"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/equipment/search")
    class SearchEquipmentTests {

        @Test
        @DisplayName("Should search equipment by name")
        void shouldSearchEquipmentByName() throws Exception {
            when(equipmentService.searchByName("sword")).thenReturn(List.of(testEquipmentResponse));

            mockMvc.perform(get("/api/v1/equipment/search").param("name", "sword"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].name").value("Longsword"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/equipment")
    class CreateEquipmentTests {

        @Test
        @DisplayName("Should create equipment successfully")
        void shouldCreateEquipmentSuccessfully() throws Exception {
            EquipmentRequest request = EquipmentRequest.builder()
                    .name("Shield")
                    .type(EquipmentType.SHIELD)
                    .quantity(1)
                    .weight(6.0)
                    .build();

            EquipmentResponse createdResponse = EquipmentResponse.builder()
                    .id(2L)
                    .name("Shield")
                    .type(EquipmentType.SHIELD)
                    .build();

            when(equipmentService.create(any(EquipmentRequest.class))).thenReturn(createdResponse);

            mockMvc.perform(post("/api/v1/equipment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Shield"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/equipment/{id}")
    class UpdateEquipmentTests {

        @Test
        @DisplayName("Should update equipment successfully")
        void shouldUpdateEquipmentSuccessfully() throws Exception {
            EquipmentRequest request = EquipmentRequest.builder()
                    .name("Longsword +1")
                    .type(EquipmentType.WEAPON)
                    .damage("1d8+1")
                    .build();

            when(equipmentService.update(anyLong(), any(EquipmentRequest.class)))
                    .thenReturn(testEquipmentResponse);

            mockMvc.perform(put("/api/v1/equipment/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/equipment/{id}")
    class DeleteEquipmentTests {

        @Test
        @DisplayName("Should delete equipment successfully")
        void shouldDeleteEquipmentSuccessfully() throws Exception {
            doNothing().when(equipmentService).delete(1L);

            mockMvc.perform(delete("/api/v1/equipment/1"))
                    .andExpect(status().isNoContent());
        }
    }
}
