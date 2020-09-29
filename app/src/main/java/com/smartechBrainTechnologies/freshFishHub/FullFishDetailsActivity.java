package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class FullFishDetailsActivity extends AppCompatActivity {

    private ImageView fish_pic;
    private TextView fish_name, fish_price, fish_availability, fish_time, seller_name, fish_qty, rating;
    private ImageButton addBTN, removeBTN;
    private LinearLayout buyNowBTN;
    private ProgressDialog mProgress;
    private TextView toolbarTitle, rating_tv;
    private RatingBar ratingBar;

    private String fishID;
    private int dealType;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference fishPostRef;
    private CollectionReference bestDealRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_fish_details);

        Intent intent = getIntent();
        fishID = intent.getStringExtra("FISH ID");
        dealType = intent.getIntExtra("DEAL TYPE", 0);

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Item Details");

        initValues();

        if (dealType == 1) {
            loadBestDealData();
        } else if (dealType == 2) {
            loadNormalData();
        } else {
            loadCommonData();
        }

        addBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentQty = fish_qty.getText().toString();
                String newQty = String.valueOf(Float.parseFloat(currentQty) + Float.parseFloat("0.5"));
                fish_qty.setText(newQty);
            }
        });

        removeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentQty = fish_qty.getText().toString();
                if (!currentQty.equals("0.0")) {
                    String newQty = String.valueOf(Float.parseFloat(currentQty) - Float.parseFloat("0.5"));
                    fish_qty.setText(newQty);
                }
            }
        });


        buyNowBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fish_qty.getText().toString().equals("0.0")) {
                    Toast.makeText(FullFishDetailsActivity.this, "Quantity cannot be 0", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent1 = new Intent(FullFishDetailsActivity.this, ConfirmOrderActivity.class);
                    intent1.putExtras(getIntent().getExtras());
                    intent1.putExtra("FISH QTY", fish_qty.getText().toString());
                    startActivity(intent1);
                }
            }
        });
    }

    private void loadCommonData() {

        fishPostRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                boolean flag = false;
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    if (documentSnapshot.getId().equals(fishID)) {
                        flag = true;
                        loadNormalData();
                    }
                }
                if (!flag) {
                    bestDealRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                if (documentSnapshot.getId().equals(fishID)) {
                                    loadBestDealData();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void loadBestDealData() {
        mProgress.setMessage("Please wait...");
        mProgress.show();

        bestDealRef.document(fishID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Picasso.get().load(documentSnapshot.getString("fishImage")).fit().centerInside().into(fish_pic);
                fish_name.setText(documentSnapshot.getString("fishName"));
                fish_price.setText("₹" + documentSnapshot.getString("fishNewPrice") + "/Kg");
                if (documentSnapshot.getString("fishAvailability").equals("Available")) {
                    fish_availability.setTextColor(Color.parseColor("#00FF0C"));
                    fish_availability.setText("Currently in Stock");
                } else {
                    fish_availability.setTextColor(Color.parseColor("#FF0000"));
                    buyNowBTN.setEnabled(false);
                    buyNowBTN.setBackgroundColor(Color.parseColor("#FF0000"));
                    fish_availability.setText("Currently out of stock");
                }
                fish_time.setText("Posted on " + documentSnapshot.getString("fishPostDate")
                        + " at " + documentSnapshot.getString("fishPostTime"));
                seller_name.setText("Posted by " + documentSnapshot.getString("sellerName"));
                if (documentSnapshot.getString("fishPostRating").equals("0")) {
                    ratingBar.setVisibility(View.GONE);
                    rating.setVisibility(View.GONE);
                    rating_tv.setText("Not rated yet.");
                } else {
                    ratingBar.setVisibility(View.VISIBLE);
                    rating.setVisibility(View.VISIBLE);
                    ratingBar.setRating(Float.parseFloat(documentSnapshot.getString("fishPostRating")));
                    rating.setText("(" + documentSnapshot.getString("fishPostRating") + ")");
                }

                mProgress.dismiss();
            }
        });

    }

    private void loadNormalData() {
        mProgress.setMessage("Please wait...");
        mProgress.show();

        fishPostRef.document(fishID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Picasso.get().load(value.getString("fishImage")).fit().centerInside().into(fish_pic);
                fish_name.setText(value.getString("fishName"));
                fish_price.setText("₹" + value.getString("fishPrice") + "/Kg");
                if (value.getString("fishAvailability").equals("Available")) {
                    fish_availability.setTextColor(Color.parseColor("#00FF0C"));
                    fish_availability.setText("Currently in Stock");
                } else {
                    fish_availability.setTextColor(Color.parseColor("#FF0000"));
                    buyNowBTN.setEnabled(false);
                    buyNowBTN.setBackgroundColor(Color.parseColor("#FF0000"));
                    fish_availability.setText("Currently out of stock");
                }
                fish_time.setText("Posted on " + value.getString("fishPostDate")
                        + " at " + value.getString("fishPostTime"));
                seller_name.setText("Posted by " + value.getString("sellerName"));
                if (value.getString("fishPostRating").equals("0")) {
                    ratingBar.setVisibility(View.GONE);
                    rating.setVisibility(View.GONE);
                    rating_tv.setText("Not rated yet.");
                } else {
                    ratingBar.setVisibility(View.VISIBLE);
                    rating.setVisibility(View.VISIBLE);
                    ratingBar.setRating(Float.parseFloat(value.getString("fishPostRating")));
                    rating.setText("(" + value.getString("fishPostRating") + ")");
                }
                mProgress.dismiss();
            }
        });

    }

    private void initValues() {
        buyNowBTN = findViewById(R.id.full_fish_buy_btn);
        fish_pic = findViewById(R.id.full_fish_pic);
        fish_name = findViewById(R.id.full_fish_name);
        fish_price = findViewById(R.id.full_fish_price);
        fish_availability = findViewById(R.id.full_fish_availability);
        fish_time = findViewById(R.id.full_fish_time);
        seller_name = findViewById(R.id.full_fish_seller_name);
        fish_qty = findViewById(R.id.full_fish_qty);
        fish_qty.setText("0.0");
        rating_tv = findViewById(R.id.full_fish_rating_tv);
        addBTN = findViewById(R.id.full_fish_qty_plus);
        rating = findViewById(R.id.full_fish_rating);
        ratingBar = findViewById(R.id.full_fish_rating_bar);
        removeBTN = findViewById(R.id.full_fish_qty_remove);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        mStorageRef = FirebaseStorage.getInstance().getReference().child("Market Fish Photos");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fishPostRef = db.collection("Fish Posts");
        bestDealRef = db.collection("Best Deals");

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}