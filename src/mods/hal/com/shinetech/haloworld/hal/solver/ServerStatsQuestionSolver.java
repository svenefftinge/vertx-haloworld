package com.shinetech.haloworld.hal.solver;

import com.shinetech.haloworld.hal.ServerStatsData;
import com.shinetech.haloworld.hal.Answer;
import static com.shinetech.haloworld.hal.Answer.TYPE.*;
import com.shinetech.haloworld.hal.AnswerContext;
import com.shinetech.haloworld.hal.QuestionSolver;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import static com.shinetech.haloworld.hal.Answer.TYPE.SERVER_STATS_RESULT;

/**
 * Knows how to answer questions about server statistics
 */
public class ServerStatsQuestionSolver implements QuestionSolver {

    private static final String RESULTS_ADDRESS = "stats.jvm.results";
    private static final String ADDRESS = "stats.jvm";
    
    private Set<String> resultIds = new HashSet<String>();
    private boolean gettingResults = false; // true if we have triggered the stats mod to start sending results

    String[] questions = new String[] {
            "how is the server doing",
            "how the server is doing",
            "how is the server performance",
            "show me the server stats",
            "show me some server stats",
            "show me the stats",
            "how are the stats",
            "how are the server stats"
    };

    @Override
    public void provideAnswer(String questionText, final AnswerContext context) {

        scheduleUpdates(context.getNewResultId(), context);
    }

    @Override
    public boolean canProvideAnswer(String questionText) {
        for(String question : questions) {
            if(questionText.contains(question)) {
                return true;
            }
        }
        return false;
    }

    private void scheduleUpdates(final String resultId, final AnswerContext context) {
        context.log(resultId, "Scheduling regular stats updates for this question");

        resultIds.add(resultId);
        
        // if we are not receiving a feed of results then send a message to the stats
        // mod to schedule the feed and setup a handler that will split off updates
        // to each feed widget
        if(!gettingResults) {
            JsonObject message = new JsonObject().putString("action", "START");
            EventBus eventBus = context.getEventBus();

            context.getLogger().debug("Scheduling JVM stats");
            eventBus.send(ADDRESS, message);
            eventBus.registerHandler(RESULTS_ADDRESS, new Handler<Message<JsonObject>>() {
                public void handle(Message<JsonObject> event) {
                    context.getLogger().debug("Distributing stats results");
                    distributeStats(event.body, context);
                }
            });
            gettingResults = true;
        }
        
    }
    
    private void distributeStats(JsonObject stats, AnswerContext context) {
        Answer answer = createAnswer(stats);
        for(String resultId : resultIds) {
            context.getLogger().debug("Publishing update for resultID: " + resultId);
            context.publishUpdate(resultId, answer);
        }
    }
    
    private Answer createAnswer(JsonObject stats) {
        ServerStatsData serverStatsData = new ServerStatsData(stats);
        return new Answer(SERVER_STATS_RESULT.name(), serverStatsData);
    }
}
