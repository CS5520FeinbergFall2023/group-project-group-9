package edu.northeastern.stage.UI.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import edu.northeastern.stage.Model.Post;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Post>> posts;

    public HomeViewModel() {
        posts = new MutableLiveData<>();
        loadPosts();
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    private void loadPosts() {
        // Load posts here
        // Once loaded, set them to the 'posts' LiveData
        // posts.setValue(loadedPosts);
    }
}