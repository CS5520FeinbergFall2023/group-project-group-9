package edu.northeastern.stage.API;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import edu.northeastern.stage.R;

public class SpotifyAPIExample extends AppCompatActivity {

    Button searchArtistBT;
    Button searchTrackBT;
    Spotify spotify = new Spotify();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_api_example);

        searchArtistBT = findViewById(R.id.searchArtistBT);
        searchTrackBT = findViewById(R.id.searchTrackBT);

        searchArtistBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CompletableFuture<ArrayList<JsonElement>> artistSearchFuture = spotify.artistSearch("Adele",10);
                artistSearchFuture.thenAccept(searchResult -> {
                    for(JsonElement track : searchResult) {
                        Log.d("ArtistSearch", track.toString());
                    }
                }).exceptionally(e -> {
                    Log.e("ArtistSearchError",e.getMessage());
                    return null;
                });
            }
        });

        searchTrackBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CompletableFuture<ArrayList<JsonElement>> trackSearchFuture = spotify.trackSearch("hello",10);
                trackSearchFuture.thenAccept(searchResult -> {
                    for(JsonElement track : searchResult) {
                        Log.d("TrackSearch", track.toString());
                    }
                }).exceptionally(e -> {
                    Log.e("TrackSearchError",e.getMessage());
                    return null;
                });
            }
        });

    }
}