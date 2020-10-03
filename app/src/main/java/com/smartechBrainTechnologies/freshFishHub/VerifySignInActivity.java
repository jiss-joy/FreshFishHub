package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifySignInActivity extends AppCompatActivity {

    private EditText otp_et;
    private ExtendedFloatingActionButton submitBTN;
    private TextView verify_text;
    private ProgressDialog mProgress;

    private String currentUserID;
    private FirebaseFirestore db;
    private FirebaseDatabase fb;
    private FirebaseAuth mAuth;
    private CollectionReference userRef, consumerReference;
    private DatabaseReference deviceTokenRef;
    private String mPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_sign_in);

        Intent intent = getIntent();
        mPhoneNumber = intent.getStringExtra("PHONE NUMBER");

        initValues();

        sendOTP();

    }

    private void sendOTP() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mPhoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                // OnVerificationStateChangedCallbacks
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInUser(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(VerifySignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(VerifySignInActivity.this, AuthenticationBridgeActivity.class));
                    }


                    @Override
                    public void onCodeSent(@NonNull final String sentOTP, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(sentOTP, forceResendingToken);
                        mProgress.dismiss();
                        submitBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String userOTP = otp_et.getText().toString();
                                if (userOTP.isEmpty()) {
                                    Toast.makeText(VerifySignInActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                                } else {
                                    mProgress.setMessage("Authenticating...");
                                    mProgress.show();
                                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(sentOTP, userOTP);
                                    signInUser(phoneAuthCredential);
                                }
                            }
                        });
                    }
                });
    }

    private void signInUser(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgress.setMessage("Signing you in...");
                            updateData();
                        } else {
                            mProgress.dismiss();
                            Toast.makeText(VerifySignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateData() {
        currentUserID = mAuth.getCurrentUser().getUid();
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    Map<String, Object> deviceToken = new HashMap<>();
                    deviceToken.put("userDeviceToken", token);
                    userRef.document(currentUserID).update(deviceToken);
                    deviceToken.put("consumerDeviceToken", token);
                    consumerReference.document(currentUserID).update(deviceToken);
                    deviceTokenRef.child(currentUserID).child("deviceToken").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgress.dismiss();
                                Toast.makeText(VerifySignInActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(VerifySignInActivity.this, MainActivity.class));
                            }
                        }
                    });
                }
            }
        });
    }

    private void initValues() {
        otp_et = findViewById(R.id.verify_sign_in_otp);
        submitBTN = findViewById(R.id.verify_sign_in_submit_btn);
        verify_text = findViewById(R.id.verify_sign_in_tv_2);
        verify_text.setText("An OTP has been sent to " + mPhoneNumber);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        fb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userRef = db.collection("Users");
        consumerReference = db.collection("Consumers");
        deviceTokenRef = fb.getReference().child("Device Tokens");

    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(VerifySignInActivity.this, AuthenticationBridgeActivity.class));
    }
}