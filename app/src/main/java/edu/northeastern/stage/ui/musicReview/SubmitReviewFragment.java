package edu.northeastern.stage.ui.musicReview;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import edu.northeastern.stage.MainActivity;
import edu.northeastern.stage.R;
import edu.northeastern.stage.model.Review;
import edu.northeastern.stage.ui.viewmodels.MusicReviewViewModel;
import edu.northeastern.stage.ui.viewmodels.SharedDataViewModel;

public class SubmitReviewFragment extends Fragment {
    private EditText reviewContentEditText;
    private RatingBar reviewRatingBar;
    private Button submitReviewButton;
    private MusicReviewViewModel mViewModel;
    private SharedDataViewModel sharedDataViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_submit_review, container, false);

        reviewContentEditText = view.findViewById(R.id.reviewContentEditText);
        reviewRatingBar = view.findViewById(R.id.reviewRatingBar);
        submitReviewButton = view.findViewById(R.id.submitReviewButton);

        sharedDataViewModel = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);
        mViewModel = new ViewModelProvider(this).get(MusicReviewViewModel.class);

        sharedDataViewModel.getUserID().observe(getViewLifecycleOwner(), userID -> {
            if(userID != null) {
                mViewModel.setUserID(userID);
            }
        });

        sharedDataViewModel.getTrackReview().observe(getViewLifecycleOwner(), track -> {
            if(track != null) {
                mViewModel.setTrack(track);
            }
        });

        submitReviewButton.setOnClickListener(v -> submitReview());

        return view;
    }

    private void submitReview() {
        String userID = mViewModel.getUserID();
        float rating = reviewRatingBar.getRating();
        Long timestamp = System.currentTimeMillis();
        String trackID = mViewModel.getTrack().getId();
        String content = reviewContentEditText.getText().toString();

        if (content.equalsIgnoreCase("")) {
            Toast.makeText(getActivity(), "Please enter your rating content.", Toast.LENGTH_SHORT).show();
        } else if (rating == 0.0f) {
            Toast.makeText(getActivity(), "Please select a rating in stars.", Toast.LENGTH_SHORT).show();
        } else {
            Review newReview = new Review(userID,content,rating,timestamp,trackID);
            mViewModel.addReview(newReview);
            Toast.makeText(getActivity(), "Submit successful!", Toast.LENGTH_SHORT).show();
            // Clear the fields after submission
            reviewContentEditText.setText("");
            reviewRatingBar.setRating(0);

            // Remove submit review page from the back stack then navigate back to music review page
//            NavOptions navOptions = new NavOptions.Builder()
//                    .setPopUpTo(R.id.navigation_music_review, true)
//                    .build();
//            NavController navController = NavHostFragment.findNavController(SubmitReviewFragment.this);
//            navController.navigate(R.id.action_navigation_submit_review_to_navigation_music_review, null, navOptions);
            ((MainActivity)requireActivity()).removeFragmentFromBackStack("SUBMIT_REVIEW_FRAGMENT");
            ((MainActivity)requireActivity()).navigateToFragment("MUSIC_REVIEW_FRAGMENT", true, null);
        }
    }
}
