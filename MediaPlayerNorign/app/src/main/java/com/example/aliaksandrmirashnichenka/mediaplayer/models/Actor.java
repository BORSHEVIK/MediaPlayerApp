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
@SuppressWarnings("ALL")
@JsonObject
public class Actor implements Serializable {

    @JsonField
    @JsonProperty("name")
    private String name;


    public static Actor convertJsonToObject(JSONObject jsonObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), Actor.class);
    }

    public String getName() {
        return name;
    }

    public void setName(String mName) {
        this.name = mName;
    }
}
