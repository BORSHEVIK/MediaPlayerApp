package com.example.aliaksandrmirashnichenka.mediaplayer.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by amirashnichenka on 8/2/2016.
 */
public class Movie implements Serializable {

    @JsonProperty("id")
    private long mID;

    @JsonProperty("title")
    private String mTitle;

    @JsonProperty("description")
    private String mDescription;

    @JsonProperty("meta")
    private Meta mMeta;

    @JsonProperty("images")
    private Image mImage;

    @JsonProperty("streams")
    private Stream mStream;

    private Movie(){}

    public static Movie convertJsonToObject(JSONObject jsonObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), Movie.class);
    }

    public long getID() {
        return mID;
    }

    public void setID(long mID) {
        this.mID = mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Meta getMeta() {
        return mMeta;
    }

    public void setMeta(Meta mMeta) {
        this.mMeta = mMeta;
    }

    public Image getImage() {
        return mImage;
    }

    public void setImage(Image mImage) {
        this.mImage = mImage;
    }

    public Stream getStream() {
        return mStream;
    }

    public void setStream(Stream mStream) {
        this.mStream = mStream;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }
}
