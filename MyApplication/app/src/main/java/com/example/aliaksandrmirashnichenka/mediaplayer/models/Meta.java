package com.example.aliaksandrmirashnichenka.mediaplayer.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by amirashnichenka on 8/2/2016.
 */
public class Meta implements Serializable {

    @JsonProperty("releaseYear")
    private int mYear;

    @JsonProperty("directors")
    private List<Director> mDirectors;

    @JsonProperty("actors")
    private List<Actor> mActors;

    private Meta(){}

    public static Meta convertJsonToObject(JSONObject jsonObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonObject.toString(), Meta.class);
    }

    public int getYear() {
        return mYear;
    }

    public void setYear(int mYear) {
        this.mYear = mYear;
    }

    public List<Director> getDirectors() {
        return mDirectors;
    }

    public void setDirectors(List<Director> mDirectors) {
        this.mDirectors = mDirectors;
    }

    public List<Actor> getActors() {
        return mActors;
    }

    public void setActors(List<Actor> mActors) {
        this.mActors = mActors;
    }
}
