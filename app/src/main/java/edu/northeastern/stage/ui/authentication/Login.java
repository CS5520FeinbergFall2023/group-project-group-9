package edu.northeastern.stage.ui.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import edu.northeastern.stage.MainActivity;
import edu.northeastern.stage.R;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    private FirebaseAuth mAuth;
    Button registerBT;
    Button loginBT;
    EditText emailET;
    EditText passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // if logged in, then go to another activity
            loginSuccessIntent();
        }

        registerBT = findViewById(R.id.registerBT);
        loginBT = findViewById(R.id.loginBT);
        emailET = findViewById(R.id.emailAddressET);
        passwordET = findViewById(R.id.passwordET);

        if(savedInstanceState != null) {
            if(!savedInstanceState.getString("email").equals("")) {
                emailET.setText(savedInstanceState.getString("email"));
            }
            if(!savedInstanceState.getString("password").equals("")) {
                passwordET.setText(savedInstanceState.getString("password"));
            }
        }

        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(emailET.getText().toString(), passwordET.getText().toString());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(emailET.getText().toString().equals("")) {
            outState.putString("email","");
        } else {
            outState.putString("email",emailET.getText().toString());
        }
        if(passwordET.getText().toString().equals("")) {
            outState.putString("password","");
        } else {
            outState.putString("password",passwordET.getText().toString());
        }
    }

    private void signIn(String email, String password) {
        // if email or password is empty
        if(Objects.equals(email, "") || Objects.equals(password, "") || email == null || password == null) {
            Toast.makeText(Login.this, "Login failed. Please make sure to enter an email and password.", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // sign in success
                                Log.i(TAG, "signInWithEmailAndPassword:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                loginSuccessIntent();
                            } else {
                                // sign in failed
                                Log.w(TAG, "signInWithEmailAndPassword:fail", task.getException());
                                Toast.makeText(Login.this, "Login failed. Please make sure your email and password are correct.", Toast.LENGTH_SHORT).show();
                                emailET.setText("");
                                passwordET.setText("");
                            }
                        }
                    });
        }
    }

    private void loginSuccessIntent() {
        //  change this intent
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}