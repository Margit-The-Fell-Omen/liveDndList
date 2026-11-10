package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.vtm.response.VtmDisciplineResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPowerResponse;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmPower;
import dev.ushki.livedndlist.enums.DisciplineType;
import dev.ushki.livedndlist.mapper.vtm.VtmCatalogMapper;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmPowerRepository;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmDisciplineCatalogServiceTest {

  @Mock
  private VtmDisciplineRepository disciplineRepository;
  @Mock
  private VtmPowerRepository powerRepository;
  @Mock
  private VtmCatalogMapper catalogMapper;
  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private VtmDisciplineCatalogService service;

  @BeforeEach
  void setUp() {
    org.mockito.Mockito.lenient().when(cacheManager.get(any(), any(), any()))
        .thenAnswer(invocation -> {
          Supplier<List<?>> supplier = (Supplier<List<?>>) invocation.getArgument(2);
          return supplier.get();
        });
  }

  @Test
  void listDisciplinesWithoutTypeReturnsAll() {
    VtmDiscipline discipline = VtmDiscipline.builder().id(1L).name("Auspex").build();
    VtmDisciplineResponse response = VtmDisciplineResponse.builder().id(1L).name("Auspex").build();
    when(disciplineRepository.findAllByOrderByNameAsc()).thenReturn(List.of(discipline));
    when(catalogMapper.toDisciplineResponse(discipline)).thenReturn(response);

    List<VtmDisciplineResponse> result = service.listDisciplines(null);

    assertThat(result).containsExactly(response);
    verify(disciplineRepository).findAllByOrderByNameAsc();
  }

  @Test
  void listDisciplinesWithTypeFiltersByType() {
    VtmDiscipline discipline = VtmDiscipline.builder().id(1L).name("Potence").build();
    VtmDisciplineResponse response = VtmDisciplineResponse.builder().id(1L).name("Potence").build();
    when(disciplineRepository.findAllByDisciplineTypeOrderByNameAsc(DisciplineType.PHYSICAL))
        .thenReturn(List.of(discipline));
    when(catalogMapper.toDisciplineResponse(discipline)).thenReturn(response);

    List<VtmDisciplineResponse> result = service.listDisciplines(DisciplineType.PHYSICAL);

    assertThat(result).containsExactly(response);
    verify(disciplineRepository).findAllByDisciplineTypeOrderByNameAsc(DisciplineType.PHYSICAL);
  }

  @Test
  void listPowersReturnsEmptyForNullDisciplineId() {
    assertThat(service.listPowers(null, null)).isEmpty();
  }

  @Test
  void listPowersWithLevelFiltersByLevel() {
    VtmPower power = VtmPower.builder().id(1L).name("Mesmerize").level(2).build();
    VtmPowerResponse response = VtmPowerResponse.builder().id(1L).name("Mesmerize").build();
    when(powerRepository.findByDisciplineIdAndLevelLessThanEqualOrderByLevelAscNameAsc(4L, 2))
        .thenReturn(List.of(power));
    when(catalogMapper.toPowerResponse(power)).thenReturn(response);

    List<VtmPowerResponse> result = service.listPowers(4L, 2);

    assertThat(result).containsExactly(response);
  }

  @Test
  void listPowersWithoutLevelReturnsAllForDiscipline() {
    VtmPower power = VtmPower.builder().id(1L).name("Compel").level(1).build();
    VtmPowerResponse response = VtmPowerResponse.builder().id(1L).name("Compel").build();
    when(powerRepository.findByDisciplineIdOrderByLevelAscNameAsc(4L)).thenReturn(List.of(power));
    when(catalogMapper.toPowerResponse(power)).thenReturn(response);

    List<VtmPowerResponse> result = service.listPowers(4L, null);

    assertThat(result).containsExactly(response);
    verify(powerRepository).findByDisciplineIdOrderByLevelAscNameAsc(4L);
  }

  @Test
  void listPowersUsesCache() {
    when(powerRepository.findByDisciplineIdOrderByLevelAscNameAsc(4L))
        .thenReturn(List.of());

    service.listPowers(4L, null);

    verify(cacheManager).get(any(), any(CompositeKey.class), any());
  }
}