package dev.ushki.livedndlist.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user account information. All fields are optional - only provided fields
 * will be updated.
 *
 * <p>This request supports partial updates. Fields set to {@code null}
 * will not modify the existing user data.
 *
 * <p>Validation rules (when provided):
 * <ul>
 *   <li>Username: 3-50 characters, must remain unique</li>
 *   <li>Email: valid email format, must remain unique</li>
 * </ul>
 *
 * <p>Note: Password updates should be handled through a separate
 * dedicated endpoint for security reasons.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @Email(message = "Invalid email format")
  private String email;
}
