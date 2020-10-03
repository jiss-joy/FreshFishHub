package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ContactUsActivity extends AppCompatActivity {

    private TextView name, number1, number2, email;
    private ProgressDialog mProgress;

    private FirebaseFirestore db;
    private DocumentReference contactUsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        mProgress.setMessage("Please wait...");
        mProgress.show();

        initValues();

        contactUsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    name.setText(task.getResult().getString("companyName"));
                    number1.setText(task.getResult().getString("companyNumber1"));
                    number2.setText(task.getResult().getString("companyNumber2"));
                    email.setText(task.getResult().getString("companyEmail"));
                }
                mProgress.dismiss();
            }
        });
    }

    private void initValues() {
        name = findViewById(R.id.contact_us_name);
        number1 = findViewById(R.id.contact_us_number1);
        number2 = findViewById(R.id.contact_us_number2);
        email = findViewById(R.id.contact_us_email);

        db = FirebaseFirestore.getInstance();
        contactUsRef = db.collection("Sellers").document("Contact Us");
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}