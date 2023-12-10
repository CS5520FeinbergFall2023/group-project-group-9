package edu.northeastern.stage.ui.profile;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.northeastern.stage.ui.adapters.PostAdapter;
import edu.northeastern.stage.R;
import edu.northeastern.stage.model.Post;
import edu.northeastern.stage.databinding.FragmentProfileBinding;
import edu.northeastern.stage.ui.adapters.RecentListenedAdapter;
import edu.northeastern.stage.ui.adapters.TagsAdapter;
import edu.northeastern.stage.ui.authentication.Login;
import edu.northeastern.stage.ui.editProfile.EditProfile;
import edu.northeastern.stage.ui.viewmodels.ProfileViewModel;
import edu.northeastern.stage.ui.viewmodels.SharedDataViewModel;

import android.content.Intent;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private SharedDataViewModel sharedDataViewModel;
    private TagsAdapter tagsAdapter;
    private PostAdapter postsAdapter;
    private RecentListenedAdapter recentListenedAdapter;
    private String profileOwnerId;
    private List<Post> posts;
    private List<String> recentlyListenedToImageURLs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // initialize variables
        posts = new ArrayList<>();
        recentlyListenedToImageURLs = new ArrayList<>();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        // initialize view models
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        sharedDataViewModel = new ViewModelProvider(requireActivity()).get(SharedDataViewModel.class);

        // get current user ID
        sharedDataViewModel.getUserID().observe(getViewLifecycleOwner(), userID -> {
            if (userID != null) {
                Bundle arguments = getArguments();
                if(arguments != null && arguments.getString("PROFILE_OWNER_ID") != null) {
                    profileOwnerId = arguments.getString("PROFILE_OWNER_ID");
                } else {
                    profileOwnerId = userID;
                }

                // set profile owner ID and current ID in the viewmodel
                viewModel.setProfileOwnerID(profileOwnerId);
                viewModel.setCurrentID(userID);

                // set up adapters
                setUpAdapters();

                // retrieve all values from database first
                viewModel.retrieveDataFromDatabase();

                // data retrieved status
                viewModel.getDataRetrievedStatus().observe(getViewLifecycleOwner(),dataRetrieved -> {
                    if(dataRetrieved) {
                        posts = viewModel.getPosts();
                        setUIValues();

                        // initiate follow status
                        viewModel.followStatus();

                        // update adapters
                        tagsAdapter.setTags(viewModel.getTags());
                        tagsAdapter.notifyDataSetChanged();
                        postsAdapter.setPosts(viewModel.getPosts());
                        postsAdapter.notifyDataSetChanged();
                        recentListenedAdapter.setImageUrls(viewModel.getRecentlyListenedToImageURLs());
                        recentListenedAdapter.notifyDataSetChanged();
                    }
                });

                // get followed status
                viewModel.getFollowedStatus().observe(getViewLifecycleOwner(), followedStatus -> {
                    // true = following this profile owner
                    // false = not following this profile owner
                    showEditProfileButtonOrFollowButton(followedStatus);
                });

                //set up logout button
                binding.LogOutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(requireContext(), Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut(); // use this to sign out
                        startActivity(intent);
                    }
                });
            }
        });

        viewModel.reset();
    }

    private void setUIValues() {
        binding.description.setText(viewModel.getDescription());
        binding.profileImage.setImageResource(viewModel.getProfilePicResource());
        binding.userName.setText(viewModel.getUserName());
        for(Post post : posts) {
            recentlyListenedToImageURLs.add(post.getImageURL());
        }
        tagsAdapter.setTags(viewModel.getTags());
        postsAdapter.setPosts(posts);
        recentListenedAdapter.setImageUrls(recentlyListenedToImageURLs);
    }

    private void setUpAdapters() {
        // Set up TagsAdapter and connect to view
        tagsAdapter = new TagsAdapter(new ArrayList<>());
        binding.tags.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.tags.setAdapter(tagsAdapter);

        // Set up PostAdapter and connect to view
        postsAdapter = new PostAdapter(getActivity(), new ArrayList<>(), viewModel.getCurrentID());
        binding.activities.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.activities.setAdapter(postsAdapter);

        // Set up RecentListenedAdapter and connect to view
        recentListenedAdapter = new RecentListenedAdapter(new ArrayList<>());
        binding.recentListened.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recentListened.setAdapter(recentListenedAdapter);
    }

    private void showEditProfileButtonOrFollowButton(boolean followedStatus) {
        // Set up Edit Profile Button or Follow Button
        if (viewModel.getCurrentID().equals(profileOwnerId)) {
            // User is viewing their own profile, show Edit Profile Button
            binding.editProfileButton.setVisibility(View.VISIBLE);
            binding.followButton.setVisibility(View.GONE);
            binding.unfollowButton.setVisibility(View.GONE);

            binding.editProfileButton.setOnClickListener(v -> launchEditProfile());
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.profile_edit).mutate();
            drawable.setColorFilter(ContextCompat.getColor(requireContext(), R.color.profile_edit_button_tint), PorterDuff.Mode.SRC_IN);
            binding.editProfileButton.setBackground(drawable);
        } else {
            // User is viewing someone else's profile, show Follow Button
            if(followedStatus) {
                binding.followButton.setVisibility(View.GONE);
                binding.unfollowButton.setVisibility(View.VISIBLE);
                binding.unfollowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.unfollow();
                    }
                });
            } else {
                binding.followButton.setVisibility(View.VISIBLE);
                binding.unfollowButton.setVisibility(View.GONE);
                binding.followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.follow();
                    }
                });
                binding.editProfileButton.setVisibility(View.GONE);
            }
        }
    }
    
    private void launchEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfile.class);
        startActivity(intent);
    }
}