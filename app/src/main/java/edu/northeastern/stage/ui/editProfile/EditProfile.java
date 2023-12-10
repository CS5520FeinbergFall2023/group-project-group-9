package edu.northeastern.stage.ui.editProfile;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.stage.R;
import edu.northeastern.stage.ui.adapters.ImageAdapter;
import edu.northeastern.stage.ui.adapters.TagsAdapter_EditProfile;
import edu.northeastern.stage.ui.viewmodels.EditProfileViewModel;

public class EditProfile extends AppCompatActivity {
    private EditText editDescription;
    private AutoCompleteTextView editTags;
    private Button buttonSave;
    private Spinner profilePicSpinner;
    private ImageView profilePic;
    private TagsAdapter_EditProfile tagsAdapter;
    private RecyclerView tagsRecyclerView;
    private EditProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        // Change the color of status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // initiate view model
        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        // find all views needed and set adapters for recycler views
        editDescription = findViewById(R.id.editDescription);
        editTags = findViewById(R.id.editTags);
        buttonSave = findViewById(R.id.buttonSaveProfile);
        profilePic = findViewById(R.id.profilePicture);
        profilePicSpinner = findViewById(R.id.editProfilePicSpinner);
        tagsRecyclerView = findViewById(R.id.tagsRecyclerView);
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tagsAdapter = new TagsAdapter_EditProfile(this, new ArrayList<>());
        tagsRecyclerView.setAdapter(tagsAdapter);

        // set up observers
        setupObservers();

        // set spinner to images
        Integer[] images = {R.drawable.anger, R.drawable.sad, R.drawable.sob, R.drawable.shock, R.drawable.blush};
        ImageAdapter adapter = new ImageAdapter(this, images);
        profilePicSpinner.setAdapter(adapter);

        // update profile picture resource
        profilePicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setProfilePictureResource(images[position]);
                profilePic.setImageResource(viewModel.getProfilePictureResource());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // edit description text change listener
        editDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setDescription(s.toString());
            }
        });

        // edit tags on text change listener to submit tags to tags recycler view
        editTags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                // Logic for Enter key to add item_tag_edit.xml
                if (s.toString().endsWith("\n")) {
                    String tag = s.toString().trim();
                    if (!tag.isEmpty()) {
                        addTag(tag.replace("\n", ""));
                        editTags.setText("");
                    }
                }
            }
        });

        // when tag is selected, remove it
        tagsAdapter.setOnItemClickListener(new TagsAdapter_EditProfile.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                removeTag(viewModel.getSelectedTags().get(position));
            }
        });
    }

    private void setupObservers() {
        // get current user ID
        viewModel.getUserID().observe(this, userID -> {
            if (userID != null) {
                // get selected tags, profile picture, description from database
                viewModel.retrieveInitialData();
            }
        });
        viewModel.getDataRetrievedStatus().observe(this, dataRetrieved -> {
            if (dataRetrieved) {
                runOnUiThread(() -> {
                    editDescription.setText(viewModel.getDescription());
                    profilePic.setImageResource(viewModel.getProfilePictureResource());
                    tagsAdapter.setTags(viewModel.getSelectedTags());

                    // Set up button click listener after data retrieval
                    buttonSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewModel.updateDatabase();
                            finish();
                        }
                    });
                });
            }
        });
    }

    /**
     * Helper method to add tag to selected tags
     * @param tag - tag to add as String
     */
    private void addTag(String tag) {
        if (!viewModel.getSelectedTags().contains(tag)) {
            List<String> tags = viewModel.getSelectedTags();
            tags.add(tag);
            viewModel.setSelectedTags(tags);
            tagsAdapter.setTags(tags);
            tagsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Helper method to remove tag from selected tags
     * @param tag - tag to add as String
     */
    private void removeTag(String tag) {
        int position = viewModel.getSelectedTags().indexOf(tag);
        if (position >= 0) {
            List<String> tags = viewModel.getSelectedTags();
            tags.remove(position);
            viewModel.setSelectedTags(tags);
            tagsAdapter.notifyDataSetChanged();
        }
    }
}