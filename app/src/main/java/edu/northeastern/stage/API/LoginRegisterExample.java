package edu.northeastern.stage.API;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import edu.northeastern.stage.R;

public class LoginRegisterExample extends AppCompatActivity {

    Button loginBT;
    Button registerBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_example);

        loginBT=findViewById(R.id.login);
        registerBT=findViewById(R.id.register);

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginRegisterExample.this,Login.class);
                startActivity(intent);
            }
        });

        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginRegisterExample.this,Register.class);
                startActivity(intent);
            }
        });
    }
}