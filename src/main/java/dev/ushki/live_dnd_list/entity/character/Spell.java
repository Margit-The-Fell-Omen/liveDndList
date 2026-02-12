package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.SpellSchool;
import jakarta.persistence.*;

@Entity
public class Spell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private DndCharacter character;

    private String name;

    private Integer level;

    @Enumerated(EnumType.STRING)
    private SpellSchool school;

    private String castingTime;
    private String range;
    private String components; // V, S, m (materials)
    private String duration;
    private boolean concentration;
    private boolean ritual;

    private String description;

    private String higherLevels;

    //TODO: getters, setters, etc.
}
