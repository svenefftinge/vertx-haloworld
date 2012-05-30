package com.shinetech.haloworld.hal;

import org.vertx.java.core.json.JsonObject;

import javax.xml.bind.annotation.XmlRootElement;

import static com.shinetech.haloworld.hal.MessageType.*;

/**
 * Represents a message sent to clients. The messageType field indicates the type of message (feed, log, chat etc.)
 */
public abstract class BaseMessage {

    public String sender;

    public MessageType messageType;

    public String messageText;

    public BaseMessage() {

    }

    public BaseMessage(MessageType messageType, String messageText) {
        this.messageType = messageType;
        this.messageText = messageText;
    }
    
    public JsonObject asJsonObject() {
        JsonObject message = new JsonObject()
                .putString("messageType", messageType.name())
                .putString("messageText", messageText)
                .putString("sender", sender);
        
        createJsonMessageData(message);
        return message;
    }
    
    public void createJsonMessageData(JsonObject message) {
        
    }
}
