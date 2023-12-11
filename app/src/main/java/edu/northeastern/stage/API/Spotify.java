package edu.northeastern.stage.API;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class defines methods that interacts with the Spotify public API
 */
public class Spotify {

    // TODO: store CLIENT_ID and CLIENT_SECRET in FBDB
    private static final String TAG = "Spotify";
//    private static final String CLIENT_ID = "cdbc9d16da944c8dad47d543ed0741b2"; // store this somewhere else
//    private static final String CLIENT_SECRET = "d418779f7c994c6683bf27b1a35f141b"; // store this somewhere else
    private static final String CLIENT_ID = "dac2479754b6411b8c911effe51ddf38"; // store this somewhere else
    private static final String CLIENT_SECRET = "f7a878a61ad944eebfc847acfd099548"; // store this somewhere else
    private String accessToken = ""; // set default value
    private long tokenExpirationTime = 0; // set default value

    /**
     * Constructor confirms access token validity
     */
    public Spotify() {
        checkAccessToken();
    }

    /**
     * Getter for access token
     * @return - access token as String
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Getter for token expiration time
     * @return - expiration time as long
     */
    public long getTokenExpirationTime() {
        return tokenExpirationTime;
    }

    /**
     * Method to check if access token is valid or not
     */
    private void checkAccessToken() {
        if (isTokenExpired()) { // if expired then retrieve it again
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
                                    Log.d(TAG,"Successfully retrieved token.");
                                }
                            });
                        } else {
                            // unsuccessful response
                            Log.e("TAG", "Unsuccessful response: " + response.code());
                        }
                    } catch (IOException e) {
                        // handle IO exception
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Log.d(TAG,"Token already exists: " + accessToken);
        }
    }

    /**
     * Method to asynchronously search for all artists that match
     * @param artist - artist to search as string
     * @param numResults - number of results to retrieve as integer
     * @return - CompletableFuture of ArrayList of JsonObject used for asynchronous API calls
     */
    public CompletableFuture<ArrayList<JsonObject>> artistSearch(final String artist, Integer numResults) {
        checkAccessToken();

        CompletableFuture<ArrayList<JsonObject>> future = new CompletableFuture<>();

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.spotify.com/v1/search?q=" + artist + "&type=artist&limit=" + numResults.toString();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        Log.d(TAG,"Successful artist search");
                        final ArrayList<JsonObject> tracks = handleTrackSearchResults(responseBody);
                        future.complete(tracks);
                    } else {
                        // unsuccessful response
                        Log.e(TAG, "Unsuccessful artist search response: " + response.code());
                        final String errorMessage = "Unsuccessful search response: " + response.code();
                        future.completeExceptionally(new RuntimeException(errorMessage));
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // handle failure
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Method to asynchronously retrieve track JsonObject by specific trackID
     * @param trackID - trackID to search for
     * @return - CompletableFuture of JsonObject used for asynchronous API calls
     */
    public CompletableFuture<JsonObject> trackSearchByID(final String trackID) {
        checkAccessToken();

        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.spotify.com/v1/tracks/" + trackID;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        Log.d(TAG,"Successful search by trackID");
                        JsonObject trackJsonObject = handleSingleTrackSearchResult(responseBody);
                        future.complete(trackJsonObject);
                    } else {
                        // unsuccessful response
                        Log.e(TAG, "Unsuccessful trackID search response: " + response.code());
                        final String errorMessage = "Unsuccessful search response: " + response.code();
                        future.completeExceptionally(new RuntimeException(errorMessage));
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // handle failure
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Method to asynchronously search for all tracks that match
     * @param track - track to search as string
     * @param numResults - number of results to retrieve as integer
     * @return - CompletableFuture of ArrayList of JsonObject used for asynchronous API calls
     */
    public CompletableFuture<ArrayList<JsonObject>> trackSearch(final String track, Integer numResults) {
        checkAccessToken();
        CompletableFuture<ArrayList<JsonObject>> future = new CompletableFuture<>();

        OkHttpClient client = new OkHttpClient();
        String url = "https://api.spotify.com/v1/search?q=" + track + "&type=track&limit=" + numResults.toString();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        Log.d("SearchSuccess","Successful search");
                        final ArrayList<JsonObject> tracks = handleTrackSearchResults(responseBody);
                        future.complete(tracks);
                    } else {
                        // unsuccessful response
                        Log.e("SearchError", "Unsuccessful search response: " + response.code());
                        final String errorMessage = "Unsuccessful search response: " + response.code();
                        future.completeExceptionally(new RuntimeException(errorMessage));
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // handle failure
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Helper method for handling JsonObject
     * @param responseBody - entire response body from API call as string
     * @return - JsonObject of single track searched
     */
    private JsonObject handleSingleTrackSearchResult(String responseBody) {
        try {
            return new Gson().fromJson(responseBody, JsonObject.class);
        } catch (JsonParseException e) {
            // Handle JSON parsing exception
            Log.e("SearchError", "JSON parsing exception");
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for handling artists search results
     * @param responseBody - entire response body as string
     * @return - ArrayList of JsonObject
     */
    private ArrayList<JsonObject> handleArtistSearchResults(String responseBody) {

        ArrayList<JsonObject> artists = new ArrayList<JsonObject>();

        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("artists") && json.getAsJsonObject("artists").has("items")) {
                JsonArray items = json.getAsJsonObject("artists").getAsJsonArray("items");
                if (items.size() > 0) {
                    for(int i = 0; i< items.size();i++) {
                        artists.add(items.get(i).getAsJsonObject());
                    }
                } else {
                    // No artists found
                    Log.e("SearchError", "No artists found");
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

    /**
     * Helper method for handling track search results
     * @param responseBody - entire response body as string
     * @return - ArrayList of JsonObject
     */
    private ArrayList<JsonObject> handleTrackSearchResults(String responseBody) {

        ArrayList<JsonObject> tracks = new ArrayList<JsonObject>();

        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("tracks") && json.getAsJsonObject("tracks").has("items")) {
                JsonArray items = json.getAsJsonObject("tracks").getAsJsonArray("items");
                if (items.size() > 0) {
                    for(int i = 0; i< items.size();i++) {
                        tracks.add(items.get(i).getAsJsonObject());
                    }
                } else {
                    // No tracks found
                    Log.e("SearchError", "No tracks found");
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

    /**
     * Helper method to parse the access token response
     * @param responseBody - response body as String
     * @return - access token as String
     */
    private static String parseAccessToken(String responseBody) {
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if(json.has("access_token")) {
                return json.get("access_token").getAsString();
            } else {
                Log.d(TAG,"Access token not found in API response!");
                return null;
            }
        } catch (JsonParseException e) {
            Log.e(TAG,"Exception occurred while parsing access token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to calculate token expiration time
     * @param responseBody - response body contains expiration time
     * @return - expiration time as long
     */
    private long calculateTokenExpirationTime(String responseBody) {
        try {
            JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
            if (json.has("expires_in")) {
                int expiresIn = json.get("expires_in").getAsInt();
                Log.d(TAG,"Access token expiration time found: " + String.valueOf(expiresIn));
                return Calendar.getInstance().getTimeInMillis() + expiresIn * 1000;
            }
        } catch (JsonParseException e) {
            Log.e(TAG,"Exception occurred while parsing access token expiration time: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Helper method to check if token is expired
     * @return - true or false as boolean
     */
    private boolean isTokenExpired() {
        return tokenExpirationTime == 0 || tokenExpirationTime < Calendar.getInstance().getTimeInMillis();
    }

    /**
     * Helper method to define Handler for asynchronous threads
     * @param runnable - runnable as Runnable
     */
    private void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
