package dev.ushki.livedndlist.demo;

import java.util.concurrent.atomic.AtomicInteger;

public class SafeCounterAtomic {

  private final AtomicInteger count = new AtomicInteger(0);

  public void increment() {
    count.incrementAndGet();
  }

  public int getCount() {
    return count.get();
  }
}
