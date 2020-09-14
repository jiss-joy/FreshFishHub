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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyOtpActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "freshfishhubNotifications";

    private EditText otp_et;
    private ExtendedFloatingActionButton submitBTN;
    private TextView verify_text;
    private ProgressDialog mProgress;

    private String currentUserID;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference userRef, consumerRef;
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
                        mProgress.dismiss();
                        submitBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String userOTP = otp_et.getText().toString();
                                if (userOTP.isEmpty()) {
                                    Toast.makeText(VerifyOtpActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
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
                            mProgress.setMessage("Please wait while we create your account...");
                            mProgress.show();
                            sendWelcomeNotification();
                            updateUserDetails();
                        } else {
                            mProgress.dismiss();
                            Toast.makeText(VerifyOtpActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserDetails() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                String token = task.getResult().getToken();
                currentUserID = mAuth.getCurrentUser().getUid();
                userData.clear();
                userData.put("userPhone", mPhoneNumber);
                userData.put("userType", mUserType);
                userData.put("userName", mName);
                userData.put("userDeviceToken", token);
                userRef.document(currentUserID).set(userData);

                userData.clear();
                userData.put("consumerID", currentUserID);
                userData.put("consumerName", mName);
                userData.put("consumerPhone", mPhoneNumber);
                userData.put("consumerEmail", mEmail);
                userData.put("consumerDeviceToken", token);

                consumerRef.document(currentUserID).set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                        Toast.makeText(VerifyOtpActivity.this, "Welcome " + mName, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(VerifyOtpActivity.this, MainActivity.class));
                    }
                });
            }
        });


    }

    private void sendWelcomeNotification() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.fish)
                        .setContentTitle("Hi " + mName + "!")
                        .setContentText("Welcome to the Fresh Fish Community.")
                        .setSubText("Order now and get fresh fish delivered right to you door!.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);

        mNotificationMgr.notify(1, mBuilder.build());
    }


    private void initValues() {
        otp_et = (EditText) findViewById(R.id.verify_otp);
        submitBTN = (ExtendedFloatingActionButton) findViewById(R.id.verify_submit_btn);
        verify_text = (TextView) findViewById(R.id.verify_tv_2);
        verify_text.setText("OTP sent to " + mPhoneNumber);
        mProgress = new ProgressDialog(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userRef = db.collection("Users");
        consumerRef = db.collection("Consumers");

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(VerifyOtpActivity.this, AuthenticationBridgeActivity.class));
    }
}