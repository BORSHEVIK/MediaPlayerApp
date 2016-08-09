package com.example.aliaksandrmirashnichenka.mediaplayer.models;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * This class is Json model for Actor
 *
 * Created by amirashnichenka on 8/2/2016.
 */
@JsonObject
public class Actor implements Serializable {

    @JsonField
    @JsonProperty("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String mName) {
        this.name = mName;
    }
}
