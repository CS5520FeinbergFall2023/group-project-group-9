package edu.northeastern.stage.UI.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.northeastern.stage.Adapter.PostAdapter;
import edu.northeastern.stage.R;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private HomeViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(new ArrayList<>()); // Initialize with empty list
        recyclerView.setAdapter(adapter);
        observeViewModel();
        return view;
    }

    private void observeViewModel() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            // Update the UI when the data changes
            adapter.setPosts(posts);
            adapter.notifyDataSetChanged();
        });
    }
}
