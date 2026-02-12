package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.SkillType;
import jakarta.persistence.*;

@Entity
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private DndCharacter character;

    private SkillType skillType;

    private boolean proficiency;
    private boolean expertise;
    private Integer bonus = 0;

    //TODO: getters, setters, etc.
}
