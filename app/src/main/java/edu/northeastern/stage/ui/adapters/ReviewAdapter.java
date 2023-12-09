package edu.northeastern.stage.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import edu.northeastern.stage.R;
import edu.northeastern.stage.model.Review;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList;
    private FirebaseDatabase mDatabase;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
        this.mDatabase = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        String userId = review.getUserID();
        // Fetch profile picture resource and set it
        DatabaseReference userRef = mDatabase
                .getReference("users")
                .child(review.getUserID())
                .child("profilePicResource");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.avatarImageView.setImageResource(Integer.parseInt(snapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here
            }
        });
        holder.userIdTextView.setText(userId);
        holder.contentTextView.setText(review.getContent());
        holder.ratingTextView.setText(String.valueOf(review.getRating()));
        Instant instant = Instant.ofEpochMilli(review.getTimestamp());
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = dateTime.format(formatter);
        holder.timestampTextView.setText(formattedDateTime);
        // If to use Glide or Picasso, load image here
        // Glide.with(holder.avatarImageView.getContext()).load(review.getAvatarUri()).into(holder.avatarImageView);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userIdTextView;
        TextView contentTextView;
        TextView ratingTextView;
        TextView timestampTextView;
        ImageView avatarImageView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userIdTextView = itemView.findViewById(R.id.tvUserId);
            contentTextView = itemView.findViewById(R.id.tvPostContent);
            ratingTextView = itemView.findViewById(R.id.tvIndividualRating);
            timestampTextView = itemView.findViewById(R.id.tvTimestamp);
            avatarImageView = itemView.findViewById(R.id.ivUserAvatar);
        }
    }
}

