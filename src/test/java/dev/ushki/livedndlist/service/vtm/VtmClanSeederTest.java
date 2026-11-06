package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.dto.vtm.seed.VtmClanSeed;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.repository.vtm.VtmClanRepository;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmClanSeederTest {

  @Mock
  private VtmClanRepository clanRepository;
  @Mock
  private VtmDisciplineRepository disciplineRepository;

  @InjectMocks
  private VtmClanSeeder seeder;

  private VtmClanSeed brujah() {
    return VtmClanSeed.builder()
        .name("Brujah")
        .curse("Prone to frenzy")
        .disciplines(List.of("Potence", "Celerity"))
        .build();
  }

  @Test
  void seedLinksRequestedDisciplinesByName() {
    VtmClan existingClan = VtmClan.builder().id(1L).name("Brujah").build();
    when(clanRepository.findByName("Brujah")).thenReturn(Optional.of(existingClan));
    when(disciplineRepository.findByName("Potence"))
        .thenReturn(Optional.of(VtmDiscipline.builder().id(2L).name("Potence").build()));
    when(disciplineRepository.findByName("Celerity"))
        .thenReturn(Optional.of(VtmDiscipline.builder().id(3L).name("Celerity").build()));

    seeder.seed(brujah());

    ArgumentCaptor<VtmClan> captor = ArgumentCaptor.forClass(VtmClan.class);
    verify(clanRepository).save(captor.capture());
    assertThat(captor.getValue().getDisciplines())
        .extracting(VtmDiscipline::getName)
        .containsExactly("Potence", "Celerity");
  }

  @Test
  void seedSkipsUnknownDisciplines() {
    VtmClan existingClan = VtmClan.builder().id(1L).name("Brujah").build();
    when(clanRepository.findByName("Brujah")).thenReturn(Optional.of(existingClan));
    when(disciplineRepository.findByName("Potence")).thenReturn(Optional.empty());

    seeder.seed(brujah());

    ArgumentCaptor<VtmClan> captor = ArgumentCaptor.forClass(VtmClan.class);
    verify(clanRepository).save(captor.capture());
    assertThat(captor.getValue().getDisciplines()).isEmpty();
  }

  @Test
  void seedIsIdempotentAndDoesNotDuplicateLinks() {
    VtmClan existingClan = VtmClan.builder().id(1L).name("Brujah").build();
    VtmDiscipline potence = VtmDiscipline.builder().id(2L).name("Potence").build();
    when(clanRepository.findByName("Brujah")).thenReturn(Optional.of(existingClan));
    when(disciplineRepository.findByName("Potence")).thenReturn(Optional.of(potence));
    when(disciplineRepository.findByName("Celerity")).thenReturn(Optional.empty());

    seeder.seed(brujah());
    seeder.seed(brujah());

    verify(clanRepository, times(2)).save(any(VtmClan.class));
    verify(clanRepository, never()).findAll();
    assertThat(existingClan.getDisciplines())
        .extracting(VtmDiscipline::getName)
        .containsExactly("Potence");
  }
}