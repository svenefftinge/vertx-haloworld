package com.shinetech.haloworld.hal.solver;

import com.shinetech.haloworld.hal.Answer;
import com.shinetech.haloworld.hal.AnswerContext;
import com.shinetech.haloworld.hal.QuestionSolver;
import com.shinetech.haloworld.hal.TextData;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import static com.shinetech.haloworld.hal.Answer.TYPE.SIMPLE_TEXT_RESULT;

/**
 * Queries Wolfram Alpha to answer questions.
 */
public class WolframAlphaSolver implements QuestionSolver {
    private static final String ADDRESS = "wolfram.query";

    String[] questions = new String[] {
        "how many (.*) in (.*)",
        "what is the (.*) of (.*)",
        "how many (.*) until (.*)",
        "when did (.*) live",
        "(.*) in (.*)",
        "convert (.*) to (.*)"
    };

    @Override
    public void provideAnswer(final String questionText, final AnswerContext context) {
        context.answeringQuestion();

        JsonObject message = new JsonObject()
                .putString("question", questionText);

        Handler<Message<JsonObject>> handler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> event) {
                handleReply(event.body, context);
            }
        };
        context.getEventBus().send(ADDRESS, message, handler);
    }
    
    private void handleReply(JsonObject reply, AnswerContext context) {
        String status = reply.getString("status");
        if("SUCCESS".equals(status)) {
            context.publishAnswer(new Answer(SIMPLE_TEXT_RESULT.name(), new TextData(reply.getString("answer"))));
        } else {
            context.couldNotAnswerQuestion(reply.getString("error"));
        }
    }

    @Override
    public boolean canProvideAnswer(String questionText) {
        for(String question : questions) {
            if(questionText.matches(question)) {
                return true;
            } else {
            }
        }
        return false;
    }


}
