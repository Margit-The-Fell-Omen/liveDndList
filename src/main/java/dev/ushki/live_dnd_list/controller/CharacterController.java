package dev.ushki.live_dnd_list.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
@Tag(name = "Characters", description = "D&D Character Sheet Management API")
public class CharacterController {

    private final CharacterService characterService;
    //TODO: finish implementing
}
