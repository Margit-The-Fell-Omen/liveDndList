package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.EquipmentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    private Integer quantity = 1;

    private Double weight;

    @Builder.Default
    private boolean equipped = false;

    @Builder.Default
    private boolean attuned = false;

    @Enumerated(EnumType.STRING)
    private EquipmentType type;

    private String damage;
    private String damageType;
    private String properties;
}
