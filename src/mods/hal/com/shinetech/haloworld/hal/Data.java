package com.shinetech.haloworld.hal;

import org.vertx.java.core.json.JsonObject;

/**
 * Base class for feed result data
 */
public abstract class Data {

    public abstract JsonObject asJsonObject();
}
