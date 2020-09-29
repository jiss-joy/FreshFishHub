package com.smartechBrainTechnologies.freshFishHub;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class AuthenticationBridgeActivity extends AppCompatActivity {

    private ExtendedFloatingActionButton phone_signinBTN, phone_signupBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication_bridge);

        initValues();

        phone_signinBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthenticationBridgeActivity.this, SignInActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AuthenticationBridgeActivity.this.getWindow().setExitTransition(new AutoTransition());
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(AuthenticationBridgeActivity.this).toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        phone_signupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthenticationBridgeActivity.this, SignUpActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AuthenticationBridgeActivity.this.getWindow().setExitTransition(new AutoTransition());
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(AuthenticationBridgeActivity.this).toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

    }

    private void initValues() {
        phone_signinBTN = findViewById(R.id.auth_sign_in);
        phone_signupBTN = findViewById(R.id.auth_sign_up);

    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }
}