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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {
    private TextView toolbarTitle, orderStatusLong, orderStatusShort, order_ID, time, fName, fPrice, fQty,
            totalPrice, name, building, area, city, pin, landmark;
    private ExtendedFloatingActionButton cancelBTN, deliverBTN;
    private ProgressDialog mProgress;

    private FirebaseFirestore db;
    private FirebaseDatabase fb;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private CollectionReference orderRef;

    private String orderID, userDeliveryCode;
    private Map<String, Object> orderDetails = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Order Summary");

        Intent intent = getIntent();
        orderID = intent.getStringExtra("ORDER ID");

        initValues();

        loadAddress();

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrder();
            }
        });

        deliverBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeliveryCode();
            }
        });
    }

    private void getDeliveryCode() {
        final Dialog dialog = new Dialog(OrderDetailsActivity.this);
        dialog.setContentView(R.layout.popup_delivery_code);
        Window window = dialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final EditText deliveryCode_et = (EditText) dialog.findViewById(R.id.delivery_code);
        ExtendedFloatingActionButton submitBTN = (ExtendedFloatingActionButton) dialog.findViewById(R.id.delivery_code_next);
        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDeliveryCode = deliveryCode_et.getText().toString();
                if (userDeliveryCode.isEmpty()) {
                    Toast.makeText(OrderDetailsActivity.this, "Please enter the delivery code.", Toast.LENGTH_SHORT).show();
                } else {
                    mProgress.setMessage("Authenticating Code...");
                    mProgress.show();
                    dialog.dismiss();
                    authenticateDeliveryCode();
                }
            }
        });
        dialog.show();
    }

    private void authenticateDeliveryCode() {
        orderRef.document(orderID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                final String fishID = task.getResult().getString("orderFishID");
                final String type = task.getResult().getString("orderType");
                if (task.isSuccessful()) {
                    if (userDeliveryCode.equals(task.getResult().getString("orderDeliveryCode"))) {
                        orderDetails.put("orderStatus", "Delivered");
                        orderRef.document(orderID).update(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                cancelBTN.setVisibility(View.GONE);
                                deliverBTN.setVisibility(View.GONE);
                                if (type.equals("Normal")) {
                                    startActivity(new Intent(OrderDetailsActivity.this, RatingActivity.class)
                                            .putExtra("FISH ID", fishID).putExtra("FISH TYPE", 1));
                                } else {
                                    startActivity(new Intent(OrderDetailsActivity.this, RatingActivity.class)
                                            .putExtra("FISH ID", fishID).putExtra("FISH TYPE", 2));
                                }
                            }
                        });
                    } else {
                        mProgress.dismiss();
                        Toast.makeText(OrderDetailsActivity.this, "Invalid Delivery Code", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void cancelOrder() {
        orderDetails.put("orderStatus", "Cancelled");
        orderRef.document(orderID).update(orderDetails);
        cancelBTN.setVisibility(View.GONE);
        deliverBTN.setVisibility(View.GONE);
    }

    private void loadAddress() {
        mProgress.setMessage("Preparing Order Summary...");
        mProgress.setCancelable(false);
        mProgress.show();
        addressRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    if (documentSnapshot.getString("addressStatus").equals("Default")) {
                        name.setText(documentSnapshot.getString("addressName"));
                        building.setText(documentSnapshot.getString("addressBuilding"));
                        area.setText(documentSnapshot.getString("addressArea"));
                        landmark.setText(documentSnapshot.getString("addressLandmark"));
                        city.setText(documentSnapshot.getString("addressCity"));
                        pin.setText(documentSnapshot.getString("addressPin"));
                    }
                }
            }
        });

        loadOrderSummary();
    }

    private void loadOrderSummary() {
        orderRef.document(orderID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                switch (value.getString("orderStatus")) {
                    case "Placed":
                        orderStatusShort.setText("Placed");
                        orderStatusLong.setText(R.string.order_placed);
                        deliverBTN.setVisibility(View.GONE);
                        cancelBTN.setVisibility(View.VISIBLE);
                        break;
                    case "Accepted":
                        orderStatusShort.setText("Accepted");
                        orderStatusShort.setTextColor(getResources().getColor(R.color.color_dark_orange));
                        orderStatusShort.setBackgroundResource(R.drawable.custom_field_orange);
                        orderStatusLong.setText(R.string.order_accepted);
                        cancelBTN.setVisibility(View.GONE);
                        deliverBTN.setVisibility(View.GONE);
                        break;
                    case "Declined":
                        orderStatusShort.setText("Declined");
                        orderStatusShort.setTextColor(getResources().getColor(R.color.color_red));
                        orderStatusShort.setBackgroundResource(R.drawable.custom_field_red);
                        orderStatusLong.setText(R.string.order_declined);
                        cancelBTN.setVisibility(View.GONE);
                        deliverBTN.setVisibility(View.GONE);
                        break;
                    case "Delivered":
                        orderStatusShort.setText("Delivered");
                        orderStatusShort.setTextColor(getResources().getColor(R.color.color_dark_green));
                        orderStatusShort.setBackgroundResource(R.drawable.custom_field_green);
                        orderStatusLong.setText(R.string.order_delivered);
                        cancelBTN.setVisibility(View.GONE);
                        deliverBTN.setVisibility(View.GONE);
                        break;
                    case "Delivery":
                        orderStatusShort.setText("Delivery");
                        orderStatusShort.setTextColor(getResources().getColor(R.color.color_dark_orange));
                        orderStatusShort.setBackgroundResource(R.drawable.custom_field_orange);
                        orderStatusLong.setText(R.string.order_delivery);
                        cancelBTN.setVisibility(View.GONE);
                        deliverBTN.setVisibility(View.VISIBLE);
                        break;
                    case "Cancelled":
                        orderStatusShort.setText("Cancelled");
                        orderStatusShort.setTextColor(getResources().getColor(R.color.color_red));
                        orderStatusShort.setBackgroundResource(R.drawable.custom_field_red);
                        orderStatusLong.setText(R.string.order_cancelled);
                        cancelBTN.setVisibility(View.GONE);
                        deliverBTN.setVisibility(View.GONE);
                        break;
                }
                fName.setText(value.getString("orderFishName"));
                fQty.setText(value.getString("orderFishQty"));
                fPrice.setText(value.getString("orderFishPrice"));
                String total = String.valueOf(Float.parseFloat(value.getString("orderFishQty")) * Float.parseFloat(value.getString("orderFishPrice")));
                totalPrice.setText(total);
                order_ID.setText(value.getId());
                time.setText(value.getString("orderDate") + "at" + value.getString("orderTime"));
            }
        });
        mProgress.dismiss();
    }

    private void initValues() {
        order_ID = (TextView) findViewById(R.id.order_details_order_id);
        orderStatusLong = (TextView) findViewById(R.id.order_details_status_tv);
        orderStatusShort = (TextView) findViewById(R.id.order_details_status);
        fName = (TextView) findViewById(R.id.order_details_fish_name);
        fPrice = (TextView) findViewById(R.id.order_details_order_price);
        fQty = (TextView) findViewById(R.id.order_details_qty);
        name = (TextView) findViewById(R.id.order_details_name);
        area = (TextView) findViewById(R.id.order_details_area);
        building = (TextView) findViewById(R.id.order_details_building);
        city = (TextView) findViewById(R.id.order_details_city);
        pin = (TextView) findViewById(R.id.order_details_pin);
        landmark = (TextView) findViewById(R.id.order_details_landmark);
        time = (TextView) findViewById(R.id.order_details_order_time);
        totalPrice = (TextView) findViewById(R.id.order_details_total);
        cancelBTN = (ExtendedFloatingActionButton) findViewById(R.id.order_details_cancel_btn);
        deliverBTN = (ExtendedFloatingActionButton) findViewById(R.id.order_details_deliver_btn);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        fb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        addressRef = db.collection("Consumers").document(currentUser.getUid()).collection("My Addresses");
        orderRef = db.collection("Orders");
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}