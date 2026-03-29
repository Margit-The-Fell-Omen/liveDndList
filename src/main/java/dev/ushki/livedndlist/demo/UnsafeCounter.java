package dev.ushki.livedndlist.demo;

public class UnsafeCounter {

  private int count = 0;

  public void increment() {
    count++;
  }

  public int getCount() {
    return count;
  }
}
