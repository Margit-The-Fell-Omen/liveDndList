package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.vtm.response.VtmClanResponse;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.mapper.vtm.VtmCatalogMapper;
import dev.ushki.livedndlist.repository.vtm.VtmClanRepository;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmClanCatalogServiceTest {

  @Mock
  private VtmClanRepository clanRepository;
  @Mock
  private VtmCatalogMapper catalogMapper;
  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private VtmClanCatalogService service;

  @BeforeEach
  void setUp() {
    when(cacheManager.get(any(), any(), any())).thenAnswer(invocation -> {
      Supplier<VtmClanResponse> supplier = (Supplier<VtmClanResponse>) invocation.getArgument(2);
      return supplier.get();
    });
  }

  @Test
  void listClansLoadsFromRepositoryInOrder() {
    VtmClan clan = VtmClan.builder().id(1L).name("Brujah").build();
    VtmClanResponse response = VtmClanResponse.builder().id(1L).name("Brujah").build();
    when(clanRepository.findAllByOrderByNameAsc()).thenReturn(List.of(clan));
    when(catalogMapper.toClanResponse(clan)).thenReturn(response);

    List<VtmClanResponse> result = service.listClans();

    assertThat(result).containsExactly(response);
    verify(cacheManager).get(eq("VtmCatalog"), any(CompositeKey.class), any());
  }

  @Test
  void listClansReturnsEmptyWhenNoClans() {
    when(clanRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

    List<VtmClanResponse> result = service.listClans();

    assertThat(result).isEmpty();
  }
}