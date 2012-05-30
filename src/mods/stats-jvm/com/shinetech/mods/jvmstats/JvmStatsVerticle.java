package com.shinetech.mods.jvmstats;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;

/**
 * Verticle that returns JVM statistics.
 */
public class JvmStatsVerticle extends Verticle {
    private final int POLLING_INTERVAL = 4000;
    private static final String ADDRESS = "stats.jvm";
    private static final String RESULTS_ADDRESS = "stats.jvm.results";

    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private long timerId = -1;

    private Logger logger;
    
    @Override
    public void start() throws Exception {
        logger = container.getLogger();
        
        registerHandlers();
    }

    private void registerHandlers() {

        EventBus eventBus = vertx.eventBus();

        Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> event) {
                handleRequest(event.body);      
            }
        };
        
        eventBus.registerHandler(ADDRESS, handler);
    }

    private void handleRequest(JsonObject message) {
        logger.debug("Received a JVM stats message ...");
        String action = message.getString("action");
        if("START".equals(action)) {
            timerId = vertx.setPeriodic(POLLING_INTERVAL, new Handler<Long>() {
                public void handle(Long event) {
                    sendStats();
                }
            });
        } else {
            // stop task
            vertx.cancelTimer(timerId);
        }
    }

    private void sendStats() {
        logger.debug("Sending JVM stats ....");
        // get stats data
        MemoryUsage usage = memoryMXBean.getHeapMemoryUsage();
        long maxMem = usage.getMax();
        long usedMem = usage.getUsed();
        float heapPercentageUsage = 100 * ((float) usedMem/maxMem);
        long uptime = runtimeMXBean.getUptime();
        
        // send the feed to a static address
        JsonObject stats = new JsonObject()
                .putNumber("uptime", uptime)
                .putNumber("heapUsage", usedMem)
                .putNumber("heapPercentageUsage", heapPercentageUsage);

        vertx.eventBus().send(RESULTS_ADDRESS, stats);
    }
}
