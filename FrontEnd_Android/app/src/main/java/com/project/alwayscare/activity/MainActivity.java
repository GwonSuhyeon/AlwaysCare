package com.project.alwayscare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.project.alwayscare.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.main_login_button);
        signupButton = findViewById(R.id.main_signup_button);
        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == loginButton) {
            Intent gotoLoginActivity = new Intent(this, LoginActivity.class);
            startActivity(gotoLoginActivity);
        }
        else if (v == signupButton) {
            Intent gotoSignupActivity = new Intent(this, SignupActivity.class);
            startActivity(gotoSignupActivity);
        }
    }
}