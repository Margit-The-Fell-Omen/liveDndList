package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.dto.vtm.request.ChronicleRequest;
import dev.ushki.livedndlist.dto.vtm.request.ChronicleStatusRequest;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleResponse;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleSummaryResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.vtm.VtmChronicleMapper;
import dev.ushki.livedndlist.repository.UserRepository;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterRepository;
import dev.ushki.livedndlist.repository.vtm.VtmChronicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtmChronicleService {

  private final VtmChronicleRepository chronicleRepository;
  private final VtmCharacterRepository characterRepository;
  private final UserRepository userRepository;
  private final VtmChronicleMapper chronicleMapper;
  private final VtmAccessControl access;

  @Transactional
  public ChronicleResponse create(ChronicleRequest request, String username) {
    User player = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

    VtmChronicle saved = chronicleRepository.save(chronicleMapper.toEntity(request, player));
    log.info("Created VtM chronicle {} for user {}", saved.getId(), username);
    return chronicleMapper.toResponse(saved, 0);
  }

  @Transactional(readOnly = true)
  public PageResponse<ChronicleSummaryResponse> getAllByPlayer(
      String username, Pageable pageable) {
    User player = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

    Page<VtmChronicle> page = chronicleRepository.findByPlayerId(player.getId(), pageable);
    Page<ChronicleSummaryResponse> mapped = page.map(chronicle ->
        chronicleMapper.toSummary(chronicle,
            characterRepository.countByChronicleId(chronicle.getId())));
    return PageResponse.of(mapped);
  }

  @Transactional(readOnly = true)
  public ChronicleResponse getById(Long id, String username) {
    VtmChronicle chronicle = access.ownedChronicle(id, username);
    return chronicleMapper.toResponse(chronicle,
        characterRepository.countByChronicleId(chronicle.getId()));
  }

  @Transactional
  public ChronicleResponse update(Long id, ChronicleRequest request, String username) {
    VtmChronicle chronicle = access.ownedChronicle(id, username);
    applyUpdates(chronicle, request);
    VtmChronicle saved = chronicleRepository.save(chronicle);
    return chronicleMapper.toResponse(saved,
        characterRepository.countByChronicleId(saved.getId()));
  }

  @Transactional
  public ChronicleResponse updateStatus(
      Long id, ChronicleStatusRequest request, String username) {
    VtmChronicle chronicle = access.ownedChronicle(id, username);
    chronicle.setStatus(request.getStatus());
    VtmChronicle saved = chronicleRepository.save(chronicle);
    return chronicleMapper.toResponse(saved,
        characterRepository.countByChronicleId(saved.getId()));
  }

  @Transactional
  public void delete(Long id, String username) {
    VtmChronicle chronicle = access.ownedChronicle(id, username);
    characterRepository.deleteAll(characterRepository.findByChronicleId(id));
    chronicleRepository.delete(chronicle);
    log.info("Deleted VtM chronicle {} for user {}", id, username);
  }

  private void applyUpdates(VtmChronicle chronicle, ChronicleRequest request) {
    if (request.getTitle() != null) {
      chronicle.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      chronicle.setDescription(request.getDescription());
    }
    if (request.getSetting() != null) {
      chronicle.setSetting(request.getSetting());
    }
    if (request.getStartDate() != null) {
      chronicle.setStartDate(request.getStartDate());
    }
    if (request.getStatus() != null) {
      chronicle.setStatus(request.getStatus());
    }
  }
}