package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ConsumerOrdersFragment extends Fragment implements AdapterShortOrder.OnOrderClickListener {

    private RecyclerView consumerOrderRecycler;
    private ProgressDialog mProgress;
    private ImageView noOrderImage;
    private TextView noOrderTV, toolbarTitle;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private CollectionReference orderRef;
    private final ArrayList<ModelShortOrder> orderList = new ArrayList<>();
    private AdapterShortOrder mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consumer_orders, container, false);

        mProgress = new ProgressDialog(getContext());
        mProgress.setCancelable(false);
        mProgress.setMessage("Please wait...");
        mProgress.show();

        toolbarTitle = (TextView) view.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Your Orders");

        initValues(view);

        setUpRecycler();

        return view;
    }


    private void setUpRecycler() {

        orderRef.orderBy("orderStatus", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                orderList.clear();
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                String currentDate = dateFormat.format(date);
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    if (documentSnapshot.getString("orderConsumerID").equals(user.getUid())) {
                        if (documentSnapshot.getString("orderDate").equals(currentDate)) {
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
                }
                if (orderList.isEmpty()) {
                    Picasso.get().load(R.drawable.empty_order).into(noOrderImage);
                    noOrderTV.setText("You do not have any order.\nPlease place a new order in the market now!");
                    noOrderImage.setVisibility(View.VISIBLE);
                    noOrderTV.setVisibility(View.VISIBLE);
                } else {
                    consumerOrderRecycler.setVisibility(View.VISIBLE);
                    mAdapter = new AdapterShortOrder(getContext(), orderList, ConsumerOrdersFragment.this);
                    consumerOrderRecycler.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
                mProgress.dismiss();
            }
        });
    }

    private void initValues(View view) {
        consumerOrderRecycler = view.findViewById(R.id.consumer_order_recycler);
        consumerOrderRecycler.setVisibility(View.GONE);
        noOrderImage = view.findViewById(R.id.consumer_order_no_orders_image);
        noOrderTV = view.findViewById(R.id.consumer_order_no_orders_tv);
        noOrderTV.setVisibility(View.GONE);
        noOrderImage.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        orderRef = db.collection("Orders");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        consumerOrderRecycler.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void OnOrderClick(int position) {
        String orderID = orderList.get(position).getOrderID();
        startActivity(new Intent(getContext(), OrderDetailsActivity.class)
                .putExtra("ORDER ID", orderID));
    }
}
