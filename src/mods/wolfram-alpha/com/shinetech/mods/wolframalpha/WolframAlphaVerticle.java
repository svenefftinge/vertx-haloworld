package com.shinetech.mods.wolframalpha;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;

/**
 * Main
 */
public class WolframAlphaVerticle extends Verticle {
    private static final String API_KEY = "XX8QPW-X274AP8K34";
    private static final String ADDRESS = "wolfram.query";
    
    Logger logger;
    private HttpClient httpClient;
    
    @Override
    public void start() throws Exception {
        logger = container.getLogger();

        registerHandlers();

        httpClient = vertx.createHttpClient()
                .setHost("api.wolframalpha.com")
                .setPort(80);
        
        logger.debug("Started Wolfram Alpha Vertical");
    }

    private void registerHandlers() {
        EventBus eventBus = vertx.eventBus();
        eventBus.registerHandler(ADDRESS, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                JsonObject body = event.body;
                String question = body.getString("question");
                queryWolfram(question, event);
            }
        });
    }

    private void queryWolfram(final String question, final Message<JsonObject> event) {
        
        final Buffer buffer = new Buffer();
        try {
            String encodedQuestion = URLEncoder.encode(question, "UTF-8");
            httpClient.getNow("/v2/query?input=" + encodedQuestion + "&appid=" + API_KEY, new Handler<HttpClientResponse>() {
                public void handle(HttpClientResponse response) {
                    logger.debug("Response code: " + response.statusCode);
                    if(response.statusCode == 200) {
                        // accumulate data into our buffer for parsing
                        response.dataHandler(new Handler<Buffer>() {
                            public void handle(Buffer data) {
                                buffer.appendBuffer(data);    
                            }
                        });
    
                        response.endHandler(new Handler<Void>() {
                            public void handle(Void v) {
                                parseAndSendAnswer(buffer, event);        
                            }
                        });
                    } else {
                        sendError("Error querying Wolfram Alpha API", event);
                    }
                }
            });        
        } catch(Exception e) {
            logger.error("Error querying Wolfram Alpha", e);
            sendError("Error querying Wolfram Alpha API: " + e.getMessage(), event);
        }
    }
    
    private void parseAndSendAnswer(Buffer buffer, Message<JsonObject> event) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    
            WolframHandler wolframHandler = new WolframHandler();
            ByteArrayInputStream is = new ByteArrayInputStream(buffer.getBytes());
            parser.parse(is, wolframHandler);
            logger.debug("Success: " + wolframHandler.isSuccess() + " - Result: " + wolframHandler.getResult());
    
            if(wolframHandler.isSuccess()) {
                sendAnswer(wolframHandler.getResult(), event);
            } else {
                sendError("Error querying Wolfram Alpha API", event);
            }
        } catch(Exception e) {
            sendError("Error parsing result from Wolfram Alpha API: " + e.getMessage(), event);
        }
    }
    
    private void sendAnswer(String answer, Message<JsonObject> event) {        
        JsonObject jsonObject = new JsonObject()
                .putString("status", "SUCCESS")
                .putString("answer", answer);
        event.reply(jsonObject);
    }
    
    private void sendError(String errorMsg, Message<JsonObject> event) {
        JsonObject jsonObject = new JsonObject()
                .putString("status", "ERROR")
                .putString("error", errorMsg);
        event.reply(jsonObject);
    }

    public static class WolframHandler extends DefaultHandler {
        public static final String QUERY_ELEMENT = "queryresult";
        public static final String POD_ELEMENT = "pod";
        public static final String PLAINTEXT_ELEMENT = "plaintext";

        private enum STATE {
            START,
            IN_QUERY,
            IN_RESULT_POD,
            IN_RESULT_TEXT
        };

        private STATE currentState = STATE.START;

        private boolean isSuccess;
        private StringBuffer result = new StringBuffer();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(QUERY_ELEMENT.equals(qName)) {
                // in query element
                currentState = STATE.IN_QUERY;
                isSuccess = Boolean.parseBoolean(attributes.getValue("success"));
            } else if(POD_ELEMENT.equals(qName) && currentState == STATE.IN_QUERY && attributes.getValue("title").startsWith("Result")) {
                // in result POD
                currentState = STATE.IN_RESULT_POD;
            } else if(PLAINTEXT_ELEMENT.equals(qName) && currentState == STATE.IN_RESULT_POD) {
                currentState = STATE.IN_RESULT_TEXT;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(currentState == STATE.IN_RESULT_TEXT && PLAINTEXT_ELEMENT.equals(qName)) {
                currentState = STATE.IN_RESULT_POD;
            } else if(currentState == STATE.IN_RESULT_POD && POD_ELEMENT.equals(qName)) {
                currentState = STATE.IN_QUERY;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(currentState == STATE.IN_RESULT_TEXT) {
                result.append(ch, start, length);
            }
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getResult() {
            return result.toString();
        }
    }
}
