package dev.ushki.livedndlist.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration. Contains all required fields for creating a new user account.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Username: 3-50 characters, must be unique</li>
 *   <li>Email: valid email format, must be unique</li>
 *   <li>Password: minimum 6 characters</li>
 * </ul>
 *
 * <p>Upon successful registration, the user will be assigned the default
 * {@code ROLE_USER} role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;
}
