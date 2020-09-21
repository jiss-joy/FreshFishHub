package com.smartechBrainTechnologies.freshFishHub;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MarketFragment extends Fragment implements AdapterShortFishDetails.OnFishSelectedListener, AdapterBestDealsFish.OnBestDealListener {

    private RecyclerView marketRecycler;
    private RecyclerView bestDealsRecycler;
    private ProgressDialog mProgress;
    private ImageView noFishImage;
    private TextView noFishTV;
    private ExtendedFloatingActionButton searchBar;


    private FirebaseFirestore db;
    private CollectionReference fishPostRef;
    private CollectionReference bestDealsRef;
    private ArrayList<ModelShortFishDetails> fishDetailsList = new ArrayList<>();
    private ArrayList<ModelBestDealsFish> bestDeals = new ArrayList<>();
    private AdapterShortFishDetails mAdapter;
    private AdapterBestDealsFish bAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        initValues(view);

        setUpBestDeals();

        setUpRecycler();

        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchEngineActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().getWindow().setExitTransition(new AutoTransition());
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    private void setUpBestDeals() {
        bestDealsRef.orderBy("fishPostTime", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                bestDeals.clear();
                for (DocumentSnapshot bestDeal : value.getDocuments()) {
                    String fishID = bestDeal.getId();
                    String fishImage = bestDeal.getString("fishImage");
                    String fishName = bestDeal.getString("fishName");
                    String fishOldPrice = bestDeal.getString("fishOldPrice");
                    String fishNewPrice = bestDeal.getString("fishNewPrice");
                    String fishAvailability = bestDeal.getString("fishAvailability");

                    ModelBestDealsFish fishDetail = new ModelBestDealsFish(fishID, fishImage, fishName, fishOldPrice, fishNewPrice, fishAvailability);
                    bestDeals.add(fishDetail);
                }
                noFishTV.setVisibility(View.GONE);
                noFishImage.setVisibility(View.GONE);
                bAdapter = new AdapterBestDealsFish(getContext(), bestDeals, MarketFragment.this);
                bestDealsRecycler.setAdapter(bAdapter);
                bAdapter.notifyDataSetChanged();
            }
        });
    }


    private void setUpRecycler() {
        mProgress.setMessage("Please wait...");
        mProgress.show();
        fishPostRef.orderBy("fishPostTime", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                fishDetailsList.clear();
                for (DocumentSnapshot fishDetails : value.getDocuments()) {
                    String fishID = fishDetails.getId();
                    String fishImage = fishDetails.getString("fishImage");
                    String fishName = fishDetails.getString("fishName");
                    String fishPrice = fishDetails.getString("fishPrice");
                    String fishAvailability = fishDetails.getString("fishAvailability");

                    ModelShortFishDetails fishDetail = new ModelShortFishDetails(fishID, fishImage, fishName, fishPrice, fishAvailability);
                    fishDetailsList.add(fishDetail);
                }
                if (fishDetailsList.isEmpty()) {
                    marketRecycler.setVisibility(View.GONE);
                    Picasso.get().load(R.drawable.empty_fish).into(noFishImage);
                    noFishTV.setText("You have not posted any fishes until now\n" +
                            "Tap the '+' button to add a new fish");
                } else {
                    noFishTV.setVisibility(View.GONE);
                    noFishImage.setVisibility(View.GONE);
                    mAdapter = new AdapterShortFishDetails(getContext(), fishDetailsList, MarketFragment.this);
                    marketRecycler.setAdapter(mAdapter);
                    marketRecycler.setVerticalFadingEdgeEnabled(false);
                    mAdapter.notifyDataSetChanged();
                }

            }

        });
        mProgress.dismiss();
    }

    private void initValues(View view) {
        marketRecycler = (RecyclerView) view.findViewById(R.id.market_recycler);
        bestDealsRecycler = (RecyclerView) view.findViewById(R.id.best_deals_recycler);
        mProgress = new ProgressDialog(getContext());
        mProgress.setCancelable(false);
        noFishImage = (ImageView) view.findViewById(R.id.market_no_fish_image);
        noFishTV = (TextView) view.findViewById(R.id.market_no_fish_tv);
        searchBar = (ExtendedFloatingActionButton) view.findViewById(R.id.market_search_bar);

        db = FirebaseFirestore.getInstance();
        fishPostRef = db.collection("Fish Posts");
        bestDealsRef = db.collection("Best Deals");

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        gridLayoutManager.setSmoothScrollbarEnabled(true);
        marketRecycler.setLayoutManager(gridLayoutManager);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        bestDealsRecycler.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onFishClick(int position) {
        String fishID = fishDetailsList.get(position).getFishID();
        Intent intent = new Intent(getContext(), FullFishDetailsActivity.class);
        intent.putExtra("FISH ID", fishID);
        intent.putExtra("DEAL TYPE", 2);
        startActivity(intent);
    }


    @Override
    public void onBestDealClick(int position) {
        String fishID = bestDeals.get(position).getFishID();
        Intent intent = new Intent(getContext(), FullFishDetailsActivity.class);
        intent.putExtra("FISH ID", fishID);
        intent.putExtra("DEAL TYPE", 1);
        startActivity(intent);
    }
}