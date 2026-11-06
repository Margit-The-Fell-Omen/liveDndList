package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.dto.vtm.seed.VtmDisciplineSeed;
import dev.ushki.livedndlist.dto.vtm.seed.VtmPowerSeed;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmPower;
import dev.ushki.livedndlist.enums.DisciplineType;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmPowerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmDisciplineSeederTest {

  @Mock
  private VtmDisciplineRepository disciplineRepository;
  @Mock
  private VtmPowerRepository powerRepository;

  @InjectMocks
  private VtmDisciplineSeeder seeder;

  private VtmDisciplineSeed potence() {
    return VtmDisciplineSeed.builder()
        .name("Potence")
        .type(DisciplineType.PHYSICAL)
        .powers(List.of(VtmPowerSeed.builder().name("Lethal Body").level(1).build()))
        .build();
  }

  @Test
  void seedCreatesDisciplineAndPower() {
    when(disciplineRepository.findByName("Potence")).thenReturn(Optional.empty());
    when(disciplineRepository.save(any(VtmDiscipline.class))).thenAnswer(invocation -> {
      VtmDiscipline discipline = invocation.getArgument(0);
      discipline.setId(2L);
      return discipline;
    });
    when(powerRepository.findByDisciplineIdAndName(eq(2L), eq("Lethal Body")))
        .thenReturn(Optional.empty());

    seeder.seed(potence());

    ArgumentCaptor<VtmDiscipline> disciplineCaptor =
        ArgumentCaptor.forClass(VtmDiscipline.class);
    verify(disciplineRepository).save(disciplineCaptor.capture());
    assertThat(disciplineCaptor.getValue().getDisciplineType())
        .isEqualTo(DisciplineType.PHYSICAL);

    verify(powerRepository).save(any(VtmPower.class));
  }

  @Test
  void seedUpdatesExistingPowerLevel() {
    VtmDiscipline existing = VtmDiscipline.builder().id(2L).name("Potence")
        .disciplineType(DisciplineType.PHYSICAL).build();
    when(disciplineRepository.findByName("Potence")).thenReturn(Optional.of(existing));
    VtmPower existingPower = VtmPower.builder()
        .discipline(existing).name("Lethal Body").level(1).build();
    when(powerRepository.findByDisciplineIdAndName(anyLong(), eq("Lethal Body")))
        .thenReturn(Optional.of(existingPower));

    seeder.seed(potence());

    ArgumentCaptor<VtmPower> powerCaptor = ArgumentCaptor.forClass(VtmPower.class);
    verify(powerRepository).save(powerCaptor.capture());
    assertThat(powerCaptor.getValue().getLevel()).isEqualTo(1);
  }
}