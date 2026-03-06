package dev.ushki.livedndlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LiveDndListApplication {

  public static void main(String[] args) {
    SpringApplication.run(LiveDndListApplication.class, args);
  }
}
