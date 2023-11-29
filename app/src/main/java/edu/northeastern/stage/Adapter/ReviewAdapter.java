package edu.northeastern.stage.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import edu.northeastern.stage.Model.Review;
import edu.northeastern.stage.R;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
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
        holder.userIdTextView.setText(review.getUserId());
        holder.contentTextView.setText(review.getContent());
        holder.ratingTextView.setText(String.valueOf(review.getRating()));
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
        ImageView avatarImageView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userIdTextView = itemView.findViewById(R.id.tvUserId);
            contentTextView = itemView.findViewById(R.id.tvPostContent);
            ratingTextView = itemView.findViewById(R.id.tvIndividualRating);
            avatarImageView = itemView.findViewById(R.id.ivUserAvatar);
        }
    }
}

