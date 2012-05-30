package com.shinetech.haloworld.hal;

import org.vertx.java.core.json.JsonObject;

import static com.shinetech.haloworld.hal.MessageType.*;

/**
 * BaseMessage containing updated data for a feed result.
 */
public class FeedResultUpdateMessage extends FeedResultMessage {

    public FeedResultUpdateMessage() {
    }

    public FeedResultUpdateMessage(String resultId, String resultType, String messageText, Data data) {
        super(resultId, resultType, messageText, data);
        this.messageType = FEED_RESULT_UPDATE;
    }
}
