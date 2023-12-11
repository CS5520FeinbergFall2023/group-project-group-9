package edu.northeastern.stage.ui.viewmodels;

import android.util.Log;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import edu.northeastern.stage.API.Spotify;
import edu.northeastern.stage.model.music.Album;
import edu.northeastern.stage.model.music.Artist;
import edu.northeastern.stage.model.music.PopularityTrack;
import edu.northeastern.stage.model.music.Track;

public class ExploreViewModel extends ViewModel {

    private MutableLiveData<List<JsonObject>> recommendations = new MutableLiveData<>();
    private MutableLiveData<Map<String,Integer>> tracksFrequency = new MutableLiveData<>();
    private ArrayList<PopularityTrack> exploreTracks = new ArrayList<>();
    private String track;
    private Spotify spotify = new Spotify();
    private String userID;
    final private float METER_TO_MILES_CONVERSION = 0.000621371F;

    // TODO: reset exploreTracks whenever there is a change to location bar
    public CompletableFuture<Void> searchTrack(String trackID) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        CompletableFuture<JsonObject> trackSearchFuture = spotify.trackSearchByID(trackID);
        trackSearchFuture.thenAccept(searchResult -> {
            String trackName = searchResult.get("name").getAsString();
            String albumImageURL = searchResult.get("album").getAsJsonObject().getAsJsonArray("images").get(0).getAsJsonObject().get("url").getAsString();
            String artistName = "";
            JsonArray artistsArray = searchResult.getAsJsonArray("artists");
            StringBuilder artistsSB = new StringBuilder();
            if (artistsArray != null && !artistsArray.isJsonNull() && artistsArray.size() > 0) {
                Iterator<JsonElement> iterator = artistsArray.iterator();

                while (iterator.hasNext()) {
                    artistsSB.append(iterator.next().getAsJsonObject().get("name").getAsString());

                    if (iterator.hasNext()) {
                        artistsSB.append(", ");
                    }
                }
            }
            artistName = artistsSB.toString();
            PopularityTrack newTrack = new PopularityTrack(trackName, artistName, albumImageURL, tracksFrequency.getValue().get(trackID));
            exploreTracks.add(newTrack);
            future.complete(null); // Complete the CompletableFuture when the operation is finished.
        }).exceptionally(e -> {
            Log.e("TrackSearchError", e.getMessage());
            future.completeExceptionally(e); // Complete exceptionally if an error occurs.
            return null;
        });

        return future;
    }


    public MutableLiveData<Map<String,Integer>> getTracksNearby(Integer radius) {

        Location userLocation = new Location("");
        Map<String,Integer> frequency = new HashMap<>();

        DatabaseReference currentUserReference = FirebaseDatabase.getInstance().getReference("users").child(userID).child("lastLocation");

        // get current user location
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userLocation.setLatitude(Double.parseDouble(snapshot.child("latitude").getValue().toString()));
                userLocation.setLongitude(Double.parseDouble(snapshot.child("longitude").getValue().toString()));

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            if(isWithinDistance(Double.parseDouble(userSnapshot.child("lastLocation").child("latitude").getValue().toString()),
                                    Double.parseDouble(userSnapshot.child("lastLocation").child("longitude").getValue().toString()),
                                    userLocation.getLatitude(), userLocation.getLongitude(),radius)) {
                                for (DataSnapshot trackSnapshot : userSnapshot.child("posts").getChildren()) {
                                    String trackID = trackSnapshot.child("trackID").getValue(String.class);
                                    if(frequency.containsKey(trackID)) {
                                        frequency.put(trackID, frequency.get(trackID) + 1);
                                    } else {
                                        frequency.put(trackID,1);
                                    }
                                }
                            }
                        }
                        tracksFrequency.setValue(frequency);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return tracksFrequency;
    }

    private boolean isWithinDistance(double lat1, double lon1, double lat2, double lon2, Integer radius) {
        float[] results = new float[1];
        Location.distanceBetween(lat1,lon1,lat2,lon2,results);
        if(results[0] * METER_TO_MILES_CONVERSION > radius) {
            return false;
        } else {
            return true;
        }
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public LiveData<List<JsonObject>> performSearch(String query) {
        MutableLiveData<List<JsonObject>> searchResults = new MutableLiveData<>();

        // change numResults
        CompletableFuture<ArrayList<JsonObject>> trackSearchFuture = spotify.trackSearch(query, 4);
        trackSearchFuture.thenAccept(searchResult -> {
            searchResults.postValue(searchResult);
            Log.d("ExploreViewModel", "performSearch - searchResult in trackSearchFuture: " + searchResult.get(0));
        }).exceptionally(e -> {
            Log.e("TrackSearchError", e.getMessage());
            return null;
        });
        return searchResults;
    }

    // method to create Track object based on the selectedTrack JsonObject from Spotify API
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

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public MutableLiveData<List<JsonObject>> getRecommendations() {
        return recommendations;
    }

    public ArrayList<PopularityTrack> getExploreTracks() {
        return exploreTracks;
    }

    public void setExploreTracks(ArrayList<PopularityTrack> exploreTracks) {
        this.exploreTracks = exploreTracks;
    }
}
