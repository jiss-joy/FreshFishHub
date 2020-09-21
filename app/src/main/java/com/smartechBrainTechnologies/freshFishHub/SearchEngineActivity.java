package com.smartechBrainTechnologies.freshFishHub;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SearchEngineActivity extends AppCompatActivity implements AdapterShortFishDetails.OnFishSelectedListener {

    private FloatingSearchView searchView;
    private RecyclerView searchRecycler;

    private FirebaseFirestore db;
    private CollectionReference fishPostRef;
    private CollectionReference bestDealsRef;
    private AdapterShortFishDetails mAdapter;
    private ArrayList<ModelShortFishDetails> searchList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_engine);

        initValues();

        setUpRecycler();

        setUpSearch();

    }

    private void setUpSearch() {
        searchView.setSearchFocused(true);
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                mAdapter.getFilter().filter(newQuery);
            }
        });
    }

    private void setUpRecycler() {
        fishPostRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                searchList.clear();
                for (DocumentSnapshot normalDeals : value.getDocuments()) {
                    String fishID = normalDeals.getId();
                    String fishImage = normalDeals.getString("fishImage");
                    String fishName = normalDeals.getString("fishName");
                    String fishPrice = normalDeals.getString("fishPrice");
                    String fishAvailability = normalDeals.getString("fishAvailability");

                    ModelShortFishDetails fishDetail = new ModelShortFishDetails(fishID, fishImage, fishName, fishPrice, fishAvailability);
                    searchList.add(fishDetail);
                }

                bestDealsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (DocumentSnapshot bestDeals : value.getDocuments()) {
                            String fishID = bestDeals.getId();
                            String fishImage = bestDeals.getString("fishImage");
                            String fishName = bestDeals.getString("fishName");
                            String fishPrice = bestDeals.getString("fishNewPrice");
                            String fishAvailability = bestDeals.getString("fishAvailability");

                            ModelShortFishDetails fishDetail = new ModelShortFishDetails(fishID, fishImage, fishName, fishPrice, fishAvailability);
                            searchList.add(fishDetail);
                        }
                        Collections.sort(searchList, new Comparator<ModelShortFishDetails>() {
                            @Override
                            public int compare(ModelShortFishDetails f1, ModelShortFishDetails f2) {
                                return f1.getFishName().compareTo(f2.getFishName());
                            }
                        });
                        mAdapter = new AdapterShortFishDetails(getApplicationContext(), searchList, SearchEngineActivity.this);
                        searchRecycler.setAdapter(mAdapter);
                        searchRecycler.setVerticalFadingEdgeEnabled(false);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initValues() {
        searchRecycler = (RecyclerView) findViewById(R.id.search_recycler);
        searchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        db = FirebaseFirestore.getInstance();
        fishPostRef = db.collection("Fish Posts");
        bestDealsRef = db.collection("Best Deals");

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        gridLayoutManager.setSmoothScrollbarEnabled(true);
        searchRecycler.setLayoutManager(gridLayoutManager);


    }

    @Override
    public void onFishClick(int position) {
        searchView.setSearchFocused(false);
        String fishID = searchList.get(position).getFishID();
        Intent intent = new Intent(SearchEngineActivity.this, FullFishDetailsActivity.class);
        intent.putExtra("FISH ID", fishID);
        intent.putExtra("DEAL TYPE", 3);
        startActivity(intent);
    }
}