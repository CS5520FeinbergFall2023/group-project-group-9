package edu.northeastern.stage.ui.explore;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.northeastern.stage.MainActivity;
import edu.northeastern.stage.databinding.FragmentExploreBinding;
import edu.northeastern.stage.model.music.Track;
import edu.northeastern.stage.ui.adapters.TrackSearchAdapter;
import edu.northeastern.stage.ui.viewmodels.ExploreViewModel;
import edu.northeastern.stage.ui.viewmodels.SharedDataViewModel;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private ExploreViewModel viewModel;
    private JsonObject selectedTrack;
    private AutoCompleteTextView actv;
    private Button buttonToMusicReview;
    private CircleView circleView;
    private SeekBar geoSlider;
    private TextView progressTextView;
    private SharedDataViewModel sharedDataViewModel;
    private int currentMileRadius; // current value selected on slider bar.
    private TrackSearchAdapter searchAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // instantiate views
        buttonToMusicReview = binding.reviewButton;
        circleView = binding.circleView;
        actv = binding.autoCompleteTextView;
        actv.setThreshold(1);
        geoSlider = binding.locationSeekBar;
        progressTextView = binding.textView;

        // set up view models to share data
        sharedDataViewModel = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        // set current user
        sharedDataViewModel.getUserID().observe(getViewLifecycleOwner(), userID -> {
            if (userID != null) {
                viewModel.setUserID(userID);
            }
        });

        //setup search
        setupSearch();

        // button to music review page
        buttonToMusicReview.setOnClickListener(v -> {
            if(!actv.getText().toString().isEmpty()) {
                actv.setText("");
                ((MainActivity)requireActivity()).navigateToFragment("MUSIC_REVIEW_FRAGMENT", true, null);
            }
        });

        // if trackReview is not empty, enable button
        sharedDataViewModel.getTrackReview().observe(getViewLifecycleOwner(), track -> {
            if (track != null) {
                buttonToMusicReview.setEnabled(true);
            }
        });

        viewModel.setCircles(circleView);

        // perform seek bar change listener event used for getting the progress value
        geoSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentMileRadius = progress;
                String progressText = currentMileRadius + " miles";
                progressTextView.setText(progressText);

                int width = geoSlider.getWidth() - geoSlider.getPaddingLeft() - geoSlider.getPaddingRight();
                int thumbPos = geoSlider.getPaddingLeft() + width * geoSlider.getProgress() / geoSlider.getMax();

                progressTextView.measure(0, 0);
                int txtW = progressTextView.getMeasuredWidth();
                int delta = txtW / 2;
                progressTextView.setX(geoSlider.getX() + thumbPos - delta);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

                viewModel.getTracksNearby(currentMileRadius).observe(getViewLifecycleOwner(),tracksFrequency -> {
                    Log.d("ABC123","Current mile radius: " + String.valueOf(currentMileRadius));
                    Log.d("ABC123","Tracks found - " + tracksFrequency.toString());
                    Log.d("ABC123","Tracks found - " + tracksFrequency.size());
                    Log.d("ABC123","Tracks found - " + tracksFrequency.get("6qAcApH8obo8eqatCKUHd9"));

//                    Set<String> a= tracksFrequency.keySet();
//                    a.getClass()
                    Integer trackSize = tracksFrequency.size();
                    for (String key : tracksFrequency.keySet()) {
                        Integer value = tracksFrequency.get(key);
                        Log.d("ABC123", "key/value : " + key + " / " + value);

                        viewModel.getTrackFromId(key, trackSize).observe(getViewLifecycleOwner(), allTrackObjects -> {

                            if(allTrackObjects.size() == tracksFrequency.size()){
                                Log.d("ABC123", "ALL TRACK OBJECTS RECEIVED " + allTrackObjects);

                                circleView = binding.circleView;
                                // Get keySet
                                Set<String> keySet = tracksFrequency.keySet();

                                // Convert to list
                                List<String> keyList = new ArrayList<>(keySet);
                                viewModel.setCirclesWithTracks(keyList, binding.circleView);

                                binding.circleView.setCircleClickListener(clickedTrack -> {
                                    viewModel.setClickedTrack(clickedTrack);
                                    Track trackToStore = viewModel.createTrack(clickedTrack); // create track in view model
                                    sharedDataViewModel.setTrackReview(trackToStore); // set track in shared data view model
                                    ((MainActivity)requireActivity()).navigateToFragment("MUSIC_REVIEW_FRAGMENT", true, null);
                                });
                            }
                        });
                    }
                });

            }
        });

        return root;
    }

    private void setupSearch() {
        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                buttonToMusicReview.setEnabled(false);
                if(s.length() == 0){
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if(s.length() != 0) {
                        viewModel.performSearch(s.toString())
                                .observe(getViewLifecycleOwner(), searchResults -> {
                                    ArrayList<JsonObject> results = new ArrayList<>();

                                    for (int i = 0; i < searchResults.size(); i++) {
                                        results.add(searchResults.get(i).getAsJsonObject());
                                    }
                                    searchAdapter = new TrackSearchAdapter(getContext(),results);
                                    actv.setAdapter(searchAdapter);
                                    searchAdapter.notifyDataSetChanged();
                                });
                    }
                } catch (Exception e) {
                    Log.e("ExploreFragment", "afterTextChanged - Error performing search", e);
                }
            }
        });

        actv.setOnItemClickListener((parent, view, position, id) -> {
            selectedTrack = searchAdapter.getItem(position);
            if (selectedTrack != null) {
                String artists = "";
                try {
                    Log.d("ExploreFragment", "onItemClick - Selected track: " + selectedTrack);
                    JsonArray artistsArray = selectedTrack.getAsJsonArray("artists");
                    if (artistsArray != null && artistsArray.size() > 0) {
                        for (JsonElement artist : artistsArray) {
                            artists = artists + artist.getAsJsonObject().get("name").getAsString() + " ";
                        }
                    }
                    actv.setText(selectedTrack.get("name").getAsString() + " by " + artists);
                    Track trackToStore = viewModel.createTrack(selectedTrack); // create track in view model
                    sharedDataViewModel.setTrackReview(trackToStore); // set track in shared data view model
                    buttonToMusicReview.setEnabled(true);
                } catch (Exception e) {
                    Log.e("ExploreFragment", "onItemClick - Error processing selected track", e);
                }
            }
        });
    }
}