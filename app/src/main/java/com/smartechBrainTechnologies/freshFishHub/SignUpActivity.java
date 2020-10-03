package com.smartechBrainTechnologies.freshFishHub;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUpActivity extends AppCompatActivity {

    private EditText phoneNumber_et, email_et, name_et;
    private ExtendedFloatingActionButton next_btn;
    private TextView warning_tv;
    private ProgressDialog mProgress;

    private String phoneNumber, email, name;
    private FirebaseFirestore db;
    private CollectionReference userRef;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initValues();

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyDetails();
            }
        });

        phoneNumber_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                warning_tv.setVisibility(View.GONE);
                return false;
            }
        });

        email_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                warning_tv.setVisibility(View.GONE);
                return false;
            }
        });

        name_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                warning_tv.setVisibility(View.GONE);
                return false;
            }
        });

    }

    private void verifyDetails() {
        phoneNumber = phoneNumber_et.getText().toString();
        if (phoneNumber.isEmpty()) {
            warning_tv.setText("PLEASE ENTER PHONE NUMBER");
            warning_tv.setVisibility(View.VISIBLE);
        } else if (phoneNumber.length() != 10) {
            warning_tv.setText("PLEASE ENTER A VALID PHONE NUMBER");
            warning_tv.setVisibility(View.VISIBLE);
        } else {
            email = email_et.getText().toString();
            if (email.isEmpty()) {
                warning_tv.setText("PLEASE ENTER EMAIL ID");
                warning_tv.setVisibility(View.VISIBLE);
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                warning_tv.setText("PLEASE ENTER A VALID EMAIL ID");
                warning_tv.setVisibility(View.VISIBLE);
            } else {
                name = name_et.getText().toString();
                if (name.isEmpty()) {
                    warning_tv.setText("PLEASE ENTER NAME");
                    warning_tv.setVisibility(View.VISIBLE);
                } else {
                    mProgress.setMessage("Verifying Number...");
                    mProgress.show();
                    checkNumberWithDatabase();
                }
            }
        }
    }

    private void checkNumberWithDatabase() {
        userRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                boolean flag = false;
                boolean sellerFlag = false;
                for (DocumentSnapshot numbers : value.getDocuments()) {
                    String userPhone = numbers.getString("userPhone");
                    String userType = numbers.getString("userType");
                    if (phoneNumber.equals(userPhone)) {
                        flag = true;
                        if (userType.equals("Seller")) {
                            sellerFlag = true;
                        }
                    }
                }

                if (flag && !sellerFlag) {
                    mProgress.dismiss();
                    warning_tv.setText("A USER WITH THIS NUMBER ALREADY EXISTS.\n" +
                            "PLEASE SIGN IN");
                    warning_tv.setVisibility(View.VISIBLE);
                } else if (sellerFlag) {
                    mProgress.dismiss();
                    warning_tv.setText("PLEASE USE THE BUSINESS APP TO SIGN IN");
                    warning_tv.setVisibility(View.VISIBLE);
                } else {
                    Intent intent = new Intent(SignUpActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("PHONE NUMBER", phoneNumber);
                    intent.putExtra("EMAIL", email);
                    intent.putExtra("NAME", name);
                    intent.putExtra("USER TYPE", "Consumer");

                    startActivity(intent);
                }

            }
        });

    }


    private void initValues() {
        phoneNumber_et = findViewById(R.id.sign_up_phone_number);
        email_et = findViewById(R.id.sign_up_email);
        name_et = findViewById(R.id.sign_up_name);
        next_btn = findViewById(R.id.sign_up_next_btn);
        warning_tv = findViewById(R.id.signup_warning_tv);
        warning_tv.setVisibility(View.GONE);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        userRef = db.collection("Users");
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}