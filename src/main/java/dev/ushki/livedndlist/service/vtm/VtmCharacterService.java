package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.dto.vtm.request.MortalMaskRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmCharacterRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmDisciplineAssignmentRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmDotsUpdateRequest;
import dev.ushki.livedndlist.dto.vtm.response.MortalMaskResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmCharacterResponse;
import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import dev.ushki.livedndlist.entity.vtm.VtmCharacterDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmCharacterDisciplineKey;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmMortalMask;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.vtm.VtmCharacterMapper;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterRepository;
import dev.ushki.livedndlist.repository.vtm.VtmClanRepository;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmMortalMaskRepository;
import dev.ushki.livedndlist.repository.vtm.VtmPredatorTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VtmCharacterService {

  private final VtmCharacterRepository characterRepository;
  private final VtmClanRepository clanRepository;
  private final VtmPredatorTypeRepository predatorTypeRepository;
  private final VtmDisciplineRepository disciplineRepository;
  private final VtmCharacterDisciplineRepository characterDisciplineRepository;
  private final VtmMortalMaskRepository maskRepository;
  private final VtmCharacterMapper characterMapper;
  private final VtmAccessControl access;

  @Transactional
  public VtmCharacterResponse create(
      Long chronicleId, VtmCharacterRequest request, String username) {
    VtmChronicle chronicle = access.ownedChronicle(chronicleId, username);
    VtmClan clan = clanRepository.findById(request.getClanId())
        .orElseThrow(() -> new ResourceNotFoundException("Clan", "id", request.getClanId()));
    VtmPredatorType predatorType = predatorTypeRepository.findById(request.getPredatorTypeId())
        .orElseThrow(
            () -> new ResourceNotFoundException("PredatorType", "id", request.getPredatorTypeId()));

    VtmCharacter saved = characterRepository.save(
        characterMapper.toEntity(request, chronicle, clan, predatorType));
    log.info("Created VtM character {} in chronicle {} for user {}", saved.getId(), chronicleId,
        username);
    return characterMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public PageResponse<VtmCharacterResponse> listByChronicle(
      Long chronicleId, String username, Pageable pageable) {
    access.ownedChronicle(chronicleId, username);
    Page<VtmCharacter> page = characterRepository.findByChronicleId(chronicleId, pageable);
    return PageResponse.of(page.map(characterMapper::toResponse));
  }

  @Transactional(readOnly = true)
  public VtmCharacterResponse getById(Long characterId, String username) {
    VtmCharacter character = access.ownedCharacter(characterId, username);
    return characterMapper.toResponse(character);
  }

  @Transactional
  public VtmCharacterResponse update(
      Long characterId, VtmCharacterRequest request, String username) {
    VtmCharacter character = access.ownedCharacter(characterId, username);

    if (request.getClanId() != null) {
      VtmClan clan = clanRepository.findById(request.getClanId())
          .orElseThrow(() -> new ResourceNotFoundException("Clan", "id", request.getClanId()));
      character.setClan(clan);
    }
    if (request.getPredatorTypeId() != null) {
      VtmPredatorType predatorType = predatorTypeRepository.findById(request.getPredatorTypeId())
          .orElseThrow(() -> new ResourceNotFoundException(
              "PredatorType", "id", request.getPredatorTypeId()));
      character.setPredatorType(predatorType);
    }
    if (request.getName() != null) {
      character.setName(request.getName());
    }
    if (request.getConcept() != null) {
      character.setConcept(request.getConcept());
    }
    if (request.getAmbition() != null) {
      character.setAmbition(request.getAmbition());
    }
    if (request.getDesire() != null) {
      character.setDesire(request.getDesire());
    }
    if (request.getGeneration() != null) {
      character.setGeneration(request.getGeneration());
    }
    if (request.getHumanity() != null) {
      character.setHumanity(request.getHumanity());
    }
    if (request.getBloodPotency() != null) {
      character.setBloodPotency(request.getBloodPotency());
    }

    VtmCharacter saved = characterRepository.save(character);
    return characterMapper.toResponse(saved);
  }

  @Transactional
  public void delete(Long characterId, String username) {
    access.ownedCharacter(characterId, username);
    maskRepository.findByCharacterId(characterId).ifPresent(maskRepository::delete);
    characterDisciplineRepository.deleteAll(
        characterDisciplineRepository.findByCharacterId(characterId));
    characterRepository.deleteById(characterId);
    log.info("Deleted VtM character {} for user {}", characterId, username);
  }

  @Transactional
  public VtmCharacterResponse assignDiscipline(
      Long characterId, VtmDisciplineAssignmentRequest request, String username) {
    access.ownedCharacter(characterId, username);
    VtmDiscipline discipline = disciplineRepository.findById(request.getDisciplineId())
        .orElseThrow(
            () -> new ResourceNotFoundException("Discipline", "id", request.getDisciplineId()));

    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(characterId, discipline.getId());
    VtmCharacterDiscipline row = characterDisciplineRepository.findById(key)
        .orElseGet(() -> VtmCharacterDiscipline.builder().id(key).build());
    row.setDots(request.getDots());
    characterDisciplineRepository.save(row);
    return characterMapper.toResponse(access.ownedCharacter(characterId, username));
  }

  @Transactional
  public VtmCharacterResponse updateDots(
      Long characterId, Long disciplineId, VtmDotsUpdateRequest request, String username) {
    access.ownedCharacter(characterId, username);

    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(characterId, disciplineId);
    VtmCharacterDiscipline row = characterDisciplineRepository.findById(key)
        .orElseThrow(() -> new ResourceNotFoundException("Character discipline", "key", key));
    row.setDots(request.getDots());
    characterDisciplineRepository.save(row);
    return characterMapper.toResponse(access.ownedCharacter(characterId, username));
  }

  @Transactional
  public void removeDiscipline(Long characterId, Long disciplineId, String username) {
    access.ownedCharacter(characterId, username);
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(characterId, disciplineId);
    characterDisciplineRepository.findById(key).ifPresent(characterDisciplineRepository::delete);
  }

  @Transactional(readOnly = true)
  public MortalMaskResponse getMask(Long characterId, String username) {
    access.ownedCharacter(characterId, username);
    return characterMapper.toMaskResponse(
        maskRepository.findByCharacterId(characterId).orElse(null));
  }

  @Transactional
  public MortalMaskResponse saveMask(
      Long characterId, MortalMaskRequest request, String username) {
    VtmCharacter character = access.ownedCharacter(characterId, username);

    VtmMortalMask mask = maskRepository.findByCharacterId(characterId)
        .orElseGet(() -> VtmMortalMask.builder().character(character).build());
    mask.setMortalName(request.getMortalName());
    mask.setOccupation(request.getOccupation());
    mask.setCoverStory(request.getCoverStory());
    mask.setDescription(request.getDescription());
    maskRepository.save(mask);
    return characterMapper.toMaskResponse(mask);
  }

  @Transactional
  public void deleteMask(Long characterId, String username) {
    access.ownedCharacter(characterId, username);
    maskRepository.findByCharacterId(characterId).ifPresent(maskRepository::delete);
  }
}