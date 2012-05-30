package com.shinetech.haloworld.hal;

import org.vertx.java.core.json.JsonObject;

/**
 * Encapsulates a feed result
 */
public class Answer {
    public String resultType;
    public Data data;

    public Answer(String resultType, Data data) {
        this.resultType = resultType;
        this.data = data;
    }

    public static enum TYPE {
        SIMPLE_TEXT_RESULT,
        SERVER_STATS_RESULT
    }
}
