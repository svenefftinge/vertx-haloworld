package com.shinetech.haloworld.hal;

import org.vertx.java.core.json.JsonObject;

/**
 * Represents text data (until I can figure out how to get simple Strings rendering to JSON Strings).
 */
public class TextData extends Data{
    public String text;

    public TextData(JsonObject jsonObject) {
        this.text = jsonObject.getString("text");
    }

    public TextData(String text) {
        this.text = text;
    }

    @Override
    public JsonObject asJsonObject() {
        return new JsonObject().putString("text", text);
    }
}
