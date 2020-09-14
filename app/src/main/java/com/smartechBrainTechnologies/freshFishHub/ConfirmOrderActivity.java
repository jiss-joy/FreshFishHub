package com.smartechBrainTechnologies.freshFishHub;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ConfirmOrderActivity extends AppCompatActivity {

    private TextView fishName, availability, price, qty, total, name, building, area, landmark, city, pin, status;
    private ExtendedFloatingActionButton placeOrderBTN;
    private ProgressDialog mProgress;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private DocumentReference fishPostRef;
    private CollectionReference orderRef;

    private String fishID;
    private String fishQty;
    private String orderTotal;
    private Map<String, String> orderAddress = new HashMap<>();
    private Map<String, String> orderDetails = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        Intent intent = getIntent();
        fishID = intent.getStringExtra("FISH ID");
        fishQty = intent.getStringExtra("FISH QTY");

        initValues();

        loadAddress();

        placeOrderBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });
    }

    private void placeOrder() {
        mProgress.setMessage("Placing Order...");
        mProgress.show();
        fishPostRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Calendar calendar = Calendar.getInstance();
                String currentDate = DateFormat.getDateInstance().format(calendar.getTime());
                String currentTime = DateFormat.getTimeInstance().format(calendar.getTime());

                DocumentSnapshot documentSnapshot = task.getResult();
                orderDetails.put("orderFishLocation", documentSnapshot.getString("fishLocation"));
                orderDetails.put("orderFishPrice", documentSnapshot.getString("fishPrice"));
                orderDetails.put("orderSellerID", documentSnapshot.getString("sellerID"));
                orderDetails.put("orderFishImage", documentSnapshot.getString("fishImage"));
                orderDetails.put("orderFishID", fishID);
                orderDetails.put("orderFishQty", fishQty);
                orderDetails.put("orderConsumerID", currentUser.getUid());
                orderDetails.put("orderStatus", "Placed");
                orderDetails.put("orderFishName", documentSnapshot.getString("fishName"));
                orderDetails.put("orderDate", currentDate);
                orderDetails.put("orderTime", currentTime);
                orderRef.add(orderDetails).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        final String orderID = task.getResult().getId();
                        addressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                    if (documentSnapshot.getString("addressStatus").equals("Default")) {
                                        orderAddress.put("addressArea", documentSnapshot.getString("addressArea"));
                                        orderAddress.put("addressBuilding", documentSnapshot.getString("addressBuilding"));
                                        orderAddress.put("addressCity", documentSnapshot.getString("addressCity"));
                                        orderAddress.put("addressCountry", documentSnapshot.getString("addressCountry"));
                                        orderAddress.put("addressLandmark", documentSnapshot.getString("addressLandmark"));
                                        orderAddress.put("addressName", documentSnapshot.getString("addressName"));
                                        orderAddress.put("addressPin", documentSnapshot.getString("addressPin"));
                                        orderAddress.put("addressState", documentSnapshot.getString("addressState"));
                                        orderAddress.put("addressID", documentSnapshot.getId());
                                        orderRef.document(orderID).collection("Order Address").document(documentSnapshot.getId()).set(orderAddress);
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
        mProgress.dismiss();
        Toast.makeText(this, "Order placed successfully", Toast.LENGTH_SHORT).show();
        finish();
        startActivity(new Intent(ConfirmOrderActivity.this, MainActivity.class));
    }

    private void loadAddress() {
        addressRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                mProgress.setMessage("Preparing Order Summary...");
                mProgress.setCancelable(false);
                mProgress.show();
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
        });

        loadOrderSummary();
    }

    private void loadOrderSummary() {
        fishPostRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                fishName.setText(value.getString("fishName"));
                if (value.getString("fishAvailability").equals("Available")) {
                    availability.setTextColor(Color.parseColor("#00FF0C"));
                } else {
                    informUser();
                    availability.setTextColor(Color.parseColor("#FF0000"));
                }
                availability.setText(value.getString("fishAvailability"));
                String fishPrice = value.getString("fishPrice");
                orderTotal = String.valueOf(Float.parseFloat(fishPrice) * Float.parseFloat(fishQty));
                price.setText("₹" + fishPrice);
                qty.setText(fishQty + " KG");
                total.setText("₹" + orderTotal);
            }
        });
    }

    private void informUser() {
        Dialog dialog = new Dialog(ConfirmOrderActivity.this);
        dialog.setContentView(R.layout.popup_oos_info);
        Window window = dialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ExtendedFloatingActionButton okBTN = (ExtendedFloatingActionButton) dialog.findViewById(R.id.oos_ok_btn);
        okBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(ConfirmOrderActivity.this, MainActivity.class));
            }
        });
        mProgress.dismiss();
        dialog.show();
    }

    private void initValues() {
        fishName = (TextView) findViewById(R.id.confirm_order_fish_name);
        availability = (TextView) findViewById(R.id.confirm_order_fish_availability);
        price = (TextView) findViewById(R.id.confirm_order_fish_price);
        qty = (TextView) findViewById(R.id.confirm_order_fish_qty);
        total = (TextView) findViewById(R.id.confirm_order_total);
        name = (TextView) findViewById(R.id.confirm_order_address_name);
        building = (TextView) findViewById(R.id.confirm_order_address_building);
        area = (TextView) findViewById(R.id.confirm_order_address_area);
        landmark = (TextView) findViewById(R.id.confirm_order_address_landmark);
        city = (TextView) findViewById(R.id.confirm_order_address_city);
        pin = (TextView) findViewById(R.id.confirm_order_address_pin);
        status = (TextView) findViewById(R.id.confirm_order_address_status);
        placeOrderBTN = (ExtendedFloatingActionButton) findViewById(R.id.confirm_order_place_order_btn);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fishPostRef = db.collection("Fish Posts").document(fishID);
        addressRef = db.collection("Consumers").document(currentUser.getUid()).collection("My Addresses");
        orderRef = db.collection("Orders");

    }
}