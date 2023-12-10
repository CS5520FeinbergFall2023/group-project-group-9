package edu.northeastern.stage.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.stage.R;
import edu.northeastern.stage.model.Post;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> postList;
    private String currentUserId;

    public PostAdapter(Context context, List<Post> postList, String currentUserId) {
        this.context = context;
        this.postList = postList;
        this.currentUserId = currentUserId;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        Post post = postList.get(position);

        String viewType = "";

        Instant instant = Instant.ofEpochMilli(post.getTimestamp());
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = dateTime.format(formatter);
        holder.tvTimestamp.setText(formattedDateTime);

        if (isOwner(post)) {
            viewType = "owner";
        } else if (isFriend(post)) {
            viewType = "friend";
        } else {
            viewType = "stranger";
        }

        // set post visibility
        String visibilityState = post.getVisibilityState();
        if(visibilityState == null) {
            visibilityState = "private";
        }

        // friend
        if (viewType.equals("friend")) {
            if (visibilityState.equals("private")) {
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
            }
        }
        // stranger
        if (viewType.equals("stranger")) {
            if (!visibilityState.equals("public")) {
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
            }
        }

        // set Visibility State Icon
        if (viewType.equals("owner")) {
            switch (visibilityState) {
                case "friends":
                    holder.visibleState.setImageResource(R.drawable.profile_friends);
                    break;
                case "private":
                    holder.visibleState.setImageResource(R.drawable.profile_private);
                    break;
                default:
                    holder.visibleState.setImageResource(R.drawable.profile_public);
            }
        }

        // set post content
        holder.tvPostContent.setText(post.getContent());

        //open music link
        String url = post.getSpotifyURL();
        holder.songCard.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            v.getContext().startActivity(i);
        });

        // update username
        setProfileUsername(holder, post);

        //display artist and track name
        holder.tvTrackName.setText(post.getTrackName());
        holder.tvArtistName.setText(post.getArtistName());

        // set profile pic
        setProfilePicResourceID(holder,post);

        //display song image
        Picasso.get()
                .load(post.getImageURL())
                .error(R.drawable.profile_recent_listened_error)
                .into(holder.tvMusicImage);

        // Set the like status on the ivLike ImageView
        updateLikeUnlike(post, holder);

        // set on click listener
        holder.ivLike.setOnClickListener(v-> {
            isLikedAndRemoveIfLiked(post);
            updateLikeUnlike(post, holder);
        });

        holder.ivUserAvatar.setOnClickListener(v -> {
            // TODO: navigate to ProfileFragment and bundle PROFILE_OWNER_ID
//            Intent intent = new Intent(context, ProfileFragment.class);
//            intent.putExtra("PROFILE_OWNER_ID", post.getOwnerID());
//            context.startActivity(intent);
        });
    }

    private void setProfileUsername(PostViewHolder holder, Post post) {
        String ownerID = post.getOwnerID();
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(ownerID)
                .child("userName");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.tvUserName.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfilePicResourceID(PostViewHolder holder, Post post) {
        // Assuming the post object contains the ownerID
        String ownerId = post.getOwnerID();

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(ownerId)
                .child("profilePicResource");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.ivUserAvatar.setImageResource(Integer.parseInt(snapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addLikeFDBD(Post post) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(post.getOwnerID())
                .child("posts")
                .child(post.getPostID())
                .child("likes")
                .child(currentUserId);

        reference.setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("PostAdapter","Added like to database.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("PostAdapter","Failed to add like to database.");
                    }
                });
    }

    private void removeLikeFBDB(Post post) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(post.getOwnerID())
                .child("posts")
                .child(post.getPostID())
                .child("likes")
                .child(currentUserId);

        reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("PostAdapter","Removed like from database.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("PostAdapter","Failed to remove like from database.");
                    }
                });
    }

    private void isLikedAndRemoveIfLiked(Post post) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(post.getOwnerID())
                .child("posts")
                .child(post.getPostID())
                .child("likes");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> likedUserIDs = new ArrayList<>();
                for(DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    likedUserIDs.add(likeSnapshot.getKey());
                }

                boolean isLiked = likedUserIDs.contains(currentUserId);
                if (isLiked) {
                    removeLikeFBDB(post);
                } else {
                    addLikeFDBD(post);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateLikeUnlike(Post post, PostViewHolder holder) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(post.getOwnerID())
                .child("posts")
                .child(post.getPostID())
                .child("likes");

        List<String> likedUserIDs = new ArrayList<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String likedUserID = likeSnapshot.getKey();
                    likedUserIDs.add(likedUserID);
                }

                if(likedUserIDs.contains(currentUserId)) {
                    holder.ivLike.setColorFilter(ContextCompat.getColor(context,R.color.green));
                } else {
                    holder.ivLike.setColorFilter(ContextCompat.getColor(context,R.color.black));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private boolean isOwner(Post post) {
        return currentUserId.equals(post.getOwnerID());
    }

    private boolean isFriend(Post post) {

        final boolean[] isFriend = {false};

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        DatabaseReference reference = mDatabase
                .getReference("users")
                .child(currentUserId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("following").hasChild(post.getOwnerID()) &&
                        snapshot.child("followers").hasChild(post.getOwnerID())) {
                    isFriend[0] = true;
                } else {
                    isFriend[0] = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return isFriend[0];
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView tvPostContent, tvTrackName, tvArtistName, tvTimestamp, tvUserName;
        ImageView ivUserAvatar, ivLike, visibleState, tvMusicImage;
        LinearLayout songCard;

        public PostViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            ivLike = itemView.findViewById(R.id.ivLike);
            visibleState = itemView.findViewById(R.id.visibleState);
            songCard = itemView.findViewById(R.id.songCard);
            tvTrackName = itemView.findViewById(R.id.tvTrackName);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tvMusicImage = itemView.findViewById(R.id.tvMusicImage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }
}