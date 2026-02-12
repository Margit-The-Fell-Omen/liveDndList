package dev.ushki.live_dnd_list.entity.character;

import dev.ushki.live_dnd_list.enums.EquipmentType;
import jakarta.persistence.*;

@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private DndCharacter character;

    private String name;

    private String description;
    private Integer quantity = 1;
    private Double weight;
    private boolean equipped = false;
    private boolean attuned = false;

    @Enumerated(EnumType.STRING)
    private EquipmentType type;

    private String damage;
    private String damageType;
    private String properties;

    //TODO: getters, setters, etc.
}
