package edu.northeastern.stage.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonObject;

import edu.northeastern.stage.model.music.Track;

public class SharedDataViewModel extends ViewModel {
    private MutableLiveData<String> userID = new MutableLiveData<>();
    private MutableLiveData<JsonObject> trackJson = new MutableLiveData<>();
    private MutableLiveData<Track> trackPost = new MutableLiveData<>();
    private MutableLiveData<Track> trackReview = new MutableLiveData<>();

    public void setUserID(String userID) {
        this.userID.setValue(userID);
    }

    public LiveData<String> getUserID() {
        return userID;
    }

    public void setTrackJson(JsonObject track) {
        this.trackJson.setValue(track);
    }

    public LiveData<JsonObject> getTrackJson() {
        return trackJson;
    }

    public void setTrackPost(Track track) {
        this.trackPost.setValue(track);
    }

    public LiveData<Track> getTrackPost() {
        return trackPost;
    }

    public void setTrackReview(Track track) {
        this.trackReview.setValue(track);
    }

    public LiveData<Track> getTrackReview() {
        return trackReview;
    }
}
