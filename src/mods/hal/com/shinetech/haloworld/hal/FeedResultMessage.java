package com.shinetech.haloworld.hal;

import org.vertx.java.core.json.JsonObject;

import static com.shinetech.haloworld.hal.MessageType.*;

/**
 * BaseMessage with feed information
 */
public class FeedResultMessage extends BaseMessage {

    public Data data;
    public String resultId;
    public String resultType;

    public FeedResultMessage() {
    }

    public FeedResultMessage(String resultId, String resultType, String messageText, Data data) {
        super(FEED_RESULT, messageText);
        this.data = data;
        this.resultId = resultId;
        this.resultType = resultType;
    }

    @Override
    public void createJsonMessageData(JsonObject message) {
        message.putObject("data", data.asJsonObject());
        message.putString("resultId", resultId);
        message.putString("resultType", resultType);
    }
}
