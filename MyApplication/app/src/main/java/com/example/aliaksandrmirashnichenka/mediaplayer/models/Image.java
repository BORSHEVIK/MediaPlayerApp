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
public class Image implements Serializable {

    @JsonField
    @JsonProperty("cover")
    private String cover;

    @JsonField
    @JsonProperty("placeholder")
    private String placeholder;

    public static Image convertJsonToObject(JSONObject jsonObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), Image.class);
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String mCoverName) {
        this.cover = mCoverName;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String mPlaceholderName) {
        this.placeholder = mPlaceholderName;
    }
}
