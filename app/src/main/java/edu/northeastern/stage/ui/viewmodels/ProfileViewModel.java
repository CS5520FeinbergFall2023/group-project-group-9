package edu.northeastern.stage.ui.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.northeastern.stage.model.Post;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> dataRetrieved = new MutableLiveData<>();
    private MutableLiveData<Boolean> followedStatus = new MutableLiveData<>();
    private boolean isFollowing;
    private boolean isFollowed;
    private Integer profilePicResource;
    private String description;
    private String userName;
    private List<Post> posts = new ArrayList<>();
    private List<String> recentlyListenedToImageURLs = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private String currentID;
    private String profileOwnerID;

    public void followStatus() {

        if(currentID != null && profileOwnerID != null) {
            FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
            DatabaseReference rootRef = mDatabase.getReference();
            DatabaseReference currentUserRef = rootRef.child("users").child(currentID).child("following").child(profileOwnerID);
            DatabaseReference profileOwnerRef = rootRef.child("users").child(profileOwnerID).child("followers").child(currentID);

            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        setIsFollowing(true);
                        followedStatus.setValue(isFollowing && isFollowed);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            profileOwnerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        setIsFollowed(true);
                        followedStatus.setValue(isFollowing && isFollowed);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

    }

    public LiveData<Boolean> getDataRetrievedStatus() {
        return dataRetrieved;
    }

    public LiveData<Boolean> getFollowedStatus() {
        return followedStatus;
    }

    public void retrieveDataFromDatabase() {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = mDatabase.getReference();
        DatabaseReference userRef = rootRef.child("users").child(profileOwnerID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    if(snapshot.hasChild("profilePicResource")) {
                        setProfilePicResource(snapshot.child("profilePicResource").getValue(Integer.class));
                    }
                    if(snapshot.hasChild("description")) {
                        setDescription(snapshot.child("description").getValue(String.class));
                    }
                    if(snapshot.hasChild("userName")) {
                        setUserName(snapshot.child("userName").getValue(String.class));
                    }
                    if(snapshot.hasChild("tags")) {
                        DataSnapshot tagsSnapshot = snapshot.child("tags");
                        for (DataSnapshot tagDataSnapshot : tagsSnapshot.getChildren()) {
                            String tag = tagDataSnapshot.getKey();
                            tags.add(tag);
                        }
                    }
                    if(snapshot.hasChild("posts")) {
                        for (DataSnapshot postsSnapshot : snapshot.child("posts").getChildren()) {
                            Post post = new Post(postsSnapshot.child("postID").getValue(String.class),
                                    postsSnapshot.child("ownerID").getValue(String.class),
                                    postsSnapshot.child("trackName").getValue(String.class),
                                    postsSnapshot.child("trackID").getValue(String.class),
                                    postsSnapshot.child("artistName").getValue(String.class),
                                    postsSnapshot.child("content").getValue(String.class),
                                    postsSnapshot.child("timestamp").getValue(Long.class),
                                    postsSnapshot.child("imageURL").getValue(String.class),
                                    postsSnapshot.child("visibilityState").getValue(String.class),
                                    postsSnapshot.child("spotifyURL").getValue(String.class));
                            posts.add(post);
                        }
                        Collections.sort(posts,new Comparator<Post>() {
                            @Override
                            public int compare(Post o1, Post o2) {
                                return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                            }
                        });
                        for(Post post : posts) {
                            recentlyListenedToImageURLs.add(post.getImageURL());
                        }
                    }
                }
                dataRetrieved.setValue(true);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void follow() {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = mDatabase.getReference();
        DatabaseReference currentUserRef = rootRef.child("users").child(currentID).child("following").child(profileOwnerID);
        DatabaseReference profileOwnerRef = rootRef.child("users").child(profileOwnerID).child("followers").child(currentID);

        currentUserRef.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("ProfileFragment","Follow success!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ProfileFragment","Follow unsuccessful!");
            }
        });
        profileOwnerRef.setValue(true);
    }

    public void unfollow() {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = mDatabase.getReference();
        DatabaseReference currentUserRef = rootRef.child("users").child(currentID).child("following").child(profileOwnerID);
        DatabaseReference profileOwnerRef = rootRef.child("users").child(profileOwnerID).child("followers").child(currentID);

        currentUserRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("ProfileFragment","Unfollow success!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ProfileFragment","Unfollow unsuccessful!");
            }
        });
        profileOwnerRef.setValue(true);
    }


    private void setIsFollowing(boolean following) {
        isFollowing = following;
    }

    private void setIsFollowed(boolean followed) {
        isFollowed = followed;
    }

    public Integer getProfilePicResource() {
        return profilePicResource;
    }

    public void setProfilePicResource(Integer profilePicResource) {
        this.profilePicResource = profilePicResource;
    }

    public void reset() {
        setPosts(new ArrayList<>());
        setRecentlyListenedToImageURLs(new ArrayList<>());
        setTags(new ArrayList<>());
        dataRetrieved.setValue(false);
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<String> getRecentlyListenedToImageURLs() {
        return recentlyListenedToImageURLs;
    }

    public void setRecentlyListenedToImageURLs(List<String> recentlyListenedToImageURLs) {
        this.recentlyListenedToImageURLs = recentlyListenedToImageURLs;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCurrentID() {
        return currentID;
    }

    public void setCurrentID(String currentID) {
        this.currentID = currentID;
    }

    public String getProfileOwnerID() {
        return profileOwnerID;
    }

    public void setProfileOwnerID(String profileOwnerID) {
        this.profileOwnerID = profileOwnerID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}