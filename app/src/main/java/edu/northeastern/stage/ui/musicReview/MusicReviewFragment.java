package edu.northeastern.stage.ui.musicReview;

import androidx.lifecycle.ViewModelProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.northeastern.stage.MainActivity;
import edu.northeastern.stage.R;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import edu.northeastern.stage.databinding.FragmentMusicReviewBinding;
import edu.northeastern.stage.model.music.Artist;
import edu.northeastern.stage.ui.adapters.ReviewAdapter;
import edu.northeastern.stage.ui.viewmodels.MusicReviewViewModel;
import edu.northeastern.stage.ui.viewmodels.SharedDataViewModel;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MusicReviewFragment extends Fragment {
    private FragmentMusicReviewBinding binding;
    private MusicReviewViewModel mViewModel;
    private SharedDataViewModel sharedDataViewModel;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private TextView overallScoreTextView;
    private TextView noReviewsTextView;
    private Button addReviewButton;
    private TextView musicTitleTextView;
    private String dynamicLink;
    private ImageView spotifyLogoImageView;
    private TextView musicAttributesTextView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentMusicReviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // share data between models
        sharedDataViewModel = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mViewModel = new ViewModelProvider(this).get(MusicReviewViewModel.class);

        // set current user
        sharedDataViewModel.getUserID().observe(getViewLifecycleOwner(), userID -> {
            if (userID != null) {
                mViewModel.setUserID(userID);
            }
        });

        // set track
        sharedDataViewModel.getTrackReview().observe(getViewLifecycleOwner(), track -> {
            if (track != null) {
                mViewModel.setTrack(track);

                updateMusicAttributes(track.getAlbum().getName(), track.getAlbum().getReleaseDate());

                updateMusicTitle(track.getName(), track.getArtists());

                dynamicLink = track.getSpotifyUrl();

                Glide.with(this)
                        .load(track.getAlbum().getImageURL())
//                  .placeholder(R.drawable.placeholder_image) // Set a placeholder image
//                  .error(R.drawable.error_image) // Set an error image
                        .into(binding.albumCoverImageView);

                // fetch all reviews for this track
                mViewModel.fetchReviews(); // fetch all reviews
            }


        });

        musicAttributesTextView = binding.musicAttributesTextView;
        reviewsRecyclerView = binding.reviewsRecyclerView;
        overallScoreTextView = binding.overallScoreTextView;
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noReviewsTextView = binding.noReviewsTextView;
        addReviewButton = binding.addReviewButton;
        musicTitleTextView = binding.musicTitleTextView;
        spotifyLogoImageView = binding.spotifyLogo;


        addReviewButton.setOnClickListener(v -> {
//            NavController navController = NavHostFragment.findNavController(MusicReviewFragment.this);
//            navController.navigate(R.id.action_navigation_music_review_to_submit_review);
            // Use the manual navigation.
            ((MainActivity)requireActivity()).navigateToFragment("SUBMIT_REVIEW_FRAGMENT", true);
        });

        mViewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
            if (reviews == null || reviews.isEmpty()) {
                // Show "No reviews yet." text and hide RecyclerView
                noReviewsTextView.setVisibility(View.VISIBLE);
                reviewsRecyclerView.setVisibility(View.GONE);
                updateOverallScore();
            } else {
                // Show RecyclerView and hide "No reviews yet." text
                reviewAdapter = new ReviewAdapter(reviews);
                reviewsRecyclerView.setAdapter(reviewAdapter);
                noReviewsTextView.setVisibility(View.GONE);
                reviewsRecyclerView.setVisibility(View.VISIBLE);
                updateOverallScore();
            }
        });


        spotifyLogoImageView.setOnClickListener(v -> {
            if(dynamicLink != null){
                gotoUrl(dynamicLink);
            }
        });

        return root;
    }

    private void gotoUrl(String s) {
        Uri uri = Uri.parse(s);
        // Try to invoke the intent.
        try {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW,uri));
        } catch (ActivityNotFoundException e) {
            // Show an error message using a toast if click doesn't launch the url
            String msg = "Uh oh! Please try again.";
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        }
    }

    private void updateMusicAttributes(String album, String releaseDate) {
        String attributesText = String.format(Locale.getDefault(),
                "Album: %s\nReleased: %s",
                album, releaseDate);

        musicAttributesTextView.setText(attributesText);
    }

    private void updateMusicTitle(String trackName, List<Artist> artists) {

        String artistNames = artists.stream()
                .map(Artist::getName)
                .collect(Collectors.joining(", "));

        String titleText = trackName + " by " + artistNames;

        musicTitleTextView.setText(titleText);
    }

    private void updateOverallScore() {
        float overallRating = mViewModel.calculateOverallRating();
        if (overallRating == 0) {
            overallScoreTextView.setText("Overall rating: N/A");
        } else {
            String formattedRating = String.format(Locale.getDefault(), "Overall rating: %.1f / 5", overallRating);
            overallScoreTextView.setText(formattedRating);
        }
    }
}