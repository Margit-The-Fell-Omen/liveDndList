package dev.ushki.livedndlist.service.sync;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SyncResult {

  private int created = 0;
  private int updated = 0;
  private int failed = 0;
  private final List<String> errors = new ArrayList<>();

  public void recordCreated() {
    created++;
  }

  public void recordUpdated() {
    updated++;
  }

  public void recordError(String entityName, Exception e) {
    failed++;
    errors.add(String.format("Error processing '%s': %s", entityName, e.getMessage()));
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public int getTotal() {
    return created + updated + failed;
  }
}
