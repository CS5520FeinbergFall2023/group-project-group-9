package edu.northeastern.stage.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Iterator;
import edu.northeastern.stage.R;

public class TrackSearchAdapter extends ArrayAdapter<JsonObject> {

    private LayoutInflater inflater;
    private AutoCompleteTextView songSearchACTV;
    private JsonObject selectedResult;
    Context context;

    public TrackSearchAdapter(Context context, AutoCompleteTextView songSearchACTV) {
        super(context, 0);
        Log.d("TrackSearchAdapter", "Constructor called");
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.songSearchACTV = songSearchACTV;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_search, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        JsonObject result = getItem(position);
        Log.d("TrackSearchAdapter", "Result size: " + result.size());

        if(result != null) {
            Log.d("TrackSearchAdapter", "getView - RESULT NOT NULL");
            viewHolder.bind(convertView.getContext(),result);
        } else {
            Log.d("TrackSearchAdapter", "getView - RESULT NULL");
        }

        return convertView;
    }

    public JsonObject getSelectedResult() {
        return selectedResult;
    }

    public void setSelectedResult(JsonObject selectedResult) {
        this.selectedResult = selectedResult;
    }

    private static class ViewHolder {
        TextView trackTitleTV;
        TextView artistNameTV;
        ImageView albumIV;

        ViewHolder(View view) {
            trackTitleTV = view.findViewById(R.id.trackTitleTV);
            artistNameTV = view.findViewById(R.id.artistNameTV);
            albumIV = view.findViewById(R.id.songAlbumIV);
        }

        void bind(Context context, JsonObject result) {

            try {
                Log.d("TrackSearchAdapter", "bind - Binding result: " + result);
                String artists = "";
                String imageURL = "";
                trackTitleTV.setText("");
                artistNameTV.setText("");
                albumIV.setImageDrawable(null);

                trackTitleTV.setText(result.get("name").getAsString());

                JsonArray artistsArray = result.getAsJsonArray("artists");
                Log.d("TrackSearchAdapter", "bind - Binding result: " + artistsArray.size());

                if (artistsArray != null && !artistsArray.isJsonNull() && artistsArray.size() > 0) {
                    StringBuilder artistsSB = new StringBuilder();
                    Iterator<JsonElement> iterator = artistsArray.iterator();

                    while (iterator.hasNext()) {
                        artistsSB.append(iterator.next().getAsJsonObject().get("name").getAsString());

                        if (iterator.hasNext()) {
                            artistsSB.append(", ");
                        }
                    }

                    artistNameTV.setText(artistsSB.toString());
                }


                JsonObject albumObject = result.getAsJsonObject("album");
                if (albumObject != null) {
                    JsonArray imagesArray = albumObject.getAsJsonArray("images");
                    if (imagesArray != null && imagesArray.size() > 0) {
                        imageURL = imagesArray.get(0).getAsJsonObject().get("url").getAsString();
                    }
                }

                Glide.with(context)
                        .load(imageURL)
//                  .placeholder(R.drawable.placeholder_image) // Set a placeholder image
//                  .error(R.drawable.error_image) // Set an error image
                        .into(albumIV);
            } catch (Exception e) {
                Log.e("TrackSearchAdapter", "bind - Error binding result", e);
            }

        }
    }
}
