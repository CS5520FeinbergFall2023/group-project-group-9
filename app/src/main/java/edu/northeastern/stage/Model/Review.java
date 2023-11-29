package edu.northeastern.stage.Model;

import androidx.annotation.NonNull;

public class Review {
    private String userId;
    private String avatarUri;
    private String content;
    private float rating;

    // Constructor
    public Review(String userId, String avatarUri, String content, float rating) {
        this.userId = userId;
        this.avatarUri = avatarUri;
        this.content = content;
        this.rating = rating;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    // toString() method for easy printing of Review object data
    @NonNull
    @Override
    public String toString() {
        return "Review{" +
                "userId='" + userId + '\'' +
                ", avatarUri='" + avatarUri + '\'' +
                ", content='" + content + '\'' +
                ", rating=" + rating +
                '}';
    }
}

