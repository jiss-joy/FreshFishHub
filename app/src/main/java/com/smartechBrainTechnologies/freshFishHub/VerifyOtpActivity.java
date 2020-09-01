package com.smartechBrainTechnologies.freshFishHub;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyOtpActivity extends AppCompatActivity {

    EditText otp_et;
    ExtendedFloatingActionButton submitBTN;
    TextView verify_text;

    private String currentUserID;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference userRef, sellerRef, consumerRef;
    private String mPhoneNumber, mEmail, mName, mUserType;
    private Map<String, Object> userData = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        Intent intent = getIntent();
        mPhoneNumber = intent.getStringExtra("PHONE NUMBER");
        mEmail = intent.getStringExtra("EMAIL");
        mName = intent.getStringExtra("NAME");
        mUserType = intent.getStringExtra("USER TYPE");

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
                        Toast.makeText(VerifyOtpActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(VerifyOtpActivity.this, AuthenticationBridgeActivity.class));
                    }


                    @Override
                    public void onCodeSent(@NonNull final String sentOTP, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(sentOTP, forceResendingToken);
                        submitBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String userOTP = otp_et.getText().toString();
                                if (userOTP.isEmpty()) {
                                    Toast.makeText(VerifyOtpActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                                } else {
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
                            updateUserDetails();
                        } else {
                            Toast.makeText(VerifyOtpActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserDetails() {

        currentUserID = mAuth.getCurrentUser().getUid();
        userData.clear();
        userData.put("userPhone", mPhoneNumber);
        userData.put("userType", mUserType);
        userRef.document(currentUserID).set(userData);

        userData.clear();
        userData.put("consumerID", currentUserID);
        userData.put("consumerName", mName);
        userData.put("consumerPhone", mPhoneNumber);
        userData.put("consumerEmail", mEmail);

        consumerRef.document(currentUserID).set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(VerifyOtpActivity.this, "Welcome " + mName, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(VerifyOtpActivity.this, MainActivity.class));
            }
        });
    }


    private void initValues() {
        otp_et = (EditText) findViewById(R.id.verify_otp);
        submitBTN = (ExtendedFloatingActionButton) findViewById(R.id.verify_submit_btn);
        verify_text = (TextView) findViewById(R.id.verify_tv_2);
        verify_text.setText("OTP sent to " + mPhoneNumber);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userRef = db.collection("Users");
        consumerRef = db.collection("Consumers");

    }
}