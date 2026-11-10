package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.vtm.response.VtmPredatorTypeResponse;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import dev.ushki.livedndlist.mapper.vtm.VtmCatalogMapper;
import dev.ushki.livedndlist.repository.vtm.VtmPredatorTypeRepository;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmPredatorTypeCatalogServiceTest {

  @Mock
  private VtmPredatorTypeRepository predatorTypeRepository;
  @Mock
  private VtmCatalogMapper catalogMapper;
  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private VtmPredatorTypeCatalogService service;

  @BeforeEach
  void setUp() {
    when(cacheManager.get(any(), any(), any())).thenAnswer(invocation -> {
      Supplier<VtmPredatorTypeResponse> supplier =
          (Supplier<VtmPredatorTypeResponse>) invocation.getArgument(2);
      return supplier.get();
    });
  }

  @Test
  void listPredatorTypesLoadsFromRepositoryInOrder() {
    VtmPredatorType type = VtmPredatorType.builder().id(1L).name("Alleycat").build();
    VtmPredatorTypeResponse response =
        VtmPredatorTypeResponse.builder().id(1L).name("Alleycat").build();
    when(predatorTypeRepository.findAllByOrderByNameAsc()).thenReturn(List.of(type));
    when(catalogMapper.toPredatorTypeResponse(type)).thenReturn(response);

    List<VtmPredatorTypeResponse> result = service.listPredatorTypes();

    assertThat(result).containsExactly(response);
    verify(cacheManager).get(any(), any(CompositeKey.class), any());
  }

  @Test
  void listPredatorTypesReturnsEmptyWhenNone() {
    when(predatorTypeRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

    List<VtmPredatorTypeResponse> result = service.listPredatorTypes();

    assertThat(result).isEmpty();
  }
}