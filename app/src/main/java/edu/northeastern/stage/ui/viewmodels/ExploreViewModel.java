package edu.northeastern.stage.ui.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import edu.northeastern.stage.model.Circle;
import edu.northeastern.stage.R;
import edu.northeastern.stage.ui.explore.CircleView;

public class ExploreViewModel extends AndroidViewModel {

    private MutableLiveData<List<String>> recommendations = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> selectedSong = new MutableLiveData<>();
//    private edu.northeastern.stage.ui.viewmodels.Explore_Review_SharedViewModel sharedViewModel;

    private static final Random rand = new Random();
    CircleView circleView;
    Map<Circle, String> circleTextMap = new HashMap<>();
    List<Circle> circles;



    public ExploreViewModel(Application application) {
        super(application);
//        sharedViewModel = new ViewModelProvider(this).get(edu.northeastern.stage.ui.viewmodels.Explore_Review_SharedViewModel.class);
    }

    public LiveData<List<String>> getRecommendations() {
        return recommendations;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void searchTextChanged(String text) {
        if (text.isEmpty()) {
            recommendations.setValue(new ArrayList<>());
        } else {
            Log.d("Explore View Model", "when searchTextChanged");
            makeDeezerReq(text);
        }
    }

//    public void songSelected(String song) {
//        selectedSong.setValue(song);
////        sharedViewModel.setSong(song);
//    }
//
//    public LiveData<String> getSelectedSong() {
//        return selectedSong;
//    }

    public void makeDeezerReq(String inputArtistName) {
        Log.d("Explore View Model", "makeDeezerReq");

        String deezerApiKey = getApplication().getString(R.string.DEEZER_API);
        isLoading.setValue(true);

        new Thread(() -> {
            try {
                URL url = new URL(getApplication().getString(R.string.DEEZER_BASE_URL) + inputArtistName);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("X-RapidAPI-Key", deezerApiKey);
                urlConnection.setRequestProperty("X-RapidAPI-Host", "deezerdevs-deezer.p.rapidapi.com");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                String jsonString = convertStreamToString(inputStream);
                parseJsonAndUpdate(jsonString);

            } catch (Exception e) {
                e.printStackTrace();
                recommendations.postValue(new ArrayList<>());
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }


    private void parseJsonAndUpdate(String jsonString) {
        try {
            JSONObject response = new JSONObject(jsonString);
            JSONArray data = response.getJSONArray("data");
            List<String> tempRecs = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject currentResult = data.getJSONObject(i);
                JSONObject artist = currentResult.getJSONObject("artist");
                String trackName = currentResult.getString("title");
                String artistName = artist.getString("name");
                tempRecs.add(trackName + " by " + artistName);
            }
            recommendations.postValue(tempRecs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
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

        while (circles.size() < 100 && attempts < maxAttempts) {
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
