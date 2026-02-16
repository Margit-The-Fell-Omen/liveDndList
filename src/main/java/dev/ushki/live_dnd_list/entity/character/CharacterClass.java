package dev.ushki.live_dnd_list.entity.character;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "character_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String className;

    private String subClass;

    @Builder.Default
    private Integer level = 1;
}
