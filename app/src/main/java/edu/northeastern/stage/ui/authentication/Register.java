package edu.northeastern.stage.ui.authentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.stage.MainActivity;
import edu.northeastern.stage.R;
import edu.northeastern.stage.ui.adapters.ImageAdapter;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    private FirebaseAuth mAuth;
    private ImageView pwConfirmIV;
    private EditText emailET;
    private EditText pwET;
    private EditText pwConfirmET;
    private EditText userNameET;
    private Button registerBT;
    private Spinner imageSpinner;
    private Integer profilePicSelected;
    private EditText usernameET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // change intent
            Intent intent = new Intent(Register.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        usernameET = findViewById(R.id.userNameET);
        pwConfirmIV = findViewById(R.id.pwConfirmIV);
        emailET = findViewById(R.id.emailAddressET);
        pwET = findViewById(R.id.passwordET);
        pwConfirmET = findViewById(R.id.pwConfirmET);
        registerBT = findViewById(R.id.registerBT);
        userNameET = findViewById(R.id.userNameET);
        imageSpinner = findViewById(R.id.spinnerIV);

        Integer[] images = {R.drawable.anger, R.drawable.sad, R.drawable.sob, R.drawable.shock, R.drawable.blush};

        ImageAdapter adapter = new ImageAdapter(this, images);
        imageSpinner.setAdapter(adapter);

        imageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                profilePicSelected = images[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (savedInstanceState != null) {
            emailET.setText(savedInstanceState.getString("email"));
            pwET.setText(savedInstanceState.getString("password"));
            pwConfirmET.setText(savedInstanceState.getString("passwordConfirm"));
        }

        pwConfirmET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!pwConfirmET.getText().toString().isEmpty() && !pwET.getText().toString().isEmpty()
                        && pwConfirmET.getText().toString().equals(pwET.getText().toString())) {
                    pwConfirmIV.setImageResource(R.drawable.ic_checkmark);
                } else {
                    pwConfirmIV.setImageResource(R.drawable.ic_cross);
                }
            }
        });

        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserAccount(emailET.getText().toString(), pwET.getText().toString(),
                        pwConfirmET.getText().toString(),usernameET.getText().toString());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", emailET.getText().toString());
        outState.putString("password", pwET.getText().toString());
        outState.putString("passwordConfirm", pwConfirmET.getText().toString());
        outState.putString("username", userNameET.getText().toString());
    }

    private void createUserAccount(String email, String password, String confirmPassword, String username) {
        if (email == null || password == null || confirmPassword == null || email.equals("") || password.equals("")
                || confirmPassword.equals("") || profilePicSelected == null || username.equals("") || username == null) {
          Toast.makeText(Register.this, "Register failed. Please make sure to enter an email and password.", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                                FirebaseUser user = mAuth.getCurrentUser();
                                // get reference to DB
                                DatabaseReference reference = mDatabase.getReference("users");

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("profilePicResource",profilePicSelected);

                                updates.put("email",user.getEmail());
                                updates.put("userName",username);

                                reference.child(user.getUid()).updateChildren(updates, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error == null) {
                                            Log.d(TAG, "Profile updated successfully");
                                        } else {
                                            Log.e(TAG, "Failed to update profile: " + error.getMessage());
                                        }
                                    }
                                });

                                // change intent
                                Intent intent = new Intent(Register.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                // register fail, check which exception
                                Log.e(TAG, "createUserWithEmail:failure", task.getException());
                                if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                    Toast.makeText(Register.this, "Try creating a stronger password!", Toast.LENGTH_SHORT).show();
                                } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(Register.this, "Email already exists in our system. Please login.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Register.this, Login.class);
                                    startActivity(intent);
                                } else if (task.getException() instanceof FirebaseAuthEmailException) {
                                    Toast.makeText(Register.this, "Email is not valid!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Register.this, "Unexpected error. Contact support.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }
}