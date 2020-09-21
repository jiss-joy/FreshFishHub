package com.smartechBrainTechnologies.freshFishHub;

import android.content.Intent;
import android.os.Bundle;
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
                startActivity(new Intent(AuthenticationBridgeActivity.this, SignInActivity.class));
            }
        });

        phone_signupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AuthenticationBridgeActivity.this, SignUpActivity.class));
            }
        });

    }

    private void initValues() {
        phone_signinBTN = (ExtendedFloatingActionButton) findViewById(R.id.auth_sign_in);
        phone_signupBTN = (ExtendedFloatingActionButton) findViewById(R.id.auth_sign_up);

    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }
}