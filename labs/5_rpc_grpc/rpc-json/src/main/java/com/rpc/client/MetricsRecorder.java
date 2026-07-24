package com.rpc.client;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class MetricsRecorder {

  private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
  private static PrometheusMeterRegistry prometheusRegistry;

  public static void init(int port) {
    try {
      prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
      Metrics.addRegistry(prometheusRegistry);

      // Bind JVM metrics
      new ClassLoaderMetrics().bindTo(prometheusRegistry);
      new JvmMemoryMetrics().bindTo(prometheusRegistry);
      try {
        new JvmGcMetrics().bindTo(prometheusRegistry);
      } catch (Exception e) {
        System.err.println("Warning: JvmGcMetrics could not be bound: " + e.getMessage());
      }
      new ProcessorMetrics().bindTo(prometheusRegistry);
      new JvmThreadMetrics().bindTo(prometheusRegistry);

      // Start HTTP Server for Prometheus scrape
      HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/metrics", exchange -> {
        String response = prometheusRegistry.scrape();
        byte[] bytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(bytes);
        }
      });
      server.setExecutor(null);
      server.start();
      System.out.println("[METRICS] Servidor Prometheus `/metrics` iniciado en puerto: " + port);
    } catch (Exception e) {
      System.err.println("[METRICS] Error al iniciar servidor Prometheus: " + e.getMessage());
    }
  }

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

  public static void recordRequest(String protocol, String method, String status, long durationNanoseconds) {
    Counter.builder("rpc.requests.total")
        .description("Total number of RPC requests")
        .tag("protocol", protocol)
        .tag("method", method)
        .tag("status", status)
        .register(Metrics.globalRegistry)
        .increment();

    Timer.builder("rpc.requests.latency")
        .description("Latency of RPC requests")
        .tag("protocol", protocol)
        .tag("method", method)
        .tag("status", status)
        .register(Metrics.globalRegistry)
        .record(durationNanoseconds, TimeUnit.NANOSECONDS);
  }
}
