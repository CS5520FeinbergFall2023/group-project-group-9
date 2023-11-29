package edu.northeastern.stage.API;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import edu.northeastern.stage.R;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    private FirebaseAuth mAuth;
    ImageView pwConfirmIV;
    EditText emailET;
    EditText pwET;
    EditText pwConfirmET;
    Button registerBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // change intent
           Intent intent = new Intent(Register.this, LoginRegisterExample.class);
           startActivity(intent);
        }

        pwConfirmIV = findViewById(R.id.pwConfirmIV);
        emailET = findViewById(R.id.emailAddressET);
        pwET = findViewById(R.id.passwordET);
        pwConfirmET = findViewById(R.id.pwConfirmET);
        registerBT = findViewById(R.id.registerBT);

        if (savedInstanceState != null) {
            emailET.setText(savedInstanceState.getString("email"));
            pwET.setText(savedInstanceState.getString("password"));
            pwConfirmET.setText(savedInstanceState.getString("passwordConfirm"));
        }

        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserAccount(emailET.getText().toString(),pwET.getText().toString(),pwConfirmET.getText().toString());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", emailET.getText().toString());
        outState.putString("password", pwET.getText().toString());
        outState.putString("passwordConfirm", pwConfirmET.getText().toString());
    }

    private void createUserAccount(String email, String password, String confirmPassword) {
        if (email == null || password == null || confirmPassword == null || email == "" || password == "" || confirmPassword == "") {
            Toast.makeText(Register.this, "Register failed. Please make sure to enter an email and password.", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                // change intent
                                Intent intent = new Intent(Register.this, LoginRegisterExample.class);
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