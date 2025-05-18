package org.example;


import com.sun.net.httpserver.HttpServer;
import com.typesafe.config.Config;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.example.impl.ConfigurationReader;
import org.example.impl.ServiceAnalytics;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;

@Slf4j
public class ServiceAnalyticsMain {

    private static PrometheusMeterRegistry registry;
    private static long startTime;

    public static void main(String[] args) throws IOException {
        startTime = System.currentTimeMillis();
        log.info("Start service Analytics");
        ConfigReader configReader = new ConfigurationReader();
        Config config = configReader.loadConfig();
        initMetrics(config);
        Service service = new ServiceAnalytics();
        log.info("made by ykwais");
        service.start(config);
        log.info("Terminate service Analytics");
    }

    private static void initMetrics(Config config) {
        try {
            registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);


            registry.config().commonTags(
                    "application", "java-service-filtering",
                    "instance", System.getenv("HOSTNAME") != null ?
                            System.getenv("HOSTNAME") : "local"
            );


            Gauge.builder("process_uptime_seconds",
                            () -> (System.currentTimeMillis() - startTime) / 1000.0)
                    .register(registry);

            Gauge.builder("process_start_time_seconds",
                            () -> startTime / 1000.0)
                    .register(registry);


            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();


            Gauge.builder("system_cpu_usage", osBean, bean -> bean.getSystemLoadAverage())
                    .register(registry);


            Gauge.builder("process_cpu_usage", osBean, bean -> bean.getProcessCpuLoad())
                    .register(registry);

            Gauge.builder("system_cpu_count", osBean, bean -> bean.getAvailableProcessors())
                    .register(registry);


            Gauge.builder("system_load_average_1m", osBean, bean -> {
                        double load = bean.getSystemLoadAverage();
                        return load < 0 ? 0 : load; // Для Windows
                    })
                    .description("System load average over 1 minute")
                    .register(registry);

            new JvmMemoryMetrics().bindTo(registry);
            new JvmGcMetrics().bindTo(registry);
            new ClassLoaderMetrics().bindTo(registry);
            new JvmThreadMetrics().bindTo(registry);


            startMetricsServer(config);

        } catch (IOException e) {
            log.error("Failed to start metrics server", e);
        }
    }

    private static void startMetricsServer(Config config) throws IOException {
        Integer serverPort = config.getInt("application.server_port");
        log.info("Server port from config: {}", serverPort);

        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/metrics", exchange -> {
            String response = registry.scrape();
            exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        server.setExecutor(null);
        server.start();
        log.info("Custom Prometheus metrics server started at http://0.0.0.0:8082/metrics");
    }


}