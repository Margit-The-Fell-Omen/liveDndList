package dev.ushki.livedndlist.demo;

public class SafeCounterSynchronized {

  private int count = 0;

  public synchronized void increment() {
    count++;
  }

  public synchronized int getCount() {
    return count;
  }
}
