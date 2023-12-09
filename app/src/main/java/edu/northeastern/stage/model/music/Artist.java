package edu.northeastern.stage.model.music;

import java.util.ArrayList;

public class Artist {
    private String spotifyUrl; // from external_urls
    private String id;
    private String name;

    public Artist(String spotifyUrl, String id, String name) {
        this.spotifyUrl = spotifyUrl;
        this.id = id;
        this.name = name;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        this.spotifyUrl = spotifyUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
