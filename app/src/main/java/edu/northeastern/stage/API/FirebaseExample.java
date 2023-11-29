package edu.northeastern.stage.API;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.northeastern.stage.R;

public class FirebaseExample extends AppCompatActivity {

    Button createPostBT;
    Button createReviewBT;
    Button updateUserBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_example);

        createPostBT = findViewById(R.id.createPostBT);
        createReviewBT = findViewById(R.id.createReviewBT);
        updateUserBT = findViewById(R.id.updateUserBT);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String UID = "";
        if (currentUser != null) {
            // if logged in
            UID = currentUser.getUid();
        } else {
            UID = "TEST";
        }

        String trackID = "1234";
        String content = "Hello there!";
        String user = UID;
        Long timeStamp = System.currentTimeMillis();
        Integer rating = 5;
        ArrayList<String> likes = new ArrayList<>();

        Post newPost = new Post(trackID,content,user,timeStamp,likes);
        Review newReview = new Review(trackID, content, user, timeStamp, rating);

        Long lastLoggedInTimeStamp = System.currentTimeMillis();
        Location lastLocation = new Location(100.0,100.0);
        String firstName = "Test";
        String lastName = "User";
        Name name = new Name(firstName,lastName);

        User updateUser = new User(lastLoggedInTimeStamp,name,lastLocation);

        String friend = "friendID";

        String finalUID = UID;
        createPostBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost(newPost, finalUID);
            }
        });

        createReviewBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReview(newReview, finalUID);
            }
        });

        updateUserBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser(updateUser,finalUID);
            }
        });
    }

    public void createPost(Post post, String UID) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(UID)
                .child("posts");

        DatabaseReference newPostRef = reference.push();

        newPostRef.setValue(post, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error != null) {
                    Log.d("NewPost","New post created!");
                } else {
                    Log.d("NewPost","New post created!");
                }
            }
        });
    }

    public void createReview(Review review, String UID) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(UID)
                .child("reviews");

        DatabaseReference newReviewRef = reference.push();

        newReviewRef.setValue(review, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error != null) {
                    Log.d("NewReview","New review created!");
                } else {
                    Log.e("NewReview","New review not created!");
                }
            }
        });
    }

    public void updateUser(User user, String UID) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(UID);

        Map<String, Object> updates = new HashMap<>();

        if(user.getLastLocation() != null) {
            updates.put("lastLocation",user.getLastLocation());
        }

        if(user.getName() != null) {
            updates.put("name",user.getName());
        }

        if(user.getLastLoggedInTimeStamp() != null) {
            updates.put("lastLoggedInTimeStamp",user.getLastLoggedInTimeStamp());
        }

        reference.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error != null) {
                    Log.d("UpdateUser","User update success!");
                } else {
                    Log.e("UpdateUser","User update fail!");
                }
            }
        });
    }

    // update my own list of likes
    // update post's like as well
    public void likePost(Review review, String UID) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(UID)
                .child("reviews");

        DatabaseReference newReviewRef = reference.push();

        newReviewRef.setValue(review, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error != null) {
                    Log.d("NewReview","New review created!");
                } else {
                    Log.e("NewReview","New review not created!");
                }
            }
        });
    }
    public void follow(Review review, String UID) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(UID)
                .child("reviews");

        DatabaseReference newReviewRef = reference.push();

        newReviewRef.setValue(review, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if(error != null) {
                    Log.d("NewReview","New review created!");
                } else {
                    Log.e("NewReview","New review not created!");
                }
            }
        });
    }
}