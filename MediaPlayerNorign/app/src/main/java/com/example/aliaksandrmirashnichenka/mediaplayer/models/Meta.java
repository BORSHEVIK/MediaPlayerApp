package com.example.aliaksandrmirashnichenka.mediaplayer.models;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * This class is Json model for Meta
 *
 * Created by amirashnichenka on 8/2/2016.
 */
@JsonObject
public class Meta implements Serializable {

    @JsonField
    @JsonProperty("releaseYear")
    private int releaseYear;

    @JsonField
    @JsonProperty("directors")
    private List<Director> directors;

    @JsonField
    @JsonProperty("actors")
    private List<Actor> actors;

    @SuppressWarnings("unused")
    public static Meta convertJsonToObject(@NonNull JSONObject jsonObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), Meta.class);
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(@NonNull int mYear) {
        this.releaseYear = mYear;
    }

    public List<Director> getDirectors() {
        return directors;
    }

    public void setDirectors(@NonNull List<Director> mDirectors) {
        this.directors = mDirectors;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(@NonNull List<Actor> mActors) {
        this.actors = mActors;
    }
}
