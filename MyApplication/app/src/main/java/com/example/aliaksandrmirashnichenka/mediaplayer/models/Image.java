package com.example.aliaksandrmirashnichenka.mediaplayer.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by amirashnichenka on 8/2/2016.
 */
public class Image implements Serializable {
    @JsonProperty("cover")
    private String mCoverName;

    @JsonProperty("placeholder")
    private String mPlaceholderName;

    private Image(){}

    public static Image convertJsonToObject(JSONObject jsonObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), Image.class);
    }

    public String getCoverName() {
        return mCoverName;
    }

    public void setCoverName(String mCoverName) {
        this.mCoverName = mCoverName;
    }

    public String getPlaceholderName() {
        return mPlaceholderName;
    }

    public void setPlaceholderName(String mPlaceholderName) {
        this.mPlaceholderName = mPlaceholderName;
    }
}
