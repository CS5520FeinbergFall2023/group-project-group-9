package edu.northeastern.stage.UI.profile;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.northeastern.stage.R;
import edu.northeastern.stage.Model.Post;
import edu.northeastern.stage.databinding.FragmentProfileBinding;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private TagsAdapter tagsAdapter;
    private List<String> tags;
    private static final int REQUEST_EDIT_PROFILE = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        initUI();
        return binding.getRoot();
    }

    private void initUI() {
        // TODO: Initialize ViewModel here if needed

        // Tags
        // TODO: connect list of tags to the database
        tags = Arrays.asList("#IndiePop", "#AlternativeRock");
        tagsAdapter = new TagsAdapter(tags);
        binding.tags.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.tags.setAdapter(tagsAdapter);

        // Edit Button
        binding.editProfileButton.setVisibility(checkIfOwner() ? View.VISIBLE : View.GONE);
        binding.editProfileButton.setOnClickListener(v -> launchEditProfile());

        // If the fragment is not for the profile owner, the edit button should not be shown
        if (!checkIfOwner()) {
            binding.editProfileButton.setVisibility(View.GONE);
        }
    }

    private boolean checkIfOwner() {
        // TODO: use firebase function to finish the userId check.
        return true;
    }

    private void launchEditProfile() {
        // TODO: Refactor for ActivityResultLauncher if using
        Intent intent = new Intent(getActivity(), EditProfile.class);
        startActivityForResult(intent, REQUEST_EDIT_PROFILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == Activity.RESULT_OK && data != null) {
            // Retrieve the data from the result intent
            String username = data.getStringExtra(EditProfile.EXTRA_USERNAME);
            String description = data.getStringExtra(EditProfile.EXTRA_DESCRIPTION);
            String tagsString = data.getStringExtra(EditProfile.EXTRA_TAGS);

            // Update your views using binding
            binding.userName.setText(username);
            binding.description.setText(description);
            // Split by the delimiter used when setting the tags
            //TODO: update with autocomplete function
            tags = Arrays.asList(tagsString.split("#(?=[^#])"));

            // Update the RecyclerView with new tags
            tagsAdapter.setTags(tags);
            tagsAdapter.notifyDataSetChanged();
        }
    }

    public static class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

        private List<Post> postList;

        public PostsAdapter(List<Post> postList) {
            this.postList = postList;
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PostViewHolder holder, int position) {
            Post post = postList.get(position);
            // Set the data to your views here
            holder.tvMusicLink.setText(post.getMusicLink());
            holder.tvPostContent.setText(post.getPostContent());
            // Set other views like image views etc.
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }

        public static class PostViewHolder extends RecyclerView.ViewHolder {

            TextView tvMusicLink, tvPostContent;
            ImageView ivUserAvatar, ivLike, visibleState;

            public PostViewHolder(View itemView) {
                super(itemView);
                tvMusicLink = itemView.findViewById(R.id.tvMusicLink);
                tvPostContent = itemView.findViewById(R.id.tvPostContent);
                ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
                ivLike = itemView.findViewById(R.id.ivLike);
                visibleState = itemView.findViewById(R.id.visibleState);
                // Initialize other views
            }
        }
    }
}
