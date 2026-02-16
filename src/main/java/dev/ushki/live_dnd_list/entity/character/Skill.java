package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.SkillType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private DndCharacter character;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType skillType;

    @Builder.Default
    private boolean proficiency = false;

    @Builder.Default
    private boolean expertise = false;

    private Integer bonus = 0;
}
