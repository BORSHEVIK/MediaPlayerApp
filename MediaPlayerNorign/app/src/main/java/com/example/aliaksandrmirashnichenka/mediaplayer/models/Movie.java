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
 * This class is Json model for Movie
 *
 * Created by amirashnichenka on 8/2/2016.
 */
@JsonObject
public class Movie implements Serializable {

    @JsonField
    @JsonProperty("id")
    private long id;

    @JsonField
    @JsonProperty("title")
    private String title;

    @JsonField
    @JsonProperty("description")
    private String description;

    @JsonField
    @JsonProperty("meta")
    private Meta meta;

    @JsonField
    @JsonProperty("images")
    private Image images;

    @JsonField
    @JsonProperty("streams")
    private Stream streams;

    public long getID() {
        return id;
    }

    public void setID(long mID) {
        this.id = mID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String mTitle) {
        this.title = mTitle;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(@NonNull Meta mMeta) {
        this.meta = mMeta;
    }

    public Image getImages() {
        return images;
    }

    public void setImages(@NonNull Image mImage) {
        this.images = mImage;
    }

    public Stream getStreams() {
        return streams;
    }

    public void setStreams(@NonNull Stream mStream) {
        this.streams = mStream;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String mDescription) {
        this.description = mDescription;
    }
}
