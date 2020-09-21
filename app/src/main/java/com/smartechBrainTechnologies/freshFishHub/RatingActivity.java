package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private LinearLayout submitLayout;
    private TextView toolbarTitle, rating_tv, rating_response;
    private ProgressDialog mProgress;

    private String fishID;
    private int fishType;
    private float r;

    private FirebaseFirestore db;
    private CollectionReference fishPostRef;
    private CollectionReference bestDealRef;
    private Map<String, Object> orderDetails = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Order Successful");

        Intent intent = getIntent();
        fishID = intent.getStringExtra("FISH ID");
        fishType = intent.getIntExtra("FISH TYPE", 0);

        initValues();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                r = rating;
                int tempRating = (int) rating;
                String message = null;

                switch (tempRating) {
                    case 1:
                        message = "Sorry to here that!";
                        break;
                    case 2:
                        message = "We always accept suggestions!";
                        break;
                    case 3:
                        message = "Fair enough!";
                        break;
                    case 4:
                        message = "Great! Thank you.";
                        break;
                    case 5:
                        message = "Awesome! You are the best!";
                        break;
                }
                rating_tv.setText(String.valueOf(rating));
                rating_response.setText(message);
                ratingBar.setRating(rating);
            }
        });

        submitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserRating();
            }
        });
    }

    private void updateUserRating() {
        mProgress.setMessage("Please Wait...");
        mProgress.show();
        if (fishType == 1) {
            fishPostRef.document(fishID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        String fishPostOrderNumbers = documentSnapshot.getString("fishPostOrderNumbers");
                        String fishPostTotalScore = documentSnapshot.getString("fishPostTotalScore");

                        float number = Float.parseFloat(fishPostOrderNumbers);
                        number = number + 1;
                        float totalRating = Float.parseFloat(fishPostTotalScore);
                        totalRating = totalRating + r;
                        float rating = totalRating / number;
                        String fishRating = String.valueOf(rating);
                        orderDetails.put("fishPostRating", fishRating);
                        orderDetails.put("fishPostOrderNumbers", String.valueOf(number));
                        orderDetails.put("fishPostTotalScore", String.valueOf(totalRating));
                        fishPostRef.document(fishID).update(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mProgress.dismiss();
                                    Toast.makeText(RatingActivity.this, "Thank you!\nYour feedback was recorded.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RatingActivity.this, MainActivity.class));
                                }
                            }
                        });
                    }
                }
            });
        } else if (fishType == 2) {
            bestDealRef.document(fishID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        String fishPostOrderNumbers = documentSnapshot.getString("fishPostOrderNumbers");
                        String fishPostTotalScore = documentSnapshot.getString("fishPostTotalScore");

                        float number = Float.parseFloat(fishPostOrderNumbers);
                        number = number + 1;
                        float totalRating = Float.parseFloat(fishPostTotalScore);
                        totalRating = totalRating + r;
                        float rating = totalRating / number;
                        String fishRating = String.valueOf(rating);
                        orderDetails.put("fishPostRating", fishRating);
                        orderDetails.put("fishPostOrderNumbers", String.valueOf(number));
                        orderDetails.put("fishPostTotalScore", String.valueOf(totalRating));
                        fishPostRef.document(fishID).update(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mProgress.dismiss();
                                    Toast.makeText(RatingActivity.this, "Thank you!\nYour feedback was recorded.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RatingActivity.this, MainActivity.class));
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void initValues() {
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        submitLayout = (LinearLayout) findViewById(R.id.rating_submit);
        rating_tv = (TextView) findViewById(R.id.rating);
        rating_response = (TextView) findViewById(R.id.rating_response);
        ratingBar.setRating(0);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        fishPostRef = db.collection("Fish Posts");
        bestDealRef = db.collection("Best Deals");

    }

    @Override
    public void onBackPressed() {
    }
}