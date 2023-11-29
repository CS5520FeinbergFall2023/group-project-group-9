package edu.northeastern.stage.API;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Spotify {

    private static final String CLIENT_ID = "cdbc9d16da944c8dad47d543ed0741b2"; // store this somewhere else
    private static final String CLIENT_SECRET = "d418779f7c994c6683bf27b1a35f141b"; // store this somewhere else
    private String accessToken = "";
    private long tokenExpirationTime = 0;

    public Spotify() {
        checkAccessToken();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getTokenExpirationTime() {
        return tokenExpirationTime;
    }

    // need to run this method at the start of each method
    private void checkAccessToken() {
        if (isTokenExpired()) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();

                    String credentials = Credentials.basic(CLIENT_ID, CLIENT_SECRET);

                    RequestBody requestBody = new FormBody.Builder()
                            .add("grant_type", "client_credentials")
                            .build();

                    Request request = new Request.Builder()
                            .url("https://accounts.spotify.com/api/token") // store this value somewhere else
                            .addHeader("Authorization", credentials)
                            .post(requestBody)
                            .build();

                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            final String responseBody = response.body().string();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    accessToken = parseAccessToken(responseBody);
                                    tokenExpirationTime = calculateTokenExpirationTime(responseBody);
                                    // Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // unsuccessful response
                            Log.e("RequestError", "Unsuccessful response: " + response.code());
                        }
                    } catch (IOException e) {
                        // handle IO exception
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            // Toast.makeText(context, "Token already exists: " + accessToken, Toast.LENGTH_SHORT).show();
        }
    }

    public CompletableFuture<ArrayList<JsonElement>> artistSearch(final String artist, Integer numResults) {
        checkAccessToken();
        CompletableFuture<ArrayList<JsonElement>> future = new CompletableFuture<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                String url = "https://api.spotify.com/v1/search?q=" + artist + "&type=artist&limit=" + numResults.toString();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        final ArrayList<JsonElement> artists = handleArtistSearchResults(responseBody);
                        future.complete(artists);
                    } else {
                        // unsuccessful response
                        Log.e("SearchError", "Unsuccessful search response: " + response.code());
                        final String errorMessage = "Unsuccessful search response: " + response.code();
                        future.completeExceptionally(new RuntimeException(errorMessage));
                    }
                } catch (IOException e) {
                    // handle IO exception
                    e.printStackTrace();
                    future.completeExceptionally(e);
                }
            }
        }).start();
        return future;
    }

    private ArrayList<JsonElement> handleArtistSearchResults(String responseBody) {

        ArrayList<JsonElement> artists = new ArrayList<JsonElement>();

        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("artists") && json.getAsJsonObject("artists").has("items")) {
                JsonArray items = json.getAsJsonObject("artists").getAsJsonArray("items");
                if (items.size() > 0) {
                    for(int i = 0; i< items.size();i++) {
                        artists.add(items.get(i));
                    }
                } else {
                    // No tracks found
                    // Toast.makeText(context, "No artists found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Unexpected response format
                Log.e("SearchError", "Unexpected search response format");
            }
        } catch (JsonParseException e) {
            // JSON parsing exception
            Log.e("SearchError", "JSON parsing exception");
        }
        return artists;
    }

    public CompletableFuture<ArrayList<JsonElement>> trackSearch(final String track, Integer numResults) {
        checkAccessToken();
        CompletableFuture<ArrayList<JsonElement>> future = new CompletableFuture<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                String url = "https://api.spotify.com/v1/search?q=" + track + "&type=track&limit=" + numResults.toString();
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        final ArrayList<JsonElement> tracks = handleTrackSearchResults(responseBody);
                        future.complete(tracks);
                    } else {
                        // unsuccessful response
                        Log.e("SearchError", "Unsuccessful search response: " + response.code());
                        final String errorMessage = "Unsuccessful search response: " + response.code();
                        future.completeExceptionally(new RuntimeException(errorMessage));
                    }
                } catch (IOException e) {
                    // handle IO exception
                    e.printStackTrace();
                    future.completeExceptionally(e);
                }
            }
        }).start();
        return future;
    }

    private ArrayList<JsonElement> handleTrackSearchResults(String responseBody) {

        ArrayList<JsonElement> tracks = new ArrayList<JsonElement>();

        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("tracks") && json.getAsJsonObject("tracks").has("items")) {
                JsonArray items = json.getAsJsonObject("tracks").getAsJsonArray("items");
                if (items.size() > 0) {
                    for(int i = 0; i< items.size();i++) {
                        tracks.add(items.get(i));
                    }
                } else {
                    // No tracks found
                    // Toast.makeText(context, "No tracks found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Unexpected response format
                Log.e("SearchError", "Unexpected search response format");
            }
        } catch (JsonParseException e) {
            // JSON parsing exception
            Log.e("SearchError", "JSON parsing exception");
        }
        return tracks;
    }

    private static String parseAccessToken(String responseBody) {
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if(json.has("access_token")) {
                return json.get("access_token").getAsString();
            } else {
                // if token is not present
                return null;
            }
        } catch (JsonParseException e) {
            // JSON parsing exception
            return null;
        }
    }

    private long calculateTokenExpirationTime(String responseBody) {
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("expires_in")) {
                // Assuming the response contains the expiration time in seconds
                int expiresIn = json.get("expires_in").getAsInt();
                return Calendar.getInstance().getTimeInMillis() + expiresIn * 1000;
            }
        } catch (JsonParseException e) {
            // JSON parsing exception
            e.printStackTrace();
        }
        return 0;
    }

    private boolean isTokenExpired() {
        return tokenExpirationTime == 0 || tokenExpirationTime < Calendar.getInstance().getTimeInMillis();
    }

    private void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
