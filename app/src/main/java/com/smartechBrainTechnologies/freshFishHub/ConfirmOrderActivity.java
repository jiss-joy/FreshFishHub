package com.smartechBrainTechnologies.freshFishHub;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    private TextView fishName, availability, price, qty, total, name, building, area, landmark, city, pin, status, noAddress, toolbarTitle;
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
    private Map<String, String> orderAddress = new HashMap<>();
    private Map<String, String> orderDetails = new HashMap<>();
    private boolean addressFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);


        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
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

                if (addressFlag) {
                    noAddress.setTextColor(getResources().getColor(R.color.color_torquise));
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
                DateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY");
                String currentDate = dateFormat.format(date);

                Date time = Calendar.getInstance().getTime();
                DateFormat timeFormat = new SimpleDateFormat("hh:MM a");
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
                orderDetails.put("orderStatus", "Placed");
                orderDetails.put("orderFishName", documentSnapshot.getString("fishName"));
                orderDetails.put("orderDate", currentDate);
                orderDetails.put("orderTime", currentTime);
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
                                        orderAddress.put("addressArea", documentSnapshot.getString("addressArea"));
                                        orderAddress.put("addressBuilding", documentSnapshot.getString("addressBuilding"));
                                        orderAddress.put("addressCity", documentSnapshot.getString("addressCity"));
                                        orderAddress.put("addressCountry", documentSnapshot.getString("addressCountry"));
                                        orderAddress.put("addressLandmark", documentSnapshot.getString("addressLandmark"));
                                        orderAddress.put("addressName", documentSnapshot.getString("addressName"));
                                        orderAddress.put("addressPin", documentSnapshot.getString("addressPin"));
                                        orderAddress.put("addressState", documentSnapshot.getString("addressState"));
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
                DateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY");
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
                orderDetails.put("orderStatus", "Placed");
                orderDetails.put("orderFishName", documentSnapshot1.getString("fishName"));
                orderDetails.put("orderDate", currentDate);
                orderDetails.put("orderTime", currentTime);
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
                                        orderAddress.put("addressArea", documentSnapshot2.getString("addressArea"));
                                        orderAddress.put("addressBuilding", documentSnapshot2.getString("addressBuilding"));
                                        orderAddress.put("addressCity", documentSnapshot2.getString("addressCity"));
                                        orderAddress.put("addressCountry", documentSnapshot2.getString("addressCountry"));
                                        orderAddress.put("addressLandmark", documentSnapshot2.getString("addressLandmark"));
                                        orderAddress.put("addressName", documentSnapshot2.getString("addressName"));
                                        orderAddress.put("addressPin", documentSnapshot2.getString("addressPin"));
                                        orderAddress.put("addressState", documentSnapshot2.getString("addressState"));
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
                            building.setText(documentSnapshot.getString("addressBuilding"));
                            area.setText(documentSnapshot.getString("addressArea"));
                            landmark.setText(documentSnapshot.getString("addressLandmark"));
                            city.setText(documentSnapshot.getString("addressCity"));
                            pin.setText(documentSnapshot.getString("addressPin"));
                            status.setText("Default");

                        }
                    }
                }
            }
        });

        if (dealType == 1) {
            loadBestOrderSummary();
        } else if (dealType == 2) {
            loadNormalOrderSummary();
        } else {
            checkDealType();
        }
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
                orderTotal = String.valueOf(Float.parseFloat(fishNewPrice) * Float.parseFloat(fishQty));
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
                orderTotal = String.valueOf(Float.parseFloat(fishPrice) * Float.parseFloat(fishQty));
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
        placeOrderBTN = (LinearLayout) findViewById(R.id.confirm_order_place_order_btn);
        optionsBTN = (ImageButton) findViewById(R.id.confirm_order_address_options_btn);
        addressCard = (CardView) findViewById(R.id.confirm_order_address_card);
        noAddress = (TextView) findViewById(R.id.confirm_order_no_address_tv);
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