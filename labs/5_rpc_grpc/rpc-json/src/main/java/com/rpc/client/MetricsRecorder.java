package com.rpc.client;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MetricsRecorder {

  private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

  public static long usedMemoryBytes() {
    return memoryBean.getHeapMemoryUsage().getUsed();
  }

  public static void printMetrics(String label, long startNano, long memoryBefore) {
    long elapsed = System.nanoTime() - startNano;
    long memoryAfter = usedMemoryBytes();
    long memoryDelta = memoryAfter - memoryBefore;

    System.out.printf(
        "[METRICS][%s] Tiempo: %.3f ms | ΔMemoria: %+.2f KB%n",
        label, elapsed / 1_000_000.0, memoryDelta / 1024.0);
  }
}
