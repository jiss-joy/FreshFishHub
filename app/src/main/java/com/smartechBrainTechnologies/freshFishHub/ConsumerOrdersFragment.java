package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ConsumerOrdersFragment extends Fragment {

    private RecyclerView consumerOrderRecycler;
    private Toolbar toolbar;
    private ProgressDialog mProgress;
    private ImageView noOrderImage;
    private TextView noOrderTV;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private CollectionReference orderRef;
    private ArrayList<ModelShortOrder> orderList = new ArrayList<>();
    private AdapterShortOrder mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consumer_orders, container, false);

        initValues(view);

        setUpRecycler();

        setUpToolbar();

        return view;
    }

    private void setUpToolbar() {
        toolbar.inflateMenu(R.menu.menu_toolbar_market);
        toolbar.setTitle("Your Orders");
        Menu menu = toolbar.getMenu();
        MenuItem menuItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setIconifiedByDefault(false);
        searchView.setIconified(true);
    }

    private void setUpRecycler() {
        mProgress.setMessage("Please wait...");
        mProgress.show();
        orderRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                orderList.clear();
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    if (documentSnapshot.getString("orderConsumerID").equals(user.getUid())) {
                        String fishImage = documentSnapshot.getString("orderFishImage");
                        String fishName = documentSnapshot.getString("orderFishName");
                        String fishPrice = documentSnapshot.getString("orderFishPrice");
                        String fishQty = documentSnapshot.getString("orderFishQty");
                        String orderStatus = documentSnapshot.getString("orderStatus");

                        ModelShortOrder order = new ModelShortOrder(fishImage, fishName, fishQty, fishPrice, orderStatus);
                        orderList.add(order);
                    }
                }
                if (orderList.isEmpty()) {
                    consumerOrderRecycler.setVisibility(View.GONE);
                    Picasso.get().load(R.drawable.empty_order).into(noOrderImage);
                    noOrderTV.setText("You do not have any order.\nPlease place a new order in the market now!");
                } else {
                    noOrderTV.setVisibility(View.GONE);
                    noOrderImage.setVisibility(View.GONE);
                    mAdapter = new AdapterShortOrder(getContext(), orderList);
                    consumerOrderRecycler.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        mProgress.dismiss();
    }

    private void initValues(View view) {
        consumerOrderRecycler = (RecyclerView) view.findViewById(R.id.consumer_order_recycler);
        toolbar = (Toolbar) view.findViewById(R.id.consumer_order_toolbar);
        mProgress = new ProgressDialog(getContext());
        mProgress.setCancelable(false);
        noOrderImage = (ImageView) view.findViewById(R.id.consumer_order_no_orders_image);
        noOrderTV = (TextView) view.findViewById(R.id.consumer_order_no_orders_tv);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        orderRef = db.collection("Orders");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        consumerOrderRecycler.setLayoutManager(linearLayoutManager);
    }
}
