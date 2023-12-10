package edu.northeastern.stage.ui.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import edu.northeastern.stage.API.Spotify;
import edu.northeastern.stage.model.Post;
import edu.northeastern.stage.model.music.Album;
import edu.northeastern.stage.model.music.Artist;
import edu.northeastern.stage.model.music.Track;

public class NewPostViewModel extends ViewModel {
    // LiveData for observing post submission status
    private final MutableLiveData<Boolean> postSubmissionStatus = new MutableLiveData<>();
    // Initiate Spotify object
    private Spotify spotify = new Spotify();
    // Initialize User ID
    private String userID = "";
    private Track track;

    // Method to handle post submission logic
    public void createPost(String postContent, String visibilityState) {
        // get instance of Firebase DB
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        if (getUserID() != null) {
            // get reference to DB
            DatabaseReference reference = mDatabase
                    .getReference("users")
                    .child(getUserID())
                    .child("posts");

            // generate unique ID for post
            DatabaseReference newPostRef = reference.push();

            if (track != null && !postContent.isEmpty()) {
                String trackName = track.getName();
                String trackID = track.getId();
                String artistName = "";
                String content = postContent;
                Long timestamp = System.currentTimeMillis();
                String imageURL = "";
                String spotifyURL = track.getSpotifyUrl();

                // get all artists
                if (track.getArtists().size() > 0) {
                    for (Artist artist : track.getArtists()) {
                        artistName = artistName + artist.getName() + " ";
                    }
                    artistName = artistName.trim();
                }

                // get image URL from album
                if (track.getAlbum() != null) {
                    if (track.getAlbum().getImageURL() != null) {
                        imageURL = track.getAlbum().getImageURL();
                    }
                }

                // Set Post object
                Post post = new Post(newPostRef.getKey(), getUserID(),trackName, trackID, artistName, content, timestamp, imageURL, visibilityState, spotifyURL);
                newPostRef.setValue(post, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            Log.d("NewPost", "New post created!");
                        } else {
                            Log.d("NewPost", "New post created failed!");
                        }
                    }
                });
            }
        }
        // Update the LiveData with the submission status
        // TODO: what is the purpose of this?
        postSubmissionStatus.setValue(true); // Or false if submission fails
    }

    // Method to handle search logic
    public LiveData<List<JsonObject>> performSearch(String query) {
        MutableLiveData<List<JsonObject>> searchResults = new MutableLiveData<>();

        // change numResults
        CompletableFuture<ArrayList<JsonObject>> trackSearchFuture = spotify.trackSearch(query, 4);
        trackSearchFuture.thenAccept(searchResult -> {
            searchResults.postValue(searchResult);
        }).exceptionally(e -> {
            Log.e("TrackSearchError", e.getMessage());
            return null;
        });
        return searchResults;
    }

    public Track createTrack(JsonObject selectedTrack) {
        // album variables
        String albumURL = selectedTrack.get("album").getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();
        String albumID = selectedTrack.get("album").getAsJsonObject().get("id").getAsString();
        String albumImageURL = selectedTrack.get("album").getAsJsonObject().getAsJsonArray("images").get(0).getAsJsonObject().get("url").getAsString();
        String albumName = selectedTrack.get("album").getAsJsonObject().get("name").getAsString();
        String albumReleaseDate = selectedTrack.get("album").getAsJsonObject().get("release_date").getAsString();
        String albumReleaseDatePrecision = selectedTrack.get("album").getAsJsonObject().get("release_date_precision").getAsString();
        JsonArray albumArtistsJsonArray = selectedTrack.get("album").getAsJsonObject().getAsJsonArray("artists");
        ArrayList<Artist> albumArtists = new ArrayList<Artist>();
        for(JsonElement artist : albumArtistsJsonArray) {
            Artist artistToAdd = new Artist(artist.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString(),
                    artist.getAsJsonObject().get("id").getAsString(),artist.getAsJsonObject().get("name").getAsString());
            albumArtists.add(artistToAdd);
        }

        // track variables
        Album album = new Album(albumURL, albumID, albumImageURL, albumName, albumReleaseDate, albumReleaseDatePrecision, albumArtists);
        JsonArray trackArtistsJsonArray = selectedTrack.getAsJsonArray("artists");
        ArrayList<Artist> trackArtists = new ArrayList<Artist>();
        for(JsonElement artist : trackArtistsJsonArray) {
            Artist artistToAdd = new Artist(artist.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString(),
                    artist.getAsJsonObject().get("id").getAsString(),artist.getAsJsonObject().get("name").getAsString());
            trackArtists.add(artistToAdd);
        }
        int durationMs = selectedTrack.get("duration_ms").getAsInt();
        String spotifyURL = selectedTrack.get("external_urls").getAsJsonObject().get("spotify").getAsString();
        String trackID = selectedTrack.get("id").getAsString();
        String trackName = selectedTrack.get("name").getAsString();
        int popularity = selectedTrack.get("popularity").getAsInt();
        return new Track(album,trackArtists,durationMs,spotifyURL,trackID,trackName,popularity);
    }

    public LiveData<Boolean> getPostSubmissionStatus() {
        return postSubmissionStatus;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}