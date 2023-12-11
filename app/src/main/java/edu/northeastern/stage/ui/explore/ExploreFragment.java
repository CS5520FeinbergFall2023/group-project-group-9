package edu.northeastern.stage.ui.explore;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import edu.northeastern.stage.MainActivity;
import edu.northeastern.stage.databinding.FragmentExploreBinding;
import edu.northeastern.stage.model.music.PopularityTrack;
import edu.northeastern.stage.model.music.Track;
import edu.northeastern.stage.ui.adapters.ExploreAdapter;
import edu.northeastern.stage.ui.adapters.TrackSearchAdapter;
import edu.northeastern.stage.ui.viewmodels.ExploreViewModel;
import edu.northeastern.stage.ui.viewmodels.SharedDataViewModel;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private ExploreViewModel viewModel;
    private RecyclerView recyclerView;
    private ExploreAdapter adapter;
    private JsonObject selectedTrack;
    private AutoCompleteTextView actv;
    private Button buttonToMusicReview;
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
        actv = binding.autoCompleteTextView;
        actv.setThreshold(1);
        geoSlider = binding.locationSeekBar;
        progressTextView = binding.textView;
        recyclerView = binding.popularPostsRV;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // set up view models to share data
        sharedDataViewModel = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        // set current user
        sharedDataViewModel.getUserID().observe(getViewLifecycleOwner(), userID -> {
            if (userID != null) {
                viewModel.setUserID(userID);
            }
        });

        // instantiate adapter
        adapter = new ExploreAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

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

        // perform seek bar change listener event used for getting the progress value
        geoSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                viewModel.setExploreTracks(new ArrayList<>());
                currentMileRadius = seekBar.getProgress();
                String progressText = currentMileRadius + " miles";
                progressTextView.setText(progressText);

                int width = geoSlider.getWidth() - geoSlider.getPaddingLeft() - geoSlider.getPaddingRight();
                int thumbPos = geoSlider.getPaddingLeft() + width * geoSlider.getProgress() / geoSlider.getMax();

                progressTextView.measure(0, 0);
                int txtW = progressTextView.getMeasuredWidth();
                int delta = txtW / 2;
                progressTextView.setX(geoSlider.getX() + thumbPos - delta);

                LiveData<Map<String, Integer>> tracksLiveData = viewModel.getTracksNearby(currentMileRadius);
                tracksLiveData.removeObservers(getViewLifecycleOwner());
                tracksLiveData.observe(getViewLifecycleOwner(),tracksFrequency -> {
                    Log.d("ABC123","CALLED");

                    if(tracksFrequency != null && !tracksFrequency.isEmpty()) {
                        viewModel.setExploreTracks(new ArrayList<>());
                        List<CompletableFuture<Void>> searchFutures = new ArrayList<>();

                        for (String key : tracksFrequency.keySet()) {
                            CompletableFuture<Void> searchFuture = viewModel.searchTrack(key);
                            searchFutures.add(searchFuture);
                        }

                        CompletableFuture<Void>[] searchFuturesArray = searchFutures.toArray(new CompletableFuture[0]);

                        CompletableFuture<Void> allOf = CompletableFuture.allOf(searchFuturesArray);

                        allOf.thenRun(() -> {
                            ArrayList<PopularityTrack> popularityTracks = viewModel.getExploreTracks();
                            Comparator<PopularityTrack> popularityComparator = Comparator.comparingInt(PopularityTrack::getRanking).reversed();
                            Collections.sort(popularityTracks, popularityComparator);
                            viewModel.setExploreTracks(popularityTracks);
                            requireActivity().runOnUiThread(() -> {
                                adapter.setExploreList(viewModel.getExploreTracks());
                                adapter.notifyDataSetChanged();
                            });
                            tracksLiveData.removeObservers(getViewLifecycleOwner());
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
                                    actv.showDropDown();
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