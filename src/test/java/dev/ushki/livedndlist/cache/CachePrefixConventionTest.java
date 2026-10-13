package dev.ushki.livedndlist.cache;

import static org.assertj.core.api.Assertions.assertThat;

import dev.ushki.livedndlist.service.CharacterService;
import dev.ushki.livedndlist.service.EquipmentService;
import dev.ushki.livedndlist.service.SpellService;
import dev.ushki.livedndlist.service.UserService;
import dev.ushki.livedndlist.service.features.FeatureCatalogService;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that services share a single, consistent prefix naming convention so that
 * {@link CacheManager#invalidateByPrefix(String)} calls correctly target the intended entries
 * (and nothing more). If two services disagree on how the "User:<u>:Character:<id>" namespace
 * is spelled, one service's invalidation would silently miss the other's cached data.
 */
@DisplayName("Cache prefix naming convention")
class CachePrefixConventionTest {

  private static String constant(Class<?> clazz, String name) {
    try {
      Field field = clazz.getDeclaredField(name);
      field.setAccessible(true);
      return (String) field.get(null);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Missing cache namespace constant " + clazz.getSimpleName()
          + "." + name, e);
    }
  }

  @Test
  @DisplayName("All services use the same 'User:' per-user prefix")
  void userResourcePrefixIsUniform() {
    assertThat(constant(CharacterService.class, "USER_RESOURCE")).isEqualTo("User:");
    assertThat(constant(UserService.class, "USER_RESOURCE")).isEqualTo("User:");
    assertThat(constant(EquipmentService.class, "USER_RESOURCE")).isEqualTo("User:");
  }

  @Test
  @DisplayName("Character-id segment is consistent across Character and Equipment services")
  void characterIdSuffixIsConsistent() {
    assertThat(constant(CharacterService.class, "CHARACTER_ID_SUFFIX"))
        .isEqualTo(":Character:");
    assertThat(constant(EquipmentService.class, "CHARACTER_ID_SUFFIX"))
        .isEqualTo(":Character:");
  }

  @Test
  @DisplayName("Character and Equipment services build the identical single-character namespace")
  void crossServiceCharacterNamespaceAgreement() {
    String fromCharacter = constant(CharacterService.class, "USER_RESOURCE") + "alice"
        + constant(CharacterService.class, "CHARACTER_ID_SUFFIX") + "7";
    String fromEquipment = constant(EquipmentService.class, "USER_RESOURCE") + "alice"
        + constant(EquipmentService.class, "CHARACTER_ID_SUFFIX") + "7";

    assertThat(fromCharacter).isEqualTo(fromEquipment);
    assertThat(fromCharacter).isEqualTo("User:alice:Character:7");
  }

  @Test
  @DisplayName("'User:<username>' prefix covers both list and single-character namespaces")
  void userPrefixCoversAllPerUserNamespaces() {
    String user = constant(CharacterService.class, "USER_RESOURCE");
    String listSuffix = constant(CharacterService.class, "CHARACTERS_SUFFIX");
    String singleSuffix = constant(CharacterService.class, "CHARACTER_ID_SUFFIX");

    String prefix = user + "alice";
    String listNamespace = user + "alice" + listSuffix;
    String singleNamespace = user + "alice" + singleSuffix + "7";

    assertThat(listNamespace).startsWith(prefix + ":");
    assertThat(singleNamespace).startsWith(prefix + ":");
  }

  @Test
  @DisplayName("Global catalog namespaces are distinct (no collisions)")
  void globalNamespacesAreDistinct() {
    assertThat(constant(EquipmentService.class, "EQUIPMENT_STRING")).isEqualTo("Equipment");
    assertThat(constant(SpellService.class, "SPELL_STRING")).isEqualTo("Spells");
    assertThat(constant(UserService.class, "USERS_NAMESPACE")).isEqualTo("Users");
    assertThat(constant(FeatureCatalogService.class, "CACHE_NAMESPACE")).isEqualTo("FeatureCatalog");

    assertThat(java.util.Set.of("Equipment", "Spells", "Users", "FeatureCatalog")).hasSize(4);
  }
}