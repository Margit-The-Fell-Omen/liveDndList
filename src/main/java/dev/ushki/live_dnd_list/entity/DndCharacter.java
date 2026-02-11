package dev.ushki.live_dnd_list.entity;

import jakarta.persistence.*;

@Entity
public class DndCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    /* TODO: finish this class */
}
