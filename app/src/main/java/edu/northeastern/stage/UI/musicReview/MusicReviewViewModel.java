package edu.northeastern.stage.UI.musicReview;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    // Method to fetch all reviews for this song
    public void fetchReviews() {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = mDatabase.getReference();
        DatabaseReference userRef = rootRef.child("users");

        List<Review> currentReviews = new ArrayList<>();

        String targetTrackID = "1234"; // this needs to be an argument

        Query reviewQuery  = userRef.orderByChild("reviews/trackID").equalTo(targetTrackID);
        reviewQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Review review = new Review(); // fill this out
                    currentReviews.add(review);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle errors
            }
        });

        reviews.setValue(currentReviews);
    }

    // Method to add a review, simulate fetching data
    public void fetchReviewsSimulate() {
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
