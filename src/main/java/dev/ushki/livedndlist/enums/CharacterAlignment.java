package dev.ushki.livedndlist.enums;

/**
 * Enumeration of character alignments in D&D 5th Edition. Represents a character's moral and
 * ethical outlook on two axes: Law vs. Chaos (attitude toward order) and Good vs. Evil (moral
 * compass).
 *
 * <p>The alignment system is organized as a 3x3 grid:
 * <pre>
 *          Lawful      Neutral      Chaotic
 * Good       LG          NG           CG
 * Neutral    LN          TN           CN
 * Evil       LE          NE           CE
 * </pre>
 *
 * <p>Alignment affects roleplay choices and may interact with certain
 * spells, magic items, and class features (e.g., Paladins traditionally favor lawful good).
 */
public enum CharacterAlignment {
  LAWFUL_GOOD, NEUTRAL_GOOD, CHAOTIC_GOOD,
  LAWFUL_NEUTRAL, TRUE_NEUTRAL, CHAOTIC_NEUTRAL,
  LAWFUL_EVIL, NEUTRAL_EVIL, CHAOTIC_EVIL,
  UNALIGNED
}
