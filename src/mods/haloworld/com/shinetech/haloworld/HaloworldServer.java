package com.shinetech.haloworld;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Main vertical for the Haloworld application
 */
public class HaloworldServer extends Verticle {
    
    private Logger logger;

    @Override
    public void start() throws Exception {
        logger = container.getLogger();
        createSockJsServer();
        
        ModuleDeployment[] deployments = new ModuleDeployment[] {
            webServerDeployment(),
            new ModuleDeployment("chat-manager", new JsonObject()),
            new ModuleDeployment("wolfram-alpha", new JsonObject()),
            new ModuleDeployment("stats-jvm", new JsonObject()),
            new ModuleDeployment("hal", new JsonObject())
        };
        
        deployModules(Arrays.asList(deployments), new Handler<Void>() {
            public void handle(Void event) {
                logger.debug("Finished deploying modules");
            }
        });

    }

    private ModuleDeployment webServerDeployment() {
        logger.debug("Creating web server");
        JsonObject config = new JsonObject();
        config.putString("web_root", "web");
        config.putString("index_page", "index.html");
        config.putString("host", "localhost");
        config.putNumber("port", 8080);
        return new ModuleDeployment("web-server", config);
    }

    private void createSockJsServer() {
        logger.debug("Creating SockJS server");
        // SockJs server listens on port 8080 for chat messages and pushes them back out along with any feed
        // and log messages from @hal or the chat manager
        HttpServer server = vertx.createHttpServer();
        JsonObject config = new JsonObject().putString("prefix", "/chat");
        
        JsonArray permitted = new JsonArray()
            .add(new JsonObject().putString("address", "chat.msg"))
            .add(new JsonObject().putString("address", "chat.manager.ping"))
            .add(new JsonObject().putString("address", "chat.manager.list"));
        
        
        vertx.createSockJSServer(server).bridge(config, permitted);
        server.listen(8081);
        

    }
    
    private void deployModules(final List<ModuleDeployment> moduleDeployments, final Handler<Void> doneHandler) {
        final Iterator<ModuleDeployment> it = moduleDeployments.iterator();
        
        final Handler<Void> handler = new Handler<Void>() {
            
            public void handle(Void event) {
                if(it.hasNext()) {
                    ModuleDeployment deployment = it.next();
                    logger.debug("Deploying module: " + deployment.moduleName);

                    container.deployVerticle(deployment.moduleName, deployment.moduleConfig, 1, this);
                } else {
                    doneHandler.handle(null);
                }
            }
        };
        
        handler.handle(null);
    }
    
    private class ModuleDeployment {
        public String moduleName;
        public JsonObject moduleConfig;

        private ModuleDeployment(String moduleName, JsonObject moduleConfig) {
            this.moduleName = moduleName;
            this.moduleConfig = moduleConfig;
        }
    }
}
