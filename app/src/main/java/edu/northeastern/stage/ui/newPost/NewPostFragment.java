package edu.northeastern.stage.ui.newPost;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import edu.northeastern.stage.R;
import edu.northeastern.stage.databinding.FragmentNewPostBinding;
import edu.northeastern.stage.model.music.Track;
import edu.northeastern.stage.ui.adapters.TrackSearchAdapter;
import edu.northeastern.stage.ui.musicReview.SubmitReviewFragment;
import edu.northeastern.stage.ui.viewmodels.NewPostViewModel;
import edu.northeastern.stage.ui.viewmodels.SharedDataViewModel;

// TODO: resolve errors

public class NewPostFragment extends Fragment {
    private FragmentNewPostBinding binding;
    private NewPostViewModel viewModel;
    private SharedDataViewModel sharedDataViewModel;
    private JsonObject selectedTrack;
    private String visibilityState;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewPostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // share data between models
        sharedDataViewModel = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        viewModel = new ViewModelProvider(this).get(NewPostViewModel.class);

        // get user ID
        sharedDataViewModel.getUserID().observe(getViewLifecycleOwner(), userID -> {
            if (userID != null) {
                viewModel.setUserID(userID);
            }
        });

        // get track if it exists
        sharedDataViewModel.getTrackPost().observe(getViewLifecycleOwner(), track -> {
            if (track != null) {
                viewModel.setTrack(track);
            }
        });

        // todo - add empty entry error messages
        // Set up the interactions for the new post elements
        binding.btnSubmitPost.setOnClickListener(v -> {
            String postContent = binding.etPostContent.getText().toString();

            // get visibility state
            int selectedId = binding.rgPostVisibility.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadioButton = getView().findViewById(selectedId);
                visibilityState = selectedRadioButton.getText().toString();
                if(visibilityState.equals("Private")) {
                    visibilityState = "private";
                } else if (visibilityState.equals("Only Friends")) {
                    visibilityState = "friends";
                } else if (visibilityState.equals("Everyone")) {
                    visibilityState = "public";
                }
            }

            if (postContent.equalsIgnoreCase("")) {
                Toast.makeText(getActivity(), "Please enter post content.", Toast.LENGTH_SHORT).show();
            } else if (selectedTrack == null) {
                Toast.makeText(getActivity(), "Please search for a song to post.", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.createPost(postContent, visibilityState);
                Toast.makeText(getActivity(), "Submit successful!", Toast.LENGTH_SHORT).show();

                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_new_post, true)
                        .build();

                // Navigate using the configured NavOptions
                NavController navController = NavHostFragment.findNavController(NewPostFragment.this);
                navController.navigate(R.id.action_navigation_new_post_to_navigation_home, null, navOptions);
            }
        });

        // Setup AutoCompleteTextView for song search
        setupSearch();

        // Observe the post submission status from the ViewModel
        viewModel.getPostSubmissionStatus().observe(getViewLifecycleOwner(), isSuccess -> {
            // Handle post submission status
        });

        return root;
    }

    // TODO: it seems like the autocomplete/search doesn't work until you delete something from the search string
    // TODO: API is getting 10 songs but the view is not being updated
    private void setupSearch() {

            TrackSearchAdapter searchAdapter = new TrackSearchAdapter(getContext(), binding.actvSongSearch);

            binding.actvSongSearch.setAdapter(searchAdapter);
            binding.actvSongSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    viewModel.performSearch(s.toString())
                            .observe(getViewLifecycleOwner(), searchResults -> {
                                searchAdapter.clear();
                                ArrayList<JsonObject> newResults = new ArrayList<>(searchResults);
                                searchAdapter.addAll(newResults);
                                searchAdapter.notifyDataSetChanged();
                            });
                }
            });

            binding.actvSongSearch.setOnItemClickListener((parent, view, position, id) -> {
                selectedTrack = searchAdapter.getItem(position);
                if (selectedTrack != null) {
                    String artists = "";
                    JsonArray artistsArray = selectedTrack.getAsJsonArray("artists");
                    if (artistsArray != null && artistsArray.size() > 0) {
                        for (JsonElement artist : artistsArray) {
                            artists = artists + artist.getAsJsonObject().get("name").getAsString() + " ";
                        }
                    }
                    artists = artists.trim();
                    Track trackToStore = viewModel.createTrack(selectedTrack);
                    sharedDataViewModel.setTrackPost(trackToStore);
                    binding.actvSongSearch.setText(selectedTrack.get("name").getAsString() + " by " + artists);
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}