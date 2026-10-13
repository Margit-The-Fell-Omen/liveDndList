package dev.ushki.livedndlist.cache;

public class CacheEntry {

  private Object value;

  public CacheEntry() {
  }

  public CacheEntry(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }
}