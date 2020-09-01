package com.smartechBrainTechnologies.freshFishHub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserid;
    private FirebaseFirestore db;
    private CollectionReference userRef;
    private ExtendedFloatingActionButton signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initValues();
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, AuthenticationBridgeActivity.class));
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, AuthenticationBridgeActivity.class);
            startActivity(intent);
        } else {

        }
    }

    private void initValues() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("Users");
        signOut = (ExtendedFloatingActionButton) findViewById(R.id.consumer_sign_out);
    }
}