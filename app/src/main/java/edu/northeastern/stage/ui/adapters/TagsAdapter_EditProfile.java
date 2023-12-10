package edu.northeastern.stage.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.stage.R;
import edu.northeastern.stage.model.Post;

public class TagsAdapter_EditProfile extends RecyclerView.Adapter<TagsAdapter_EditProfile.ViewHolder> {
    private List<String> tagsList;
    private Context context;
    private OnItemClickListener mItemClickListener;

    // Constructor to initialize the adapter with a list of tags
    public TagsAdapter_EditProfile(Context context, List<String> tagsList) {
        this.context = context;
        this.tagsList = tagsList;
    }

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // Inner ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tagText;

        public ViewHolder(View itemView) {
            super(itemView);
            tagText = itemView.findViewById(R.id.tagTextEditProfile);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String tag = tagsList.get(position);
        holder.tagText.setText(tag);

        // Add code to handle other views and actions here if needed
    }

    @Override
    public int getItemCount() {
        return tagsList.size();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void removeAt(int position) {
        tagsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tagsList.size());
    }

    public void setTags(List<String> tags) {
        this.tagsList = tags;
        notifyDataSetChanged();
    }
}
