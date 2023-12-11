package edu.northeastern.stage.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import edu.northeastern.stage.R;
import edu.northeastern.stage.model.music.PopularityTrack;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder> {
    private ArrayList<PopularityTrack> exploreList;
    public ExploreAdapter(ArrayList<PopularityTrack> exploreList) {
        this.exploreList = exploreList;
    }

    public void setExploreList(ArrayList<PopularityTrack> n) {
        this.exploreList = n;
    }

    @NonNull
    @Override
    public ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore, parent, false);
        return new ExploreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreViewHolder holder, int position) {
        PopularityTrack track = exploreList.get(position);
        holder.tvTrackName.setText(track.getTrackName());
        holder.tvArtistName.setText(track.getArtistName());
        holder.tvRanking.setText("Frequency: " + track.getRanking().toString());
        Picasso.get()
                .load(track.getAlbumImage())
                .error(R.drawable.profile_recent_listened_error)
                .into(holder.ivTrack);
    }

    @Override
    public int getItemCount() {
        return exploreList.size();
    }

    public static class ExploreViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTrack;
        TextView tvTrackName;
        TextView tvArtistName;
        TextView tvRanking;

        public ExploreViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTrack = itemView.findViewById(R.id.ivTrack);
            tvTrackName = itemView.findViewById(R.id.tvTrackName);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tvRanking = itemView.findViewById(R.id.tvRanking);
        }
    }
}

