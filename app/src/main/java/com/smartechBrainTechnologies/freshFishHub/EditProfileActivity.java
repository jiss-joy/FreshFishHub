package com.smartechBrainTechnologies.freshFishHub;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private final Map<String, Object> user = new HashMap<>();
    private TextView warning_tv;
    private ExtendedFloatingActionButton submitBTN;
    private ProgressDialog mProgress;

    private String name, email;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference consumerRef;
    private CollectionReference userRef;
    private EditText name_et, phone_et;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initValues();
        mProgress.setMessage("Please wait...");
        mProgress.show();
        loadData();

        name_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                warning_tv.setVisibility(View.GONE);
                return false;
            }
        });

        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = name_et.getText().toString();
                if (name.isEmpty()) {
                    warning_tv.setText("Name cannot be empty.");
                    warning_tv.setVisibility(View.VISIBLE);
                } else {
                    mProgress.setMessage("Updating User Details ...");
                    mProgress.show();
                    updateData(name, email);
                }
            }
        });

    }

    private void updateData(String name, String email) {
        user.put("userName", name);
        userRef.document(mAuth.getCurrentUser().getUid()).update(user);
        user.put("consumerName", name);
        user.put("consumerEmail", email);
        consumerRef.document(mAuth.getCurrentUser().getUid()).update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();
                finish();
                Toast.makeText(EditProfileActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        consumerRef.document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    name_et.setText(task.getResult().getString("consumerName"));
                    phone_et.setText(task.getResult().getString("consumerPhone"));
                    phone_et.setEnabled(false);
                    mProgress.dismiss();
                }
            }
        });
    }

    private void initValues() {
        name_et = findViewById(R.id.edit_profile_name);
        phone_et = findViewById(R.id.edit_profile_phone);
        submitBTN = findViewById(R.id.edit_profile_next);
        warning_tv = findViewById(R.id.edit_profile_warning_tv);
        warning_tv.setVisibility(View.GONE);
        mProgress = new ProgressDialog(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        consumerRef = db.collection("Consumers");
        userRef = db.collection("Users");
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}