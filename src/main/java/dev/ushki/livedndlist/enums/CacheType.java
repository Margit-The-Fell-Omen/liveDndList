package dev.ushki.livedndlist.enums;

/**
 * Types of cached data for proper invalidation.
 */
public enum CacheType {

  // Character queries
  CHARACTER_LIST,           // GET /characters (paginated list)
  CHARACTER_SEARCH,         // GET /characters/search
  CHARACTER_RECENT,         // GET /characters/recent
  CHARACTER_DETAIL,         // GET /characters/{id}
  CHARACTER_SHEET,          // GET /characters/{id}/sheet
  CHARACTER_COMBAT,         // GET /characters/{id}/combat
  CHARACTER_SPELLCASTING,   // GET /characters/{id}/spellcasting
  CHARACTER_SKILLS,         // GET /characters/{id}/skills
  CHARACTER_INVENTORY,      // GET /characters/{id}/inventory
  CHARACTER_SAVING_THROWS,  // GET /characters/{id}/saving-throws
  CHARACTER_SUMMARY,        // GET /characters/{id}/summary
  CHARACTER_SPELLS,         // GET /characters/{id}/spells
  CHARACTER_ADVANCED_SEARCH,// GET /characters/search/advanced

  // Spell queries (public)
  SPELL_LIST,               // GET /spells
  SPELL_DETAIL,             // GET /spells/{id}
  SPELL_SEARCH,             // GET /spells/search

  // User queries
  USER_LIST,                // GET /users (admin)
  USER_DETAIL,              // GET /users/{id}
  USER_SEARCH               // GET /users/search
}
