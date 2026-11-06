package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.dto.vtm.seed.VtmPredatorTypeSeed;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import dev.ushki.livedndlist.repository.vtm.VtmPredatorTypeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmPredatorTypeSeederTest {

  @Mock
  private VtmPredatorTypeRepository predatorTypeRepository;

  @InjectMocks
  private VtmPredatorTypeSeeder seeder;

  private VtmPredatorTypeSeed alleycat() {
    return VtmPredatorTypeSeed.builder()
        .name("Alleycat")
        .grantedSpecialty("Brawl")
        .build();
  }

  @Test
  void seedCreatesMissingPredatorType() {
    when(predatorTypeRepository.findByName("Alleycat")).thenReturn(Optional.empty());

    seeder.seed(alleycat());

    ArgumentCaptor<VtmPredatorType> captor = ArgumentCaptor.forClass(VtmPredatorType.class);
    verify(predatorTypeRepository).save(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo("Alleycat");
    assertThat(captor.getValue().getGrantedSpecialty()).isEqualTo("Brawl");
  }

  @Test
  void seedUpdatesExistingPredatorTypeIdempotently() {
    VtmPredatorType existing = VtmPredatorType.builder().id(1L).name("Alleycat").build();
    when(predatorTypeRepository.findByName("Alleycat")).thenReturn(Optional.of(existing));

    seeder.seed(alleycat());
    seeder.seed(alleycat());

    verify(predatorTypeRepository, times(2)).save(any(VtmPredatorType.class));
  }
}