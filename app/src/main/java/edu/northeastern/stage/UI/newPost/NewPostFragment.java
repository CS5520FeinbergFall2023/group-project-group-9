package edu.northeastern.stage.UI.newPost;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import edu.northeastern.stage.databinding.FragmentNewPostBinding;

public class NewPostFragment extends Fragment {
    private FragmentNewPostBinding binding;
    private NewPostViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewPostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(NewPostViewModel.class);

        // Set up the interactions for the new post elements
        binding.btnSubmitPost.setOnClickListener(v -> {
            // Use the ViewModel to handle post submission

//            viewModel.submitPost(/* post content here */);
        });

        // Set up the SearchView listener using the ViewModel
        binding.svSongSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Use the ViewModel to perform the search
                viewModel.performSearch(query).observe(getViewLifecycleOwner(), searchResults -> {
                    // Update UI with the search results
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // React to text change needed
                return false;
            }
        });

        // Observe the post submission status from the ViewModel
        viewModel.getPostSubmissionStatus().observe(getViewLifecycleOwner(), isSuccess -> {
            // Update UI based on submission status
            if (isSuccess) {
                // Show success message or transition to another screen
            } else {
                // Show error message
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}