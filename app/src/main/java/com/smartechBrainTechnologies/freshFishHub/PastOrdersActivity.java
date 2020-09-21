package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PastOrdersActivity extends AppCompatActivity implements AdapterShortOrder.OnOrderClickListener {

    private RecyclerView pastOrderRecycler;
    private ProgressDialog mProgress;
    private ImageView noOrderImage;
    private TextView noOrderTV, toolbarTitle;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private CollectionReference orderRef;
    private ArrayList<ModelShortOrder> orderList = new ArrayList<>();
    private AdapterShortOrder mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_orders);

        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Past Orders");

        initValues();

        setUpRecycler();

    }

    private void setUpRecycler() {
        mProgress.setMessage("Please wait...");
        mProgress.show();
        orderRef.orderBy("orderDate", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                orderList.clear();
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    if (documentSnapshot.getString("orderConsumerID").equals(user.getUid())) {
                        String orderID = documentSnapshot.getId();
                        String fishImage = documentSnapshot.getString("orderFishImage");
                        String fishName = documentSnapshot.getString("orderFishName");
                        String fishPrice = documentSnapshot.getString("orderFishPrice");
                        String fishQty = documentSnapshot.getString("orderFishQty");
                        String orderStatus = documentSnapshot.getString("orderStatus");

                        ModelShortOrder order = new ModelShortOrder(orderID, fishImage, fishName, fishQty, fishPrice, orderStatus);
                        orderList.add(order);
                    }
                }
                if (orderList.isEmpty()) {
                    pastOrderRecycler.setVisibility(View.GONE);
                    Picasso.get().load(R.drawable.empty_order).into(noOrderImage);
                    noOrderTV.setText("You have not ordered anything until now.\nPlease place a new order in the market now!");
                } else {
                    noOrderTV.setVisibility(View.GONE);
                    noOrderImage.setVisibility(View.GONE);
                    mAdapter = new AdapterShortOrder(PastOrdersActivity.this, orderList, PastOrdersActivity.this);
                    pastOrderRecycler.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        mProgress.dismiss();
    }

    private void initValues() {
        pastOrderRecycler = (RecyclerView) findViewById(R.id.past_order_recycler);
        mProgress = new ProgressDialog(PastOrdersActivity.this);
        mProgress.setCancelable(false);
        noOrderImage = (ImageView) findViewById(R.id.past_order_no_orders_image);
        noOrderTV = (TextView) findViewById(R.id.past_order_no_orders_tv);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        orderRef = db.collection("Orders");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PastOrdersActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        pastOrderRecycler.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void OnOrderClick(int position) {
        String orderID = orderList.get(position).getOrderID();
        startActivity(new Intent(PastOrdersActivity.this, OrderDetailsActivity.class)
                .putExtra("ORDER ID", orderID));
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}