package edu.northeastern.stage.ui.explore;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.northeastern.stage.model.Circle;
import edu.northeastern.stage.R;

public class ExploreFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    private AutoCompleteTextView actv;
    private Button buttonToMusicReview;
    private CircleView circleView;
    private SeekBar geoSlider;
    private ExploreViewModel viewModel;
    ExploreLocationSeekBar seekBar;
    SeekBar locationSeekBar;
    TextView progressTextView;
    private static final Random rand = new Random();


    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            Log.d(TAG, "in beforeTextChanged");
            buttonToMusicReview.setEnabled(false);
            if(s.length() == 0){
//                resultText.setText("");
            }
            // This function is called before text is edited
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.d(TAG, "in onTextChanged");
            // This function is called when text is edited
//            toastMsg("Text is edited, and onTextChangedListener is called.");
            if(s.length() == 0){
//                resultText.setText("");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
//            Log.d(TAG, "in afterTextChanged");

            viewModel.searchTextChanged(s.toString());

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_explore, container, false);

        buttonToMusicReview = fragmentView.findViewById(R.id.reviewButton);
        circleView = fragmentView.findViewById(R.id.circleView);
        actv = fragmentView.findViewById(R.id.autoCompleteTextView);
        geoSlider = fragmentView.findViewById(R.id.locationSeekBar);
        progressTextView = fragmentView.findViewById(R.id.textView);

        // perform seek bar change listener event used for getting the progress value
        geoSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                progressTextView.setText(String.valueOf(progressChangedValue));

                int width = geoSlider.getWidth() - geoSlider.getPaddingLeft() - geoSlider.getPaddingRight();
                int thumbPos = geoSlider.getPaddingLeft() + width * geoSlider.getProgress() / geoSlider.getMax();

                progressTextView.measure(0, 0);
                int txtW = progressTextView.getMeasuredWidth();
                int delta = txtW / 2;
                progressTextView.setX(geoSlider.getX() + thumbPos - delta);

                // Update ImageView properties based on seekbar progress
//                float scale = 0.5f + (float) progress / 100.0f; // Adjust scale based on progress
//                circleImageView.setScaleX(scale);
//                circleImageView.setScaleY(scale);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(requireContext(), "Seek bar progress is :" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
        observeViewModel();

        actv.setThreshold(1);

        actv.addTextChangedListener(textWatcher);

        actv.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSong = (String) parent.getItemAtPosition(position);
            viewModel.songSelected(selectedSong);
            buttonToMusicReview.setEnabled(true);
        });

        viewModel.setCircles(createCircles());

        buttonToMusicReview.setOnClickListener(v -> {
            // Use the NavController to navigate to the MusicReviewFragment
            NavController navController = NavHostFragment.findNavController(ExploreFragment.this);
            navController.navigate(R.id.action_navigation_explore_to_navigation_music_review);
        });

        return fragmentView;
    }

    private void observeViewModel() {
        viewModel.getRecommendations().observe(getViewLifecycleOwner(), recommendations -> {
            adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recommendations);
            actv.setAdapter(adapter);
        });
    }

    public List<Circle> createCircles() {
        List<Circle> circles = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = 100000; // Limit the number of attempts to avoid infinite loop
        int MIN_DISTANCE_THRESHOLD = 10;

        while (circles.size() < 100 && attempts < maxAttempts) {
            float x = rand.nextFloat() * 2000 - 1000; //-1000 to 1000
            float y = rand.nextFloat() * 2000 - 1000;
            float radius = rand.nextFloat() * 200 + 5;

            // Ensure the newly created circle doesn't overlap with existing circles
            boolean isOverlapping = false;
            for (Circle existingCircle : circles) {
                float distance = calculateDistance(x, y, existingCircle.getX(), existingCircle.getY());
                // add min_distance_threshold so they are bit further away from each other
                float minDistance = radius + existingCircle.getRadius() + MIN_DISTANCE_THRESHOLD;
                if (distance < minDistance) {
                    isOverlapping = true;
                    break; // This circle overlaps, generate a new one
                }
            }

            if (!isOverlapping) {
                circles.add(new Circle(x, y, radius));
            }

            attempts++;
        }

        // Set the circles to the existing CircleView
        if (circleView != null) {
            circleView.setCircles(circles);
            circleView.invalidate(); // Request a redraw
        }

        return circles;
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        //euclidean distance
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }


}