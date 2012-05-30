package com.shinetech.haloworld.hal;

import com.shinetech.haloworld.hal.solver.CurrentTimeQuestionSolver;
import com.shinetech.haloworld.hal.solver.ServerStatsQuestionSolver;
import com.shinetech.haloworld.hal.solver.WolframAlphaSolver;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.shinetech.haloworld.hal.MessageType.*;

/**
 * HAL the intelligent chat bot.
 */
public class HAL extends Verticle {

    private static final String CHAT_ADDRESS = "chat.msg";
    private static final String CHAT_MANAGER_BASE_ADDRESS = "chat.manager";
    private static final String CHAT_MANAGER_PING_ADDRESS = CHAT_MANAGER_BASE_ADDRESS + ".ping";
    private static final String CHAT_MANAGER_UPDATE_ADDRESS = CHAT_MANAGER_BASE_ADDRESS + ".updates";
    private static final String NAME = "HAL";
    private static final String HANDLE = "@" + NAME.toLowerCase();

    private List<QuestionSolver> questionSolvers = new ArrayList<QuestionSolver>();
    private Logger logger;

    private Handler<Message<JsonObject>> chatMessageHandler = new Handler<Message<JsonObject>>() {
        public void handle(Message<JsonObject> event) {
            handleChatMessage(event);
        }
    };
    
    private Handler<Message<JsonObject>> updateMessageHandler = new Handler<Message<JsonObject>>() {
        public void handle(Message<JsonObject> event) {
            handleUpdateMessage(event);
        }
    };

    public HAL() { }

    @Override
    public void start() throws Exception {
        logger = container.getLogger();

        registerHandlers();

        vertx.setPeriodic(1000, new Handler<Long>() {
            public void handle(Long event) {
                EventBus eventBus = vertx.eventBus();
                eventBus.send(CHAT_MANAGER_PING_ADDRESS, new JsonObject().putString("name", getChatName()));
            }
        });

        addQuestionSolvers();
        logger.debug("Started up HAL");
        
    }

    private void addQuestionSolvers() {
        questionSolvers.add(new CurrentTimeQuestionSolver());
        questionSolvers.add(new WolframAlphaSolver());
        questionSolvers.add(new ServerStatsQuestionSolver());
    }

    public String getChatName() {
        return NAME;
    }

    private void registerHandlers() {
        vertx.eventBus().registerHandler(CHAT_ADDRESS, chatMessageHandler);
        vertx.eventBus().registerHandler(CHAT_MANAGER_UPDATE_ADDRESS, updateMessageHandler);
    }
    
    private void handleUpdateMessage(Message<JsonObject> message) {
        JsonObject body = message.body;
        
        String eventType = body.getString("event");
        if("joined".equals(eventType)) {
            String name = body.getString("name");
            if(!NAME.equals(name)) {
                sendMessage(new ChatMessage("Hello " + name + ", welcome to the chat ... I'm here to help"));
            }
        }
    }

    private void handleChatMessage(Message<JsonObject> message) {
        JsonObject body = message.body;
        
        // ignore if not a chat message
        if(!CHAT.name().equals(body.getString("messageType"))) {
            return;
        }
        
        String sender = body.getString("sender");
        String messageText = body.getString("messageText");

        logger.debug("HAL heard a message from " + sender);
        
        String text = messageText.trim().toLowerCase();

        if(mentionedInMessage(text)) {
            // remove the @mention and any question mark
            text = text.replace(HANDLE, "").replace("?", "").trim();

            for(QuestionSolver questionSolver : questionSolvers) {
                if(questionSolver.canProvideAnswer(text)) {
                    questionSolver.provideAnswer(text, answerContext);
                    return;
                }
            }
            // if we are here we couldn't answer the question
            sendMessage(new ChatMessage("Sorry " + sender + ", I could not answer your question"));
        }
    }
    
    private void sendMessage(BaseMessage message) {
        message.sender = getChatName();
        JsonObject jsonMessage = message.asJsonObject();
        EventBus eventBus = vertx.eventBus();
        eventBus.send(CHAT_ADDRESS, jsonMessage);
    }

    private boolean mentionedInMessage(String messageText) {
        return messageText.startsWith(HANDLE);
    }

    private String createResultId() {
        Long id = new Date().getTime();
        return id.toString();
    }

    private final AnswerContext answerContext = new AnswerContext() {
        @Override
        public String publishAnswer(Answer answer) {
            String resultId =  createResultId();
            FeedResultMessage message = new FeedResultMessage(resultId, answer.resultType, "Here is your answer", answer.data);
            sendMessage(message);
            return resultId;
        }

        @Override
        public void publishUpdate(String resultId, Answer answer) {
            FeedResultUpdateMessage message = new FeedResultUpdateMessage(resultId, answer.resultType, "", answer.data);
            sendMessage(message);
        }

        @Override
        public void couldNotAnswerQuestion(String reason) {
            sendMessage(new ChatMessage("Sorry, I could not answer your question"));
        }

        @Override
        public void answeringQuestion() {
            sendMessage(new ChatMessage("Looking up your answer, please wait ..."));
        }

        @Override
        public void log(String resultId, String message) {
            sendMessage(new LogMessage(LogLevel.INFO, "Result " + resultId + ": " + message));
        }

        @Override
        public void executeJobRepeatedly(final int interval, final Runnable job) {

            vertx.setPeriodic(interval, new Handler<Long>() {
                @Override
                public void handle(Long event) {
                    job.run();
                }
            });

        }

        public EventBus getEventBus() {
            return vertx.eventBus();
        }

        @Override
        public String getNewResultId() {
            return createResultId();
        }

        @Override
        public Logger getLogger() {
            return logger;
        }
    };


}
