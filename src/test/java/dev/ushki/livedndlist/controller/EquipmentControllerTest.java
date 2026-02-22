package dev.ushki.livedndlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import dev.ushki.livedndlist.service.EquipmentService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for EquipmentController.
 */
@WebMvcTest(EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private EquipmentService equipmentService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  private EquipmentResponse longswordResponse;
  private EquipmentResponse shieldResponse;
  private EquipmentResponse plateArmorResponse;

  @BeforeEach
  void setUp() {
    longswordResponse = EquipmentResponse.builder()
        .id(1L)
        .name("Longsword")
        .type(EquipmentType.WEAPON)
        .damage("1d8")
        .damageType("slashing")
        .quantity(1)
        .weight(3.0)
        .equipped(true)
        .build();

    shieldResponse = EquipmentResponse.builder()
        .id(2L)
        .name("Shield")
        .type(EquipmentType.SHIELD)
        .quantity(1)
        .weight(6.0)
        .equipped(true)
        .build();

    plateArmorResponse = EquipmentResponse.builder()
        .id(3L)
        .name("Plate Armor")
        .type(EquipmentType.ARMOR)
        .quantity(1)
        .weight(65.0)
        .equipped(false)
        .build();
  }

  @Nested
  @DisplayName("GET /api/v1/equipment")
  class GetAllEquipmentTests {

    @Test
    @DisplayName("Should return all equipment without filters")
    void shouldReturnAllEquipmentWithoutFilters() throws Exception {
      when(equipmentService.getAll(
          isNull(), isNull(), isNull(), eq("name"), eq("asc")))
          .thenReturn(List.of(longswordResponse, shieldResponse, plateArmorResponse));

      mockMvc.perform(get("/api/v1/equipment"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(3))
          .andExpect(jsonPath("$.data[0].name").value("Longsword"));
    }

    @Test
    @DisplayName("Should return equipment filtered by type")
    void shouldReturnEquipmentFilteredByType() throws Exception {
      when(equipmentService.getAll(
          eq(EquipmentType.WEAPON), isNull(), isNull(), eq("name"), eq("asc")))
          .thenReturn(List.of(longswordResponse));

      mockMvc.perform(get("/api/v1/equipment")
              .param("type", "WEAPON"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(1))
          .andExpect(jsonPath("$.data[0].type").value("WEAPON"));
    }

    @Test
    @DisplayName("Should return equipment filtered by minimum weight")
    void shouldReturnEquipmentFilteredByMinWeight() throws Exception {
      when(equipmentService.getAll(
          isNull(), eq(10.0), isNull(), eq("name"), eq("asc")))
          .thenReturn(List.of(plateArmorResponse));

      mockMvc.perform(get("/api/v1/equipment")
              .param("minWeight", "10.0"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Plate Armor"));
    }

    @Test
    @DisplayName("Should return equipment filtered by maximum weight")
    void shouldReturnEquipmentFilteredByMaxWeight() throws Exception {
      when(equipmentService.getAll(
          isNull(), isNull(), eq(10.0), eq("name"), eq("asc")))
          .thenReturn(List.of(longswordResponse, shieldResponse));

      mockMvc.perform(get("/api/v1/equipment")
              .param("maxWeight", "10.0"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("Should return equipment filtered by weight range")
    void shouldReturnEquipmentFilteredByWeightRange() throws Exception {
      when(equipmentService.getAll(
          isNull(), eq(5.0), eq(70.0), eq("name"), eq("asc")))
          .thenReturn(List.of(shieldResponse, plateArmorResponse));

      mockMvc.perform(get("/api/v1/equipment")
              .param("minWeight", "5.0")
              .param("maxWeight", "70.0"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("Should return equipment with custom sorting")
    void shouldReturnEquipmentWithCustomSorting() throws Exception {
      when(equipmentService.getAll(
          isNull(), isNull(), isNull(), eq("weight"), eq("desc")))
          .thenReturn(List.of(plateArmorResponse, shieldResponse, longswordResponse));

      mockMvc.perform(get("/api/v1/equipment")
              .param("sortBy", "weight")
              .param("sortDir", "desc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Plate Armor"));
    }

    @Test
    @DisplayName("Should return equipment with multiple filters")
    void shouldReturnEquipmentWithMultipleFilters() throws Exception {
      when(equipmentService.getAll(
          eq(EquipmentType.ARMOR), eq(50.0), eq(100.0), eq("weight"), eq("asc")))
          .thenReturn(List.of(plateArmorResponse));

      mockMvc.perform(get("/api/v1/equipment")
              .param("type", "ARMOR")
              .param("minWeight", "50.0")
              .param("maxWeight", "100.0")
              .param("sortBy", "weight")
              .param("sortDir", "asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(1))
          .andExpect(jsonPath("$.data[0].type").value("ARMOR"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/equipment/{id}")
  class GetEquipmentByIdTests {

    @Test
    @DisplayName("Should return equipment by ID")
    void shouldReturnEquipmentById() throws Exception {
      when(equipmentService.getById(1L)).thenReturn(longswordResponse);

      mockMvc.perform(get("/api/v1/equipment/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.name").value("Longsword"))
          .andExpect(jsonPath("$.data.type").value("WEAPON"))
          .andExpect(jsonPath("$.data.damage").value("1d8"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/equipment/search")
  class SearchEquipmentTests {

    @Test
    @DisplayName("Should search equipment by name")
    void shouldSearchEquipmentByName() throws Exception {
      when(equipmentService.searchByName(eq("sword"), isNull()))
          .thenReturn(List.of(longswordResponse));

      mockMvc.perform(get("/api/v1/equipment/search")
              .param("name", "sword"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Longsword"));
    }

    @Test
    @DisplayName("Should search equipment by name with type filter")
    void shouldSearchEquipmentByNameWithTypeFilter() throws Exception {
      when(equipmentService.searchByName(eq("plate"), eq(EquipmentType.ARMOR)))
          .thenReturn(List.of(plateArmorResponse));

      mockMvc.perform(get("/api/v1/equipment/search")
              .param("name", "plate")
              .param("type", "ARMOR"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Plate Armor"))
          .andExpect(jsonPath("$.data[0].type").value("ARMOR"));
    }

    @Test
    @DisplayName("Should return empty list when no equipment matches search")
    void shouldReturnEmptyListWhenNoMatch() throws Exception {
      when(equipmentService.searchByName(eq("nonexistent"), isNull()))
          .thenReturn(List.of());

      mockMvc.perform(get("/api/v1/equipment/search")
              .param("name", "nonexistent"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Should search equipment with partial name match")
    void shouldSearchEquipmentWithPartialMatch() throws Exception {
      when(equipmentService.searchByName(eq("ar"), isNull()))
          .thenReturn(List.of(plateArmorResponse));

      mockMvc.perform(get("/api/v1/equipment/search")
              .param("name", "ar"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data[0].name").value("Plate Armor"));
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

      when(equipmentService.create(any(EquipmentRequest.class))).thenReturn(shieldResponse);

      mockMvc.perform(post("/api/v1/equipment")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.name").value("Shield"))
          .andExpect(jsonPath("$.data.type").value("SHIELD"));
    }

    @Test
    @DisplayName("Should return 400 when name is blank")
    void shouldReturn400WhenNameBlank() throws Exception {
      EquipmentRequest request = EquipmentRequest.builder()
          .name("")
          .type(EquipmentType.WEAPON)
          .build();

      mockMvc.perform(post("/api/v1/equipment")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create weapon with damage properties")
    void shouldCreateWeaponWithDamageProperties() throws Exception {
      EquipmentRequest request = EquipmentRequest.builder()
          .name("Greatsword")
          .type(EquipmentType.WEAPON)
          .damage("2d6")
          .damageType("slashing")
          .properties("two-handed, heavy")
          .weight(6.0)
          .build();

      EquipmentResponse greatswordResponse = EquipmentResponse.builder()
          .id(4L)
          .name("Greatsword")
          .type(EquipmentType.WEAPON)
          .damage("2d6")
          .damageType("slashing")
          .properties("two-handed, heavy")
          .weight(6.0)
          .build();

      when(equipmentService.create(any(EquipmentRequest.class)))
          .thenReturn(greatswordResponse);

      mockMvc.perform(post("/api/v1/equipment")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.name").value("Greatsword"))
          .andExpect(jsonPath("$.data.damage").value("2d6"))
          .andExpect(jsonPath("$.data.damageType").value("slashing"));
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

      EquipmentResponse updatedResponse = EquipmentResponse.builder()
          .id(1L)
          .name("Longsword +1")
          .type(EquipmentType.WEAPON)
          .damage("1d8+1")
          .damageType("slashing")
          .build();

      when(equipmentService.update(anyLong(), any(EquipmentRequest.class)))
          .thenReturn(updatedResponse);

      mockMvc.perform(put("/api/v1/equipment/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.name").value("Longsword +1"))
          .andExpect(jsonPath("$.data.damage").value("1d8+1"));
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
