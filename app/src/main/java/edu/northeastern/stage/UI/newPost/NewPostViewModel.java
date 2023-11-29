package edu.northeastern.stage.UI.newPost;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class NewPostViewModel extends ViewModel {
    // LiveData for observing post submission status
    private final MutableLiveData<Boolean> postSubmissionStatus = new MutableLiveData<>();

    // Method to handle post submission logic
    public void submitPost(String postContent) {
        // Logic to submit the post

        // Once the submission logic is complete, update the LiveData
        postSubmissionStatus.setValue(true); // true if successful, false otherwise
    }

    // Method to handle search logic
    public LiveData<List<String>> performSearch(String query) {
        MutableLiveData<List<String>> searchResults = new MutableLiveData<>();

        // Logic to perform search and retrieve results

        // Update searchResults with the processed data

        // searchResults.setValue(processedData);

        return searchResults;
    }

    // Getters for LiveData
    public LiveData<Boolean> getPostSubmissionStatus() {
        return postSubmissionStatus;
    }
}