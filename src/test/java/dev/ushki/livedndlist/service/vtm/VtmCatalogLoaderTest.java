package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.dto.vtm.seed.VtmClanSeed;
import dev.ushki.livedndlist.dto.vtm.seed.VtmDisciplineSeed;
import dev.ushki.livedndlist.dto.vtm.seed.VtmPredatorTypeSeed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

class VtmCatalogLoaderTest {

  @Mock
  private VtmDisciplineSeeder disciplineSeeder;
  @Mock
  private VtmClanSeeder clanSeeder;
  @Mock
  private VtmPredatorTypeSeeder predatorTypeSeeder;
  @Mock
  private CacheManager cacheManager;

  private VtmCatalogLoader loader;
  private List<VtmClanSeed> seededClans;
  private List<VtmDisciplineSeed> seededDisciplines;
  private List<VtmPredatorTypeSeed> seededPredatorTypes;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    seededClans = new ArrayList<>();
    seededDisciplines = new ArrayList<>();
    seededPredatorTypes = new ArrayList<>();
    org.mockito.Mockito.doAnswer((Answer<Void>) invocation -> {
      seededDisciplines.add(invocation.getArgument(0));
      return null;
    }).when(disciplineSeeder).seed(any(VtmDisciplineSeed.class));
    org.mockito.Mockito.doAnswer((Answer<Void>) invocation -> {
      seededClans.add(invocation.getArgument(0));
      return null;
    }).when(clanSeeder).seed(any(VtmClanSeed.class));
    org.mockito.Mockito.doAnswer((Answer<Void>) invocation -> {
      seededPredatorTypes.add(invocation.getArgument(0));
      return null;
    }).when(predatorTypeSeeder).seed(any(VtmPredatorTypeSeed.class));

    loader = new VtmCatalogLoader(new ObjectMapper(), disciplineSeeder, clanSeeder,
        predatorTypeSeeder, cacheManager);
  }

  @Test
  void loadReadsAndSeedsAllBundledResources() {
    loader.load();

    verify(predatorTypeSeeder, times(6)).seed(any(VtmPredatorTypeSeed.class));
    verify(clanSeeder, times(7)).seed(any(VtmClanSeed.class));
    verify(disciplineSeeder, times(10)).seed(any(VtmDisciplineSeed.class));

    assertThat(seededPredatorTypes).hasSize(6);
    assertThat(seededClans).hasSize(7);
    assertThat(seededDisciplines).hasSize(10);
  }

  @Test
  void loadSeedsAssignedDisciplineNames() {
    loader.load();

    assertThat(seededDisciplines)
        .extracting(VtmDisciplineSeed::getName)
        .contains("Potence", "Auspex", "Dominate", "Obfuscate");
    assertThat(seededClans)
        .extracting(VtmClanSeed::getName)
        .contains("Brujah", "Tremere", "Ventrue", "Nosferatu");
    assertThat(seededPredatorTypes)
        .extracting(VtmPredatorTypeSeed::getName)
        .contains("Alleycat", "Farmer", "Cleaver", "Consensualist");
  }

  @Test
  void loadInvalidatesCatalogCache() {
    loader.load();

    verify(cacheManager).invalidate("VtmCatalog");
  }

  @Test
  void loadSeedsPowersWithinDisciplines() {
    loader.load();

    assertThat(seededDisciplines)
        .anyMatch(d -> d.getName().equals("Potence") && d.getPowers() != null
            && d.getPowers().stream().anyMatch(p -> "Lethal Body".equals(p.getName())));
  }
}