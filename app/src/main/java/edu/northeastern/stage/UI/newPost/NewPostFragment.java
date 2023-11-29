package edu.northeastern.stage.UI.newPost;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.northeastern.stage.databinding.FragmentNewPostBinding;

public class NewPostFragment extends Fragment {
    private FragmentNewPostBinding binding;
    private NewPostViewModel viewModel;
    private String UID;

    // TODO: get TextView value dynamically
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout of this fragment
        binding = FragmentNewPostBinding.inflate(inflater, container, false);

        // Get root view
        View root = binding.getRoot();

        // Initiate ViewModel
        viewModel = new ViewModelProvider(this).get(NewPostViewModel.class);

        // Check if user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // if user is logged in get UID if not navigate somewhere else
        if (currentUser != null) {
            // if logged in
            UID = currentUser.getUid();
        } else {
            // if user is not logged in navigate somewhere else?
        }

        // Set up the interactions for the new post elements
        binding.btnSubmitPost.setOnClickListener(v -> {
            // viewModel.submitPost(post object here);
        });

        // Set up the SearchView listener using the ViewModel
        binding.svSongSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Use the ViewModel to perform the search
                //TODO(SAM): is this needed if we want the search results to change based on if querytextchanges?
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