package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class FullFishDetailsActivity extends AppCompatActivity {

    private ImageView fish_pic;
    private TextView fish_name, fish_price, fish_availability, fish_location, fish_time, seller_name;
    private ExtendedFloatingActionButton buyNowBTN;
    private ProgressDialog mProgress;

    private String fishID;

    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference fishPostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_fish_details);

        Intent intent = getIntent();
        fishID = intent.getStringExtra("FISH ID");

        initValues();

        loadData();

        buyNowBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(FullFishDetailsActivity.this, BuyFishActivity.class);
                intent1.putExtras(getIntent().getExtras());
                startActivity(intent1);
            }
        });
    }

    private void loadData() {
        mProgress.setMessage("Please wait...");
        mProgress.show();

        fishPostRef.document(fishID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Picasso.get().load(documentSnapshot.getString("fishImage")).fit().centerInside().into(fish_pic);
                fish_name.setText(documentSnapshot.getString("fishName"));
                fish_price.setText("₹" + documentSnapshot.getString("fishPrice") + "/Kg");
                if (documentSnapshot.getString("fishAvailability").equals("Available")) {
                    fish_availability.setTextColor(Color.parseColor("#00FF0C"));
                } else {
                    fish_availability.setTextColor(Color.parseColor("#FF0000"));
                    buyNowBTN.setEnabled(false);
                    buyNowBTN.setBackgroundColor(Color.parseColor("#FF0000"));
                }
                fish_availability.setText(documentSnapshot.getString("fishAvailability"));
                fish_location.setText("Available at: " + documentSnapshot.getString("fishLocation"));
                fish_time.setText("Posted on " + documentSnapshot.getString("fishPostDate")
                        + " at " + documentSnapshot.getString("fishPostTime"));
                seller_name.setText("Posted by " + documentSnapshot.getString("sellerName"));
                mProgress.dismiss();
            }
        });

    }

    private void initValues() {
        buyNowBTN = (ExtendedFloatingActionButton) findViewById(R.id.full_fish_buy_btn);
        fish_pic = (ImageView) findViewById(R.id.full_fish_pic);
        fish_name = (TextView) findViewById(R.id.full_fish_name);
        fish_price = (TextView) findViewById(R.id.full_fish_price);
        fish_availability = (TextView) findViewById(R.id.full_fish_availability);
        fish_location = (TextView) findViewById(R.id.full_fish_location);
        fish_time = (TextView) findViewById(R.id.full_fish_time);
        seller_name = (TextView) findViewById(R.id.full_fish_seller_name);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        mStorageRef = FirebaseStorage.getInstance().getReference().child("Market Fish Photos");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fishPostRef = db.collection("Fish Posts");

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}