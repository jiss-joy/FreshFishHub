package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class BuyFishActivity extends AppCompatActivity {

    private EditText qty;
    private TextView name, building, area, landmark, city, pin, state, country, status, noAddress;
    private CardView addressCard;
    private ExtendedFloatingActionButton nextBTN;
    private ImageButton optionsBTN;
    private ProgressDialog mProgress;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private boolean addressFlag = false;

    private String fishID;
    private String fishQty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_fish);

        Intent intent = getIntent();
        fishID = intent.getStringExtra("FISH ID");

        initValues();

        loadData();

        optionsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(BuyFishActivity.this, v);
                popupMenu.inflate(R.menu.menu_address_change);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_change_address:
                                Intent intent = new Intent(BuyFishActivity.this, MyAddressesActivity.class);
                                startActivity(intent);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        nextBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fishQty = qty.getText().toString();
                if (fishQty.isEmpty()) {
                    Toast.makeText(BuyFishActivity.this, "Please enter quantity.", Toast.LENGTH_SHORT).show();
                } else if (addressFlag) {
                    Toast.makeText(BuyFishActivity.this, "Please add an address to deliver to.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent1 = new Intent(BuyFishActivity.this, ConfirmOrderActivity.class);
                    intent1.putExtras(getIntent().getExtras());
                    intent1.putExtra("FISH QTY", fishQty);
                    startActivity(intent1);
                }
            }
        });

        noAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BuyFishActivity.this, MyAddressesActivity.class));
            }
        });
    }

    private void loadData() {
        mProgress.setMessage("Please wait...");
        mProgress.show();
        addressRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.isEmpty()) {
                    addressFlag = true;
                    addressCard.setVisibility(View.GONE);
                    noAddress.setVisibility(View.VISIBLE);
                    noAddress.setText("You do not have any addresses.\nClick here to add now.");
                    mProgress.dismiss();
                } else {
                    noAddress.setVisibility(View.GONE);
                    addressCard.setVisibility(View.VISIBLE);
                    addressFlag = false;
                    for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                        if (documentSnapshot.getString("addressStatus").equals("Default")) {
                            name.setText(documentSnapshot.getString("addressName"));
                            building.setText(documentSnapshot.getString("addressBuilding"));
                            area.setText(documentSnapshot.getString("addressArea"));
                            landmark.setText(documentSnapshot.getString("addressLandmark"));
                            city.setText(documentSnapshot.getString("addressCity"));
                            pin.setText(documentSnapshot.getString("addressPin"));
                            status.setText("Default");
                            mProgress.dismiss();
                        }
                    }
                }
            }
        });
    }

    private void initValues() {
        qty = (EditText) findViewById(R.id.buy_fish_qty);
        name = (TextView) findViewById(R.id.buy_fish_address_name);
        building = (TextView) findViewById(R.id.buy_fish_address_building);
        area = (TextView) findViewById(R.id.buy_fish_address_area);
        landmark = (TextView) findViewById(R.id.buy_fish_address_landmark);
        city = (TextView) findViewById(R.id.buy_fish_address_city);
        pin = (TextView) findViewById(R.id.buy_fish_address_pin);
        state = (TextView) findViewById(R.id.buy_fish_address_state);
        country = (TextView) findViewById(R.id.buy_fish_address_country);
        status = (TextView) findViewById(R.id.buy_fish_address_status);
        noAddress = (TextView) findViewById(R.id.buy_fish_no_address_tv);
        nextBTN = (ExtendedFloatingActionButton) findViewById(R.id.buy_fish_confirm_btn);
        optionsBTN = (ImageButton) findViewById(R.id.buy_fish_address_options_btn);
        addressCard = (CardView) findViewById(R.id.buy_fish_address);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        addressRef = db.collection("Consumers").document(currentUser.getUid()).collection("My Addresses");
    }
}