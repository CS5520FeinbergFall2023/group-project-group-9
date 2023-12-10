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

import java.util.ArrayList;
import java.util.List;

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
    TrackSearchAdapter searchAdapter;
    private static final int SEARCH_DELAY = 500;
    private long lastSearchTime = 0;

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

        searchAdapter = new TrackSearchAdapter(getContext(), actv);
        //setup search
        setupSearch();

        buttonToMusicReview.setOnClickListener(v -> {
            if(!actv.getText().toString().isEmpty()) {
                actv.setText("");
//                NavController navController = NavHostFragment.findNavController(ExploreFragment.this);
//                navController.navigate(R.id.action_navigation_explore_to_navigation_music_review);
                // Use the manual navigation.
                ((MainActivity)requireActivity()).navigateToFragment("MUSIC_REVIEW_FRAGMENT", true);
            }
        });

        viewModel.setCircles(circleView);

        // in the fragment, when a circle is clicked, get entire track JsonElement by API call
        // then, store this in the shared view model and convert the jsonelement to Track object and store that in shared view model

        // perform seek bar change listener event used for getting the progress value
        geoSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                progressTextView.setText(String.valueOf(progressChangedValue));

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
//                Toast.makeText(requireContext(), "Seek bar progress is :" + progressChangedValue,
//                        Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void setupSearch() {
        actv.setAdapter(searchAdapter);

        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                buttonToMusicReview.setEnabled(false);
                if(s.length() == 0){
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0){
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    long currentTime = System.currentTimeMillis();
                    // add delay of 500 ms between current time and last search time for efficiency
                    // search length should be more than 0
                    if(currentTime - lastSearchTime > SEARCH_DELAY && s.length() != 0) {
                        lastSearchTime = currentTime;
                        actv.showDropDown();

                        Log.d("ExploreFragment", "afterTextChanged - Performing search for: " + s.toString());
                        viewModel.performSearch(s.toString())
                                .observe(getViewLifecycleOwner(), searchResults -> {
                                    Log.d("ExploreFragment", "afterTextChanged - SEARCH RESULTS ->  " + searchResults);

                                    List<JsonObject> results = new ArrayList<>();

                                    for (int i = 0; i < searchResults.size(); i++) {
                                        Log.d("ExploreFragment", "afterTextChanged - LOOP " + searchResults.get(i).get("name").getAsString() + " BY " + searchResults.get(i).getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString());
                                        results.add(searchResults.get(i).getAsJsonObject());
                                    }
                                    searchAdapter.setSelectedResult(results);
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