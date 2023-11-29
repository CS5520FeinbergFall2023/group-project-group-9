package edu.northeastern.stage.UI.newPost;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import edu.northeastern.stage.API.Spotify;

public class NewPostViewModel extends ViewModel {
    // LiveData for observing post submission status
    private final MutableLiveData<Boolean> postSubmissionStatus = new MutableLiveData<>();
    private Spotify spotify = new Spotify();

    // Method to handle post submission logic
    // When calling in NewPostFragment, need to pass in Post object with all fields
    public void submitPost(Post post) {
        createPost(post);
        postSubmissionStatus.setValue(true); // true if successful, false otherwise
    }

    // Method to handle search logic
    // in NewPostFragment, need to handle JsonElement by getting key/value pairs
    public LiveData<List<JsonElement>> performSearch(String query) {
        MutableLiveData<List<JsonElement>> searchResults = new MutableLiveData<>();

        // change numResults
        CompletableFuture<ArrayList<JsonElement>> trackSearchFuture = spotify.trackSearch(query,10);
        trackSearchFuture.thenAccept(searchResult -> {
            searchResults.postValue(searchResult);
        }).exceptionally(e -> {
            Log.e("TrackSearchError",e.getMessage());
            return null;
        });
        return searchResults;
    }

    // Getters for LiveData
    public LiveData<Boolean> getPostSubmissionStatus() {
        return postSubmissionStatus;
    }

    // TODO: edit definition of Post Model
    private void createPost(Post post) {
        // get instance of FBDB
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        // get reference to DB
        DatabaseReference reference = mDatabase
                .getReference("users")
                .child()
                .child("posts");

        // generate unique ID for post
        DatabaseReference newPostRef = reference.push();

        // add fields to post
        newPostRef.setValue(post, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error != null) {
                    Log.d("NewPost","New post created!");
                } else {
                    Log.d("NewPost","New post created failed!");
                }
            }
        });
    }
}