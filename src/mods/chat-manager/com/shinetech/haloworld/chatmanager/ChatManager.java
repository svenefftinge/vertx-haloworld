package com.shinetech.haloworld.chatmanager;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the list of users currently in the chat and can provide presence information to anyone
 * who requests it.<p/>
 * Chat clients should send a message to address <code>chat.manager.ping</code> every second providing the login name of
 * the user. The message sent should be of the format <code>{ name: "LOGIN-NAME"}</code> where <code>LOGIN-NAME</code>
 * is the name they are using in the chat. If the chat manager has not seen the user before it will add it to the list.
 * If the chat manager has not seen a ping from a client in two seconds it will treat the user as having left the chat.<p/>
 * The chat manager will send presence updates periodically on the <code>chat.manager.updates</code> address. They will be
 * messages of the format <code>{ event: "EVENT-TYPE", name: "LOGIN-NAME" }</code>. <code>EVENT-TYPE</code> can
 * be either <code>joined</code> or <code>left</code>.<p/>
 * The chat manager will respond to any message sent to address <code>chat.manager.list</code> and return a message
 * of the format <code>{ names: LOGIN-NAMES }</code> where <code>LOGIN-NAMES</code> is an array of member login names.
 */
public class ChatManager extends Verticle {

    private static final String CHAT_MANAGER_BASE_ADDRESS = "chat.manager";
    private static final String CHAT_MANAGER_LIST = CHAT_MANAGER_BASE_ADDRESS + ".list";
    private static final String CHAT_MANAGER_UPDATES = CHAT_MANAGER_BASE_ADDRESS + ".updates";
    private static final String CHAT_MANAGER_PING = CHAT_MANAGER_BASE_ADDRESS + ".ping";
    
    private static final String EVENT_TYPE_JOINED = "joined";
    private static final String EVENT_TYPE_LEFT = "left";
    
    private Map<String, Long> members = new HashMap<String, Long>();
    private Logger logger;
    
    // DELEGATING EVENT HANDLERS
    
    private Handler<Message<JsonObject>> listRequestHandler = new Handler<Message<JsonObject>>() {
        public void handle(Message<JsonObject> event) {
            handleListRequest(event);    
        }
    };
   
    private Handler<Message<JsonObject>> pingRequestHandler = new Handler<Message<JsonObject>>() {
        public void handle(Message<JsonObject> event) {
            handlePingRequest(event);
        }
    };

    // EVENT HANDLING LOGIC
    private JsonArray memberNames() {
        JsonArray names = new JsonArray();

        for(String name : members.keySet()) {
            names.addString(name);
        }
        return names;
    }

    private void handleListRequest(Message<JsonObject> event) {
        logger.debug("Received list request - sending current members back");

        
        JsonObject message = new JsonObject();
        message.putArray("names", memberNames());
        event.reply(message);
    }

    private void handlePingRequest(Message<JsonObject> event) {
        String name = event.body.getString("name");
//        logger.debug("Received ping from " + name);
        
        // update last seen timestamp for member or add them in if they are not there
        boolean isNew = !members.containsKey(name);
        long now = System.currentTimeMillis();
        members.put(name, now);
        
        if(isNew) {
            logger.debug("@" + name + " has joined the chat");
            notifyJoined(name);
        }
    }
    
    private void checkMembership() {
        long now = System.currentTimeMillis();
        List<String> leavers = new ArrayList<String>();
        
        // evict members who have not sent an update in more than two seconds
        for (Map.Entry<String, Long> entry : members.entrySet()) {
            long lastSeen = entry.getValue();
            if((now - lastSeen) > 2000) {
                String name = entry.getKey();
                leavers.add(name);
                logger.debug("@" + name + " has left the chat");
                
            }
        }
        for(String name : leavers) {
            members.remove(name);
            notifyLeft(name);
        }
    }
    
    private void notifyJoined(String name) {
        EventBus eventBus = vertx.eventBus();
        eventBus.send(CHAT_MANAGER_UPDATES, createNotificationEvent(EVENT_TYPE_JOINED, name));
    }
    
    private void notifyLeft(String name) {
        EventBus eventBus = vertx.eventBus();
        eventBus.send(CHAT_MANAGER_UPDATES, createNotificationEvent(EVENT_TYPE_LEFT, name));
    }
    
    private JsonObject createNotificationEvent(String eventType, String memberName) {
        JsonObject message = new JsonObject();
        message.putString("event", eventType);
        message.putString("name", memberName);
        message.putArray("names", memberNames());
        return message;
    }

    @Override
    public void start() throws Exception {
        logger = container.getLogger();

        // register handlers to listen for list and ping requests.
        registerHandlers();

        // setup periodic timer to check presence status and
        // send out updates
        vertx.setPeriodic(1000, new Handler<Long>() {
            public void handle(Long event) {
                checkMembership();
            }
        });
        
        logger.debug("Started Chat Manager");
        
    }

    private void registerHandlers() {
        EventBus eventBus = vertx.eventBus();
        
        eventBus.registerHandler(CHAT_MANAGER_LIST, listRequestHandler);
        eventBus.registerHandler(CHAT_MANAGER_PING, pingRequestHandler);

    }
}
