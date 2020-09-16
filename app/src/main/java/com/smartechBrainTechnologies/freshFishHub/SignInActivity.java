package com.smartechBrainTechnologies.freshFishHub;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {

    private EditText phoneNumber_et;
    private ExtendedFloatingActionButton nextBTN;
    private TextView warning_tv;
    private ProgressDialog mProgress;

    private String phoneNumber;
    private FirebaseFirestore db;
    private CollectionReference userRef;
    private CollectionReference consumerReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initValues();

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
                    sendOTP();
                } else if (sellerFlag) {
                    mProgress.dismiss();
                    Toast.makeText(SignInActivity.this, "Please sign in with the Business App", Toast.LENGTH_LONG).show();
                } else {
                    mProgress.dismiss();
                    Toast.makeText(SignInActivity.this, "No User with this number found\nPlease Sign Up.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                }
            }
        });

    }


    private void sendOTP() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,        // Phone number to verify
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
                        Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignInActivity.this, AuthenticationBridgeActivity.class));
                    }


                    @Override
                    public void onCodeSent(@NonNull final String sentOTP, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(sentOTP, forceResendingToken);
                        final Dialog dialog = new Dialog(SignInActivity.this);
                        dialog.setContentView(R.layout.popup_otp);
                        Window window = dialog.getWindow();
                        window.setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        final EditText otp_et = (EditText) dialog.findViewById(R.id.otp);
                        TextView otp_tv = (TextView) dialog.findViewById(R.id.otp_text);
                        ExtendedFloatingActionButton submitBTN = (ExtendedFloatingActionButton) dialog.findViewById(R.id.otp_next);
                        submitBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String userOTP = otp_et.getText().toString();
                                if (userOTP.isEmpty()) {
                                    Toast.makeText(SignInActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                                } else {
                                    dialog.dismiss();
                                    mProgress.setMessage("Authenticating...");
                                    mProgress.show();
                                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(sentOTP, userOTP);
                                    signInUser(phoneAuthCredential);
                                }
                            }
                        });
                        otp_tv.setText("We have sent an OTP to +91-" + phoneNumber);
                        mProgress.dismiss();
                        dialog.show();
                    }
                });
    }

    private void signInUser(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (task.isSuccessful()) {
                                        String token = task.getResult().getToken();
                                        consumerReference.document(mAuth.getCurrentUser().getUid()).update("consumerDeviceToken", token);
                                        userRef.document(mAuth.getCurrentUser().getUid()).update("userDeviceToken", token);
                                        mProgress.dismiss();
                                        Toast.makeText(SignInActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                                        finish();
                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                    }
                                }
                            });

                        } else {
                            mProgress.dismiss();
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initValues() {
        phoneNumber_et = (EditText) findViewById(R.id.sign_in_phone_number);
        nextBTN = (ExtendedFloatingActionButton) findViewById(R.id.sign_in_next_btn);
        warning_tv = (TextView) findViewById(R.id.signin_warning_tv);
        warning_tv.setVisibility(View.INVISIBLE);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userRef = db.collection("Users");
        consumerReference = db.collection("Consumers");
    }
}