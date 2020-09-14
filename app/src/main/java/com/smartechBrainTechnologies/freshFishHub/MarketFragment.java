package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MarketFragment extends Fragment implements AdapterShortFishDetails.OnFishSelectedListener {

    private RecyclerView marketRecycler;
    private Toolbar toolbar;
    private ProgressDialog mProgress;
    private ImageView noFishImage;
    private TextView noFishTV;


    private FirebaseFirestore db;
    private CollectionReference fishPostRef;
    private ArrayList<ModelShortFishDetails> fishDetailsList = new ArrayList<>();
    private AdapterShortFishDetails mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        initValues(view);

        setUpRecycler();

        setUpToolbar();

        return view;
    }

    private void setUpToolbar() {
        toolbar.inflateMenu(R.menu.menu_toolbar_market);
        toolbar.setTitle("Market");
        Menu menu = toolbar.getMenu();
        MenuItem menuItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setIconifiedByDefault(false);
        searchView.setIconified(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
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
                    String fishLocation = fishDetails.getString("fishLocation");

                    ModelShortFishDetails fishDetail = new ModelShortFishDetails(fishID, fishImage, fishName, fishPrice, fishAvailability, fishLocation);
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
                    mAdapter.notifyDataSetChanged();
                }

            }

        });
        mProgress.dismiss();
    }

    private void initValues(View view) {
        marketRecycler = (RecyclerView) view.findViewById(R.id.market_recycler);
        toolbar = (Toolbar) view.findViewById(R.id.market_toolbar);
        mProgress = new ProgressDialog(getContext());
        mProgress.setCancelable(false);
        noFishImage = (ImageView) view.findViewById(R.id.market_no_fish_image);
        noFishTV = (TextView) view.findViewById(R.id.market_no_fish_tv);


        db = FirebaseFirestore.getInstance();
        fishPostRef = db.collection("Fish Posts");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        marketRecycler.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onFishClick(int position) {
        String fishID = fishDetailsList.get(position).getFishID();
        Intent intent = new Intent(getContext(), FullFishDetailsActivity.class);
        intent.putExtra("FISH ID", fishID);
        startActivity(intent);
    }
}