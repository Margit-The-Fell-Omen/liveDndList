package dev.ushki.live_dnd_list.entity.character;

import jakarta.persistence.*;

@Entity
public class CharacterClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Why this depends of DndChar?
    @ManyToOne(fetch = FetchType.LAZY)
    private DndCharacter character;

    private String className;

    private String subClass;

    private Integer level = 1;
}
