package com.shinetech.haloworld.hal;

import org.vertx.java.core.json.JsonObject;

import java.text.DecimalFormat;

/**
 * Represents server statistics
 */
public class ServerStatsData extends Data{

    public String uptime;
    public String heapUsage;
    public float heapPercentageUsage;
    public String heapPercentageUsageString;

    public ServerStatsData(JsonObject jsonObject) {
        uptime = formatUptime(jsonObject.getNumber("uptime").longValue());
        heapUsage = formatHeapUsageInMB(jsonObject.getNumber("heapUsage").longValue());
        heapPercentageUsage = jsonObject.getNumber("heapPercentageUsage").floatValue();
        heapPercentageUsageString = formatHeapPercentageUsageAsString(heapPercentageUsage);
    }

    public ServerStatsData(String uptime, String heapUsage, float heapPercentageUsage, String heapPercentageUsageString) {
        this.uptime = uptime;
        this.heapUsage = heapUsage;
        this.heapPercentageUsage = heapPercentageUsage;
        this.heapPercentageUsageString = heapPercentageUsageString;
    }

    @Override
    public JsonObject asJsonObject() {
        return new JsonObject()
                .putString("uptime", uptime)
                .putString("heapUsage", heapUsage)
                .putNumber("heapPercentageUsage", heapPercentageUsage)
                .putString("heapPercentageUsageString", heapPercentageUsageString);
    }

    private String formatHeapUsageInMB(long usedMem) {
        StringBuffer buf = new StringBuffer();
        float mb = (float) usedMem / (1024 * 1024);
        DecimalFormat df = new DecimalFormat("#.#");
        buf.append(df.format(mb)).append(" MB");
        return buf.toString();
    }
    
    private String formatUptime(long uptime) {
        StringBuffer buf = new StringBuffer();
        final long MILLISECONDS_IN_ONE_SECOND = 1000;
        final long MILLISECONDS_IN_ONE_MINUTE = MILLISECONDS_IN_ONE_SECOND * 60;
        final long MILLISECONDS_IN_ONE_HOUR = MILLISECONDS_IN_ONE_MINUTE * 60;

        int hours = (int) (uptime / MILLISECONDS_IN_ONE_HOUR);
        long remainder = uptime % MILLISECONDS_IN_ONE_HOUR;

        if(hours > 0) buf.append(hours).append("hrs ");

        int minutes = (int) (remainder / MILLISECONDS_IN_ONE_MINUTE);
        remainder = remainder % MILLISECONDS_IN_ONE_MINUTE;

        if(minutes > 0) buf.append(minutes).append(" min. ");

        int seconds = (int) (remainder / MILLISECONDS_IN_ONE_SECOND);

        if (seconds > 0) buf.append(seconds).append(" sec.");

        return buf.toString();
    }
    
    private String formatHeapPercentageUsageAsString(float heapPercentageUsage) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(heapPercentageUsage);
    }
}
