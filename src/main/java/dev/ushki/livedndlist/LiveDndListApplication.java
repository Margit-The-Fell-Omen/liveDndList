package dev.ushki.livedndlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Live D&D List application. A Spring Boot application for managing D&D
 * 5th Edition characters.
 *
 * <p>This application provides:
 * <ul>
 *   <li>Character creation and management</li>
 *   <li>Equipment and spell libraries</li>
 *   <li>User authentication with JWT tokens</li>
 *   <li>RESTful API for character sheet operations</li>
 * </ul>
 *
 * <p>Key features:
 * <ul>
 *   <li>Full character sheet tracking (stats, skills, spells, equipment)</li>
 *   <li>Multiclassing support</li>
 *   <li>Searchable spell and equipment libraries</li>
 *   <li>User-owned character management</li>
 *   <li>JWT-based stateless authentication</li>
 * </ul>
 *
 * <p>Technology stack:
 * <ul>
 *   <li>Spring Boot 3.x</li>
 *   <li>Spring Security with JWT</li>
 *   <li>Spring Data JPA (Hibernate)</li>
 *   <li>H2 Database (development) / PostgreSQL (production)</li>
 *   <li>Lombok for boilerplate reduction</li>
 * </ul>
 *
 * <p>API base path: {@code /api/v1}
 *
 * @see dev.ushki.livedndlist.controller
 * @see dev.ushki.livedndlist.service
 */
@SpringBootApplication
public class LiveDndListApplication {

  /**
   * Application entry point. Bootstraps the Spring Boot application and starts the embedded web
   * server.
   *
   * @param args command-line arguments (not used)
   */
  public static void main(String[] args) {
    SpringApplication.run(LiveDndListApplication.class, args);
  }
}
