package edu.northeastern.stage.API;

import java.util.ArrayList;

public class Post {

    private String trackID;
    private String content;
    private String user;
    private Long timestamp;
    private ArrayList<String> likes;

    public Post(String trackID, String content, String user, Long timestamp, ArrayList<String> likes) {
        this.trackID = trackID;
        this.content = content;
        this.user = user;
        this.timestamp = timestamp;
        this.likes = likes;
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

    public ArrayList<String> getLikes() {
        return likes;
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

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }
}
