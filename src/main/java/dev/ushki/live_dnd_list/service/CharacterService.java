package dev.ushki.live_dnd_list.service;


import dev.ushki.live_dnd_list.dto.request.CharacterCreateRequest;
import dev.ushki.live_dnd_list.dto.request.CharacterUpdateRequest;
import dev.ushki.live_dnd_list.dto.request.EquipmentRequest;
import dev.ushki.live_dnd_list.dto.response.CharacterResponse;
import dev.ushki.live_dnd_list.dto.response.CharacterSummaryResponse;
import dev.ushki.live_dnd_list.entity.User;
import dev.ushki.live_dnd_list.entity.character.DndCharacter;
import dev.ushki.live_dnd_list.entity.character.Equipment;
import dev.ushki.live_dnd_list.entity.character.Spell;
import dev.ushki.live_dnd_list.exceptions.ResourceNotFoundException;
import dev.ushki.live_dnd_list.exceptions.UnauthorizedException;
import dev.ushki.live_dnd_list.mapper.CharacterMapper;
import dev.ushki.live_dnd_list.mapper.EquipmentMapper;
import dev.ushki.live_dnd_list.repository.CharacterRepository;
import dev.ushki.live_dnd_list.repository.SpellRepository;
import dev.ushki.live_dnd_list.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final SpellRepository spellRepository;
    private final CharacterMapper characterMapper;
    private final EquipmentMapper equipmentMapper;

    // CRUD operations
    @Transactional(readOnly = true)
    public List<CharacterSummaryResponse> getAllByUsername(String username) {
        User user = findUserByUsername(username);
        List<DndCharacter> characters = characterRepository.findAllByOwnerOrderByUpdatedAtDesc(user);
        return characterMapper.toSummaryResponseList(characters);
    }

    @Transactional(readOnly = true)
    public CharacterResponse getById(Long id, String username) {
        DndCharacter character = findCharacterWithOwnershipCheck(id, username);
        return characterMapper.toResponse(character);
    }

    public CharacterResponse create(CharacterCreateRequest request, String username) {
        User user = findUserByUsername(username);

        DndCharacter character = characterMapper.toEntity(request);
        character.setOwner(user);

        DndCharacter savedCharacter = characterRepository.save(character);
        log.info("Character '{}' created for user '{}'", savedCharacter.getName(), username);

        return characterMapper.toResponse(savedCharacter);
    }

    public CharacterResponse update(Long id, CharacterUpdateRequest request, String username) {
        DndCharacter character = findCharacterWithOwnershipCheck(id, username);

        characterMapper.updateEntity(character, request);

        DndCharacter savedCharacter = characterRepository.save(character);
        log.info("Character '{}' updated", savedCharacter.getName());

        return characterMapper.toResponse(savedCharacter);
    }

    public void delete(Long id, String username) {
        DndCharacter character = findCharacterWithOwnershipCheck(id, username);
        characterRepository.delete(character);
        log.info("Character '{}' deleted", character.getName());
    }

    // Equipment operations
    public CharacterResponse addEquipment(Long characterId, EquipmentRequest request, String username) {
        DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

        Equipment equipment = equipmentMapper.toEntity(request);
        character.addEquipment(equipment);

        DndCharacter savedCharacter = characterRepository.save(character);
        log.info("Equipment '{}' added to character '{}'", equipment.getName(), character.getName());

        return characterMapper.toResponse(savedCharacter);
    }

    public CharacterResponse removeEquipment(Long characterId, Long equipmentId, String username) {
        DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

        Equipment equipment = character.getEquipment().stream()
                .filter(e -> e.getId().equals(equipmentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        character.removeEquipment(equipment);

        DndCharacter savedCharacter = characterRepository.save(character);
        log.info("Equipment removed from character '{}'", character.getName());

        return characterMapper.toResponse(savedCharacter);
    }

    // Spell operations
    public CharacterResponse addSpell(Long characterId, Long spellId, String username) {
        DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

        Spell spell = spellRepository.findById(spellId)
                .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", spellId));

        character.addSpell(spell);

        DndCharacter savedCharacter = characterRepository.save(character);
        log.info("Spell '{}' added to character '{}'", spell.getName(), character.getName());

        return characterMapper.toResponse(savedCharacter);
    }

    public CharacterResponse removeSpell(Long characterId, Long spellId, String username) {
        DndCharacter character = findCharacterWithOwnershipCheck(characterId, username);

        Spell spell = spellRepository.findById(spellId)
                .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", spellId));

        character.removeSpell(spell);

        DndCharacter savedCharacter = characterRepository.save(character);
        log.info("Spell '{}' removed from character '{}'", spell.getName(), character.getName());

        return characterMapper.toResponse(savedCharacter);
    }

    // Helper methods
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private DndCharacter findCharacterWithOwnershipCheck(Long id, String username) {
        DndCharacter character = characterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Character", "id", id));

        if (!character.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedException("You don't have access to this character");
        }

        return character;
    }
}
