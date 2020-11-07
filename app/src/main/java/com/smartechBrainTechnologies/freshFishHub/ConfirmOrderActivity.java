package com.smartechBrainTechnologies.freshFishHub;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ConfirmOrderActivity extends AppCompatActivity {

    public static float DELIVERY_CHARGE;
    private final Map<String, String> orderAddress = new HashMap<>();
    private LinearLayout placeOrderBTN;
    private ProgressDialog mProgress;
    private ImageButton optionsBTN;
    private CardView addressCard;

    private FirebaseFirestore db;
    private FirebaseDatabase fb;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private CollectionReference fishPostRef;
    private CollectionReference orderRef;
    private CollectionReference bestDealsRef;
    private DatabaseReference notificationRef;

    private String fishID;
    private String fishQty;
    private String fishPrice;
    private String fishNewPrice;
    private int dealType;
    private String orderTotal;
    private final Map<String, String> orderDetails = new HashMap<>();
    private TextView fishName, availability, price, qty, total, name, address, contact, status,
            address_delivery_status, noAddress, toolbarTitle, delivery_charge_tv, delivery_charge, total_tv;
    private boolean addressFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);


        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Order Summary");

        Intent intent = getIntent();
        fishID = intent.getStringExtra("FISH ID");
        fishQty = intent.getStringExtra("FISH QTY");
        dealType = intent.getIntExtra("DEAL TYPE", 0);

        initValues();

        loadAddress();

        optionsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ConfirmOrderActivity.this, v);
                popupMenu.inflate(R.menu.menu_address_change);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_change_address:
                                Intent intent = new Intent(ConfirmOrderActivity.this, MyAddressesActivity.class);
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

        placeOrderBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrderBTN.setClickable(true);
                placeOrderBTN.setAnimation(new Animation() {
                    @Override
                    protected Animation clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                if (addressFlag) {
                    noAddress.setTextColor(getResources().getColor(R.color.color_red));
                    Toast.makeText(ConfirmOrderActivity.this, "Please add an address to continue.", Toast.LENGTH_LONG).show();
                } else {
                    if (dealType == 1) {
                        placeBestOrder();
                    } else if (dealType == 2) {
                        placeNormalOrder();
                    } else {
                        checkForDeal();
                    }
                }
            }
        });

        noAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConfirmOrderActivity.this, MyAddressesActivity.class));
            }
        });

    }


    private void checkForDeal() {
        fishPostRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                boolean flag = false;
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    if (documentSnapshot.getId().equals(fishID)) {
                        flag = true;
                        placeNormalOrder();
                    }
                }
                if (!flag) {
                    placeBestOrder();
                }
            }
        });
    }

    private void placeBestOrder() {
        mProgress.setMessage("Placing Order...");
        mProgress.show();
        bestDealsRef.document(fishID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Random random = new Random();
                String deliveryCode = String.valueOf(random.nextInt(9000) + 1000);

                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                String currentDate = dateFormat.format(date);

                Date time = Calendar.getInstance().getTime();
                DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                String currentTime = timeFormat.format(time);

                DocumentSnapshot documentSnapshot = task.getResult();
                final String sellerID = documentSnapshot.getString("sellerID");
                orderDetails.put("orderFishPrice", documentSnapshot.getString("fishNewPrice"));
                orderDetails.put("orderSellerID", documentSnapshot.getString("sellerID"));
                orderDetails.put("orderFishImage", documentSnapshot.getString("fishImage"));
                orderDetails.put("orderFishID", fishID);
                orderDetails.put("orderFishQty", fishQty);
                orderDetails.put("orderType", "Best");
                orderDetails.put("orderConsumerID", currentUser.getUid());
                orderDetails.put("orderStatus", "B");
                orderDetails.put("orderFishName", documentSnapshot.getString("fishName"));
                orderDetails.put("orderDate", currentDate);
                orderDetails.put("orderTime", currentTime);
                orderDetails.put("deliveryCharge", String.valueOf(DELIVERY_CHARGE));
                orderDetails.put("orderDeliveryCode", deliveryCode);
                orderRef.add(orderDetails).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        final String orderID = task.getResult().getId();
                        addressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                    if (documentSnapshot.getString("addressStatus").equals("Default")) {
                                        orderAddress.put("addressName", documentSnapshot.getString("addressName"));
                                        orderAddress.put("address", documentSnapshot.getString("address"));
                                        orderAddress.put("addressContact", documentSnapshot.getString("addressContact"));
                                        orderAddress.put("addressLatitude", documentSnapshot.getString("addressLatitude"));
                                        orderAddress.put("addressLongitude", documentSnapshot.getString("addressLongitude"));
                                        orderAddress.put("addressID", documentSnapshot.getId());
                                        orderRef.document(orderID).collection("Order Address").document(documentSnapshot.getId()).set(orderAddress)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            HashMap<String, String> orderNotification = new HashMap<>();
                                                            orderNotification.put("senderID", currentUser.getUid());
                                                            orderNotification.put("notificationType", "orderPlaced");
                                                            notificationRef.child("Placed Notifications").child(sellerID).push().setValue(orderNotification);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
        mProgress.dismiss();
        finish();
        startActivity(new Intent(ConfirmOrderActivity.this, OrderPlacedActivity.class));
    }


    private void placeNormalOrder() {
        mProgress.setMessage("Placing Order...");
        mProgress.show();
        fishPostRef.document(fishID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Random random = new Random();
                String deliveryCode = String.valueOf(random.nextInt(9000) + 1000);

                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                String currentDate = dateFormat.format(date);

                Date time = Calendar.getInstance().getTime();
                DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                String currentTime = timeFormat.format(time);

                DocumentSnapshot documentSnapshot1 = task.getResult();
                final String sellerID = documentSnapshot1.getString("sellerID");
                orderDetails.put("orderFishPrice", documentSnapshot1.getString("fishPrice"));
                orderDetails.put("orderSellerID", sellerID);
                orderDetails.put("orderFishImage", documentSnapshot1.getString("fishImage"));
                orderDetails.put("orderFishID", fishID);
                orderDetails.put("orderFishQty", fishQty);
                orderDetails.put("orderType", "Normal");
                orderDetails.put("orderConsumerID", currentUser.getUid());
                orderDetails.put("orderStatus", "B");
                orderDetails.put("orderFishName", documentSnapshot1.getString("fishName"));
                orderDetails.put("orderDate", currentDate);
                orderDetails.put("orderTime", currentTime);
                orderDetails.put("deliveryCharge", String.valueOf(DELIVERY_CHARGE));
                orderDetails.put("orderDeliveryCode", deliveryCode);
                orderRef.add(orderDetails).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        final String orderID = task.getResult().getId();
                        addressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (final DocumentSnapshot documentSnapshot2 : task.getResult().getDocuments()) {
                                    if (documentSnapshot2.getString("addressStatus").equals("Default")) {
                                        orderAddress.put("addressName", documentSnapshot2.getString("addressName"));
                                        orderAddress.put("address", documentSnapshot2.getString("address"));
                                        orderAddress.put("addressContact", documentSnapshot2.getString("addressContact"));
                                        orderAddress.put("addressLatitude", documentSnapshot2.getString("addressLatitude"));
                                        orderAddress.put("addressLongitude", documentSnapshot2.getString("addressLongitude"));
                                        orderAddress.put("addressID", documentSnapshot2.getId());
                                        orderRef.document(orderID).collection("Order Address").document(documentSnapshot2.getId()).set(orderAddress)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            HashMap<String, String> orderNotification = new HashMap<>();
                                                            orderNotification.put("senderID", currentUser.getUid());
                                                            orderNotification.put("notificationType", "orderPlaced");
                                                            notificationRef.child("Placed Notifications").child(sellerID).push().setValue(orderNotification);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
        mProgress.dismiss();
        finish();
        startActivity(new Intent(ConfirmOrderActivity.this, OrderPlacedActivity.class));
    }

    private void loadAddress() {
        mProgress.setMessage("Preparing Order Summary...");
        mProgress.setCancelable(false);
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
                            address.setText(documentSnapshot.getString("address"));
                            contact.setText(documentSnapshot.getString("addressContact"));
                            if (documentSnapshot.getString("addressDeliveryStatus").equals("No Delivery")) {
                                address_delivery_status.setVisibility(View.VISIBLE);
                                delivery_charge_tv.setVisibility(View.GONE);
                                delivery_charge.setVisibility(View.GONE);
                                total_tv.setVisibility(View.GONE);
                                total.setVisibility(View.GONE);
                                placeOrderBTN.setEnabled(false);
                            } else if (documentSnapshot.getString("addressDeliveryStatus").equals("Delivery Charge")) {
                                address_delivery_status.setVisibility(View.GONE);
                                delivery_charge_tv.setVisibility(View.VISIBLE);
                                delivery_charge.setVisibility(View.VISIBLE);
                                total_tv.setVisibility(View.VISIBLE);
                                total.setVisibility(View.VISIBLE);
                                placeOrderBTN.setEnabled(true);
                                DELIVERY_CHARGE = 50.0f;
                            } else {
                                address_delivery_status.setVisibility(View.GONE);
                                delivery_charge_tv.setVisibility(View.GONE);
                                delivery_charge.setVisibility(View.GONE);
                                total_tv.setVisibility(View.VISIBLE);
                                total.setVisibility(View.VISIBLE);
                                placeOrderBTN.setEnabled(true);
                                DELIVERY_CHARGE = 0.0f;
                            }
                            status.setText("Default");

                        }
                    }
                }
                if (dealType == 1) {
                    loadBestOrderSummary();
                } else if (dealType == 2) {
                    loadNormalOrderSummary();
                } else {
                    checkDealType();
                }
            }
        });
    }

    private void checkDealType() {
        fishPostRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                boolean flag = false;
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    if (documentSnapshot.getId().equals(fishID)) {
                        flag = true;
                        loadNormalOrderSummary();
                    }
                }
                if (!flag) {
                    loadBestOrderSummary();
                }
            }
        });
    }

    private void loadBestOrderSummary() {
        bestDealsRef.document(fishID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                fishName.setText(value.getString("fishName"));
                if (value.getString("fishAvailability").equals("Available")) {
                    availability.setTextColor(Color.parseColor("#00FF0C"));
                    availability.setText("Currently in Stock");
                } else {
                    informUser();
                    availability.setTextColor(Color.parseColor("#FF0000"));
                    availability.setText("Currently out of stock");
                }
                fishNewPrice = value.getString("fishNewPrice");
                orderTotal = String.valueOf(Float.parseFloat(fishNewPrice) * Float.parseFloat(fishQty) + DELIVERY_CHARGE);
                price.setText("₹" + fishNewPrice);
                qty.setText(fishQty + " KG");
                total.setText("₹" + orderTotal);
                mProgress.dismiss();
            }
        });
    }

    private void loadNormalOrderSummary() {
        fishPostRef.document(fishID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                fishName.setText(value.getString("fishName"));
                if (value.getString("fishAvailability").equals("Available")) {
                    availability.setTextColor(getResources().getColor(R.color.color_green));
                    availability.setText("Currently in Stock");
                } else {
                    informUser();
                    availability.setTextColor(getResources().getColor(R.color.color_red));
                    availability.setText("Out of Stock");
                }
                fishPrice = value.getString("fishPrice");
                orderTotal = String.valueOf(Float.parseFloat(fishPrice) * Float.parseFloat(fishQty) + DELIVERY_CHARGE);
                price.setText("₹" + fishPrice);
                qty.setText(fishQty + " KG");
                total.setText("₹" + orderTotal);
                mProgress.dismiss();
            }
        });
    }

    private void informUser() {
        Dialog dialog = new Dialog(ConfirmOrderActivity.this);
        dialog.setContentView(R.layout.popup_oos_info);
        Window window = dialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ExtendedFloatingActionButton okBTN = dialog.findViewById(R.id.oos_ok_btn);
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
        fishName = findViewById(R.id.order_fish_name);
        availability = findViewById(R.id.order_fish_availability);
        price = findViewById(R.id.order_fish_price);
        qty = findViewById(R.id.order_fish_qty);
        total = findViewById(R.id.order_total);
        name = findViewById(R.id.address_name);
        address = findViewById(R.id.address);
        contact = findViewById(R.id.address_contact);
        address_delivery_status = findViewById(R.id.no_delivery_tv);
        status = findViewById(R.id.address_status);
        placeOrderBTN = findViewById(R.id.confirm_order_place_order_btn);
        optionsBTN = findViewById(R.id.address_options_btn);
        addressCard = findViewById(R.id.confirm_order_address_card);
        noAddress = findViewById(R.id.confirm_order_no_address_tv);
        delivery_charge_tv = findViewById(R.id.order_delivery_charge_tv);
        delivery_charge = findViewById(R.id.order_delivery_charge);
        total_tv = findViewById(R.id.order_total_tv);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        fb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fishPostRef = db.collection("Fish Posts");
        addressRef = db.collection("Consumers").document(currentUser.getUid()).collection("My Addresses");
        orderRef = db.collection("Orders");
        bestDealsRef = db.collection("Best Deals");
        notificationRef = fb.getReference().child("Notifications");

    }
}