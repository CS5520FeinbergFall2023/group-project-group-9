package edu.northeastern.stage.UI.musicReview;

import androidx.lifecycle.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.stage.Model.Review;

public class MusicReviewViewModel extends ViewModel {
    private MutableLiveData<List<Review>> reviews;

    public MusicReviewViewModel() {
        reviews = new MutableLiveData<>();

        reviews.setValue(new ArrayList<>());
    }

    public LiveData<List<Review>> getReviews() {
        return reviews;
    }

    // Method to add a review, simulate fetching data
    public void fetchReviews() {
        // replace by actual data retrieval logic
        List<Review> reviewList = new ArrayList<>();
        reviewList.add(new Review("user123", "uri_to_avatar", "This album is great!", 4.5f));
        reviewList.add(new Review("user123", "uri_to_avatar", "This album is great!", 4.5f));
        reviewList.add(new Review("user123", "uri_to_avatar", "This album is great!", 4.5f));
        reviewList.add(new Review("user123", "uri_to_avatar", "This album is great!", 4.5f));
        reviewList.add(new Review("user123", "uri_to_avatar", "This album is great!", 4.5f));
        // ... add more reviews
        reviews.setValue(reviewList);
    }
}
