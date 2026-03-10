package dev.ushki.livedndlist.cache;

import java.util.Arrays;

public class CompositeKey {

  private final Object[] keys;

  public CompositeKey(Object... keys) {
    this.keys = keys;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompositeKey that = (CompositeKey) o;
    return Arrays.deepEquals(keys, that.keys);
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(keys);
  }

  @Override
  public String toString() {
    return Arrays.deepToString(keys);
  }
}
