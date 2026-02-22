package dev.ushki.livedndlist.enums;

/**
 * Enumeration of user roles in the application. Determines access levels and permissions for
 * different features.
 *
 * <p>Roles follow Spring Security naming convention with "ROLE_" prefix.
 *
 * <p>Role hierarchy:
 * <ul>
 *   <li>{@code ROLE_USER} - Standard user permissions</li>
 *   <li>{@code ROLE_ADMIN} - Full administrative access</li>
 * </ul>
 */
public enum Role {
  ROLE_USER,
  ROLE_ADMIN
}
