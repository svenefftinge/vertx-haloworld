package com.shinetech.haloworld.hal;

import static com.shinetech.haloworld.hal.MessageType.*;

/**
 * Stores a message sent to the chat session.
 */
public class ChatMessage extends BaseMessage {

    public ChatMessage() {
        messageType = CHAT;
    }

    public ChatMessage(String sender, String messageText) {
        super(CHAT, messageText);
        this.sender = sender;
    }

    public ChatMessage(String messageText) {
        super(CHAT, messageText);
    }
}
