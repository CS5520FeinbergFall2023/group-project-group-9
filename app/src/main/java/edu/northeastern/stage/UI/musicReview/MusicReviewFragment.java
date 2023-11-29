package edu.northeastern.stage.UI.musicReview;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.northeastern.stage.R;
import edu.northeastern.stage.Adapter.ReviewAdapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MusicReviewFragment extends Fragment {
    private MusicReviewViewModel mViewModel;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;

    public static MusicReviewFragment newInstance() {
        return new MusicReviewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_review, container, false);
        reviewsRecyclerView = view.findViewById(R.id.reviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mViewModel = new ViewModelProvider(this).get(MusicReviewViewModel.class);
        mViewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
            reviewAdapter = new ReviewAdapter(reviews);
            reviewsRecyclerView.setAdapter(reviewAdapter);
        });

        mViewModel.fetchReviews(); // Simulate fetching data

        return view;
    }
}
