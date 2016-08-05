package com.example.aliaksandrmirashnichenka.mediaplayer.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by amirashnichenka on 8/2/2016.
 */
@SuppressWarnings("ALL")
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

    public static Meta convertJsonToObject(JSONObject jsonObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), Meta.class);
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int mYear) {
        this.releaseYear = mYear;
    }

    public List<Director> getDirectors() {
        return directors;
    }

    public void setDirectors(List<Director> mDirectors) {
        this.directors = mDirectors;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> mActors) {
        this.actors = mActors;
    }
}
