package com.smartechBrainTechnologies.freshFishHub;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class SignInActivity extends AppCompatActivity {

    private EditText phoneNumber_et;
    private ExtendedFloatingActionButton nextBTN;
    private TextView warning_tv;
    private ProgressDialog mProgress;

    private String phoneNumber;
    private FirebaseFirestore db;
    private CollectionReference userRef;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initValues();

        phoneNumber_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warning_tv.setVisibility(View.GONE);
            }
        });

        phoneNumber_et.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                warning_tv.setVisibility(View.GONE);
                return false;
            }
        });

        nextBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = phoneNumber_et.getText().toString();
                if (phoneNumber.isEmpty()) {
                    warning_tv.setText("PLEASE ENTER PHONE NUMBER");
                    warning_tv.setVisibility(View.VISIBLE);
                } else if (phoneNumber.length() != 10) {
                    warning_tv.setText("PLEASE ENTER A VALID PHONE NUMBER");
                    warning_tv.setVisibility(View.VISIBLE);
                } else {
                    mProgress.setMessage("Verifying number...");
                    mProgress.show();
                    checkNumberWithDatabase();
                }
            }
        });
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
                    startActivity(new Intent(SignInActivity.this, VerifySignInActivity.class).putExtra("PHONE NUMBER", phoneNumber));
                } else if (sellerFlag) {
                    mProgress.dismiss();
                    warning_tv.setText("PLEASE SIGN IN WITH THE BUSINESS APP");
                    warning_tv.setVisibility(View.VISIBLE);
                } else {
                    mProgress.dismiss();
                    Toast.makeText(SignInActivity.this, "No User with this number found\nPlease Sign Up.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        SignInActivity.this.getWindow().setExitTransition(new AutoTransition());
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SignInActivity.this).toBundle());
                    } else {
                        startActivity(intent);
                    }
                }
            }
        });

    }

    private void initValues() {
        phoneNumber_et = findViewById(R.id.sign_in_phone_number);
        nextBTN = findViewById(R.id.sign_in_next_btn);
        warning_tv = findViewById(R.id.signin_warning_tv);
        warning_tv.setVisibility(View.GONE);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        userRef = db.collection("Users");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}