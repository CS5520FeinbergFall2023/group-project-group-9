package edu.northeastern.stage.Model;

public class Post {
    private String userAvatarUrl;
    private String musicLink;
    private String postContent;
    private boolean isLiked;
    private String visibilityState;

    public Post(String userAvatarUrl, String musicLink, String postContent, boolean isLiked, String visibilityState) {
        this.userAvatarUrl = userAvatarUrl;
        this.musicLink = musicLink;
        this.postContent = postContent;
        this.isLiked = isLiked;
        this.visibilityState = visibilityState;
    }

    // Getters and Setters
    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getMusicLink() {
        return musicLink;
    }

    public void setMusicLink(String musicLink) {
        this.musicLink = musicLink;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getVisibilityState() {
        return visibilityState;
    }

    public void setVisibilityState(String visibilityState) {
        this.visibilityState = visibilityState;
    }
}
