package com.shinetech.haloworld.hal;

import org.vertx.java.core.json.JsonObject;

import javax.xml.bind.annotation.XmlRootElement;

import static com.shinetech.haloworld.hal.LogLevel.*;
import static com.shinetech.haloworld.hal.MessageType.*;

/**
 * Represents a log message that can have a logging level to indicate severity.
 */
@XmlRootElement
public class LogMessage extends BaseMessage {

    public LogLevel level = DEBUG;

    public LogMessage(String messageText) {
        super(LOG, messageText);
    }

    public LogMessage(LogLevel level, String messageText) {
        super(LOG, messageText);
        this.level = level;
    }

    @Override
    public void createJsonMessageData(JsonObject message) {
        message.putString("level", level.name());
    }
}
