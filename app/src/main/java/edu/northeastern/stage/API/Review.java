package edu.northeastern.stage.API;

import java.util.ArrayList;

public class Review {

    private String trackID;
    private String content;
    private String user;
    private Long timestamp;
    private Integer rating;

    public Review(String trackID, String content, String user, Long timestamp, Integer rating) {
        this.trackID = trackID;
        this.content = content;
        this.user = user;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    public String getTrackID() {
        return trackID;
    }

    public String getContent() {
        return content;
    }

    public String getUser() {
        return user;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getRating() {
        return rating;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
