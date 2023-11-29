package edu.northeastern.stage.UI.explore;

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
import android.widget.SeekBar;
import android.widget.TextView;
import edu.northeastern.stage.R;

public class ExploreFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    private AutoCompleteTextView actv;
    private Button buttonToMusicReview;
    private CircleView circleView;
    private SeekBar geoSlider;
    private ExploreViewModel viewModel;
    TextView progressTextView;

    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            buttonToMusicReview.setEnabled(false);
            if(s.length() == 0){
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length() == 0){
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
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

        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
        observeViewModel();
        viewModel.setCircles(circleView);

        actv.setThreshold(1);
        actv.addTextChangedListener(textWatcher);
        actv.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSong = (String) parent.getItemAtPosition(position);
            // TODO: why are we setting song selected if we aren't using this anywhere?
            viewModel.songSelected(selectedSong);
            buttonToMusicReview.setEnabled(true);
        });

        buttonToMusicReview.setOnClickListener(v -> {
            // Use the NavController to navigate to the MusicReviewFragment
            NavController navController = NavHostFragment.findNavController(ExploreFragment.this);
            // TODO: when button is clicked, we should navigate to music review fragment but also create a Review object
            // TODO: and pass it in somehow
            navController.navigate(R.id.action_navigation_explore_to_navigation_music_review);
        });

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
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(requireContext(), "Seek bar progress is :" + progressChangedValue,
//                        Toast.LENGTH_SHORT).show();
            }
        });

        return fragmentView;
    }

    // TODO: need to use ArrayList<JsonElement> and create an adapter that contains not only the string values of tracks but also artist name
    private void observeViewModel() {
        viewModel.getRecommendations().observe(getViewLifecycleOwner(), recommendations -> {
            adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recommendations);
            actv.setAdapter(adapter);
        });
    }


}