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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import edu.northeastern.stage.API.Spotify;
import edu.northeastern.stage.model.Circle;
import edu.northeastern.stage.model.music.Album;
import edu.northeastern.stage.model.music.Artist;
import edu.northeastern.stage.model.music.Track;
import edu.northeastern.stage.ui.explore.CircleView;

public class ExploreViewModel extends ViewModel {

    private MutableLiveData<List<JsonObject>> recommendations = new MutableLiveData<>();
    private String track;
    private Spotify spotify = new Spotify();
    private static final Random rand = new Random();
    private String userID;
    CircleView circleView;
    Map<Circle, String> circleTextMap = new HashMap<>();
    List<Circle> circles;
    final private float METER_TO_MILES_CONVERSION = 0.000621371F;

    public Map<String,Integer> getTracksNearby(Integer radius) {

        Location userLocation = new Location("");

        DatabaseReference currentUserReference = FirebaseDatabase.getInstance().getReference("users").child(userID);

        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userLocation.setLatitude(Double.parseDouble(snapshot.child("latitude").getKey()));
                userLocation.setLongitude(Double.parseDouble(snapshot.child("longitude").getKey()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Map<String,Integer> tracksFrequency = new HashMap<>();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = mDatabase.getReference("users");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if(isWithinDistance(Double.parseDouble(userSnapshot.child("location").child("latitude").getKey()),
                            Double.parseDouble(userSnapshot.child("location").child("longitude").getKey()),
                            userLocation.getLatitude(), userLocation.getLongitude(),radius)) {
                        for (DataSnapshot trackSnapshot : userSnapshot.child("posts").getChildren()) {
                            String trackID = trackSnapshot.child("trackID").getValue(String.class);
                            if(tracksFrequency.containsKey(trackID)) {
                                tracksFrequency.put(trackID, tracksFrequency.get(trackID) + 1);
                            } else {
                                tracksFrequency.put(trackID,1);
                            }
                        }
                    }
                }
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

    public void setCircles(CircleView circleView) {
        // Process the circles as needed in the ViewModel
        this.circleView = circleView;
        createCircles();
    }

    public List<Circle> createCircles() {
        circles = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = 100000; // Limit the number of attempts to avoid infinite loop
        int MIN_DISTANCE_THRESHOLD = 10;

        while (circles.size() < 20 && attempts < maxAttempts) {
            float x = rand.nextFloat() * 2000 - 1000; //-1000 to 1000
            float y = rand.nextFloat() * 2000 - 1000;
            float radius = rand.nextFloat() * 200 + 100;

            // Ensure the newly created circle doesn't overlap with existing circles
            boolean isOverlapping = false;
            for (Circle existingCircle : circles) {
                float distance = calculateDistance(x, y, existingCircle.getX(), existingCircle.getY());
                // add min_distance_threshold so they are bit further away from each other
                float minDistance = radius + existingCircle.getRadius() + MIN_DISTANCE_THRESHOLD;
                if (distance < minDistance) {
                    isOverlapping = true;
                    break; // This circle overlaps, generate a new one
                }
            }

            if (!isOverlapping) {
                circles.add(new Circle(x, y, radius));
            }

            attempts++;
        }

        generateCircleTexts();

        // Set the circles to the existing CircleView
        if (circleView != null) {
            circleView.setCircles(circles, (HashMap<Circle, String>) circleTextMap);
            circleView.invalidate(); // Request a redraw
        }
        return circles;
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        //euclidean distance
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void generateCircleTexts(){
        String textInCircle;
        for(Circle c: circles){
            textInCircle = generateRandomText();
            // Store in map
            circleTextMap.put(c, textInCircle);
        }
    }

    // Function to generate random text
    public String generateRandomText() {
        // Replace this with your own logic to generate random text
        String[] texts = {"Text1", "Text2", "Text3", "Text4", "Text5"};
        int randomIndex = new Random().nextInt(texts.length);
        return texts[randomIndex];
    }

}
