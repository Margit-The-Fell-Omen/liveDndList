package dev.ushki.live_dnd_list.repository;

import dev.ushki.live_dnd_list.entity.User;
import dev.ushki.live_dnd_list.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .password("password123")
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void shouldReturnEmptyWhenUsernameNotFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        Optional<User> found = userRepository.findByEmail("test@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("Should return true if username exists")
    void shouldReturnTrueIfUsernameExists() {
        boolean exists = userRepository.existsByUsername("testuser");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false if username does not exist")
    void shouldReturnFalseIfUsernameDoesNotExist() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true if email exists")
    void shouldReturnTrueIfEmailExists() {
        boolean exists = userRepository.existsByEmail("test@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        User newUser = User.builder()
                .username("newuser")
                .email("new@test.com")
                .password("password")
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        userRepository.delete(testUser);
        entityManager.flush();

        Optional<User> found = userRepository.findByUsername("testuser");
        assertThat(found).isEmpty();
    }
}
