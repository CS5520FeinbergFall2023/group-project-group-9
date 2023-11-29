package edu.northeastern.stage.UI.profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import edu.northeastern.stage.R;

public class EditProfile extends AppCompatActivity {
    private EditText editUsername;
    private EditText editDescription;
    private EditText editTags;
    private Button buttonSave;
    private ImageView profilePicture;

    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_TAGS = "extra_tags";
    public static final int REQUEST_IMAGE_GET = 1;

    public static void start(Context context) {
        Intent intent = new Intent(context, EditProfile.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        editUsername = findViewById(R.id.editUsername);
        editDescription = findViewById(R.id.editDescription);
        editTags = findViewById(R.id.editTags);
        buttonSave = findViewById(R.id.buttonSaveProfile);

        ImageView profilePicture = findViewById(R.id.profilePicture);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_GET);
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra(EXTRA_USERNAME, editUsername.getText().toString());
                data.putExtra(EXTRA_DESCRIPTION, editDescription.getText().toString());
                data.putExtra(EXTRA_TAGS, editTags.getText().toString());

                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            // Use the URI to load the image
            profilePicture.setImageURI(fullPhotoUri);
            // TODO: upload to the database
        }
    }
}
