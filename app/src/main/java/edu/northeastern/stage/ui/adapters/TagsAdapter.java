package edu.northeastern.stage.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.stage.R;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagViewHolder> {

    private List<String> tags;

    public TagsAdapter() {
        this.tags = new ArrayList<>();
    }
    // Existing constructor
    public TagsAdapter(List<String> tags) {
        this.tags = tags;
    }

    public void setTags(List<String> n) {
        this.tags = n;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        String tag = tags.get(position);
        holder.tagTextView.setText(tag);
    }

    @Override
    public int getItemCount() {
        return tags != null ? tags.size() : 0;
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        final TextView tagTextView;

        TagViewHolder(View view) {
            super(view);
            tagTextView = view.findViewById(R.id.textViewTagItem);
        }
    }
}
