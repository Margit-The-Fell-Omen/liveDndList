package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.SpellSchool;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spells")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Spell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer level;

    @Enumerated(EnumType.STRING)
    private SpellSchool school;

    private String castingTime;

    private String range;

    private String components; // V, S, M (materials)

    private String duration;

    @Builder.Default
    private boolean concentration = false;

    @Builder.Default
    private boolean ritual = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String higherLevels;
}
