package edu.northeastern.stage.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.stage.Model.Post;
import edu.northeastern.stage.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        // set the data to ViewHolder
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        // DefineViewHolder's views, e.g.,
        // TextView tvMusicLink, tvPostContent;
        // ImageView ivUserAvatar, ivLike;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views, e.g.,
            // tvMusicLink = itemView.findViewById(R.id.tvMusicLink);
            // tvPostContent = itemView.findViewById(R.id.tvPostContent);
            // ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            // ivLike = itemView.findViewById(R.id.ivLike);
        }
    }
}

