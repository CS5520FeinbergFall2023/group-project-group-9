package edu.northeastern.stage.ui.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.stage.model.Post;

public class EditProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> dataRetrieved = new MutableLiveData<>();
    private MutableLiveData<String> currentUserID = new MutableLiveData<>();
    private Integer profilePictureResource;
    private String description;
    private List<String> selectedTags = new ArrayList<>();

    public MutableLiveData<Boolean> getDataRetrievedStatus() {
        return dataRetrieved;
    }

    public MutableLiveData<String> getUserID() {
        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserID.setValue(UID);
        return currentUserID;
    }

    public void updateDatabase() {
        if(dataRetrieved.getValue()) {
            FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
            DatabaseReference reference = mDatabase
                    .getReference("users")
                    .child(currentUserID.getValue());

            Map<String, Object> updates = new HashMap<>();

            updates.put("imageResource", profilePictureResource);
            updates.put("description", description);

            reference.updateChildren(updates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error == null) {
                        Log.d("UpdateUser", "User update successful");
                    } else {
                        Log.e("UpdateUser", "Update user failed: " + error.getMessage());
                    }
                }
            });

            Map<String, Object> tagsToUpdate = new HashMap<>();

            for (String tag : selectedTags) {
                tagsToUpdate.put(tag,true);
            }

            reference.child("tags").updateChildren(tagsToUpdate);

            DatabaseReference existingTagsReference = reference.child("tags");

            existingTagsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot tagSnapshot : snapshot.getChildren()) {
                        String tag = tagSnapshot.getKey();
                        if(!selectedTags.contains(tag)) {
                            tagSnapshot.getRef().removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    public void retrieveInitialData() {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = mDatabase.getReference();
        DatabaseReference userRef = rootRef.child("users").child(currentUserID.getValue());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    if(snapshot.hasChild("imageResource")) {
                        profilePictureResource = snapshot.child("imageResource").getValue(Integer.class);
                    } else {

                    }
                    if(snapshot.hasChild("description")) {
                        description = snapshot.child("description").getValue(String.class);
                    } else {
                        description = "";
                    }
                    if(snapshot.hasChild("tags")) {
                        for (DataSnapshot tagsSnapshot : snapshot.child("tags").getChildren()) {
                            String tag = tagsSnapshot.getKey();
                            selectedTags.add(tag);
                        }
                    } else {
                        selectedTags = new ArrayList<>();
                    }
                    dataRetrieved.setValue(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public Integer getProfilePictureResource() {
        return profilePictureResource;
    }

    public void setProfilePictureResource(Integer profilePictureResource) {
        this.profilePictureResource = profilePictureResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSelectedTags() {
        return selectedTags;
    }

    public void setSelectedTags(List<String> selectedTags) {
        this.selectedTags = selectedTags;
    }
}