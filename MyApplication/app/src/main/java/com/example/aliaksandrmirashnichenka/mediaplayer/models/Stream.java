package com.example.aliaksandrmirashnichenka.mediaplayer.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by amirashnichenka on 8/2/2016.
 */
@JsonObject
public class Stream implements Serializable {

    @JsonField
    @JsonProperty("type")
    private String type;

    @JsonField
    @JsonProperty("url")
    private String url;

    public static Stream getObjectFromJson(JSONObject jsonObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), Stream.class);
    }

    public String getType() {
        return type;
    }

    public void setType(String mType) {
        this.type = mType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String mUrl) {
        this.url = mUrl;
    }
}
