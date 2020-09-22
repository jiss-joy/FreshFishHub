package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyAddressesActivity extends AppCompatActivity implements AdapterAddress.OnOptionsClickListener {

    private RecyclerView addressRecycler;
    private ExtendedFloatingActionButton addAddressBTN;
    private ProgressDialog mProgress;
    private ImageView noAddressImage;
    private TextView noAddressTV, toolbarTitle;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private ArrayList<ModelAddress> addressList = new ArrayList<>();
    private ArrayList<String> addressOptionList = new ArrayList<>();
    private AdapterAddress mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_addresses);

        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("My Addresses");

        initValues();

        setUpRecycler();

        addAddressBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyAddressesActivity.this, AddNewAddressActivity.class));
            }
        });
    }

    private void setUpRecycler() {
        mProgress.setMessage("Please wait...");
        mProgress.show();
        addressRef.orderBy("addressStatus", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                addressList.clear();
                addressOptionList.clear();
                for (DocumentSnapshot addresses : value.getDocuments()) {
                    String addressName = addresses.getString("addressName");
                    String addressBuilding = addresses.getString("addressBuilding");
                    String addressCity = addresses.getString("addressCity");
                    String addressLandmark = addresses.getString("addressLandmark");
                    String addressArea = addresses.getString("addressArea");
                    String addressPin = addresses.getString("addressPin");
                    String addressState = addresses.getString("addressState");
                    String addressStatus = addresses.getString("addressStatus");

                    ModelAddress address = new ModelAddress(addressArea, addressBuilding, addressCity, addressLandmark, addressName, addressPin, addressState, addressStatus);
                    addressList.add(address);
                    addressOptionList.add(addresses.getId());
                }
                if (addressList.isEmpty()) {
                    addressRecycler.setVisibility(View.GONE);
                    addAddressBTN.extend();
                    noAddressTV.setVisibility(View.VISIBLE);
                    noAddressImage.setVisibility(View.VISIBLE);
                } else {
                    noAddressTV.setVisibility(View.GONE);
                    noAddressImage.setVisibility(View.GONE);
                    addressRecycler.setVisibility(View.VISIBLE);
                    mAdapter = new AdapterAddress(MyAddressesActivity.this, addressList, MyAddressesActivity.this);
                    addressRecycler.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        mProgress.dismiss();
    }

    private void initValues() {
        addressRecycler = (RecyclerView) findViewById(R.id.my_addresses_recycler);
        addAddressBTN = (ExtendedFloatingActionButton) findViewById(R.id.my_addresses_add_btn);
        addAddressBTN.shrink();
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        noAddressTV = (TextView) findViewById(R.id.my_addresses_no_address_tv);
        noAddressImage = (ImageView) findViewById(R.id.my_addresses_no_address_image);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        addressRef = db.collection("Consumers").document(currentUser.getUid()).collection("My Addresses");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        addressRecycler.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onOptionsClick(final int position, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_address_options);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_address_make_default:
                        makeDefault(position);
                        return true;
                    case R.id.menu_address_delete:
                        deleteAddress(position);
                        return true;
                    case R.id.menu_address_edit:
                        Intent intent = new Intent(MyAddressesActivity.this, EditAddressActivity.class);
                        intent.putExtra("Address List", addressOptionList);
                        intent.putExtra("Address ID", position);
                        startActivity(intent);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void makeDefault(final int position) {
        mProgress.setMessage("Setting address as default...");
        mProgress.show();
        addressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot documentSnapshots : task.getResult().getDocuments()) {
                    addressRef.document(documentSnapshots.getId()).update("addressStatus", "Not Default");
                }
                addressRef.document(addressOptionList.get(position)).update("addressStatus", "Default").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgress.dismiss();
                    }
                });
            }
        });
    }

    private void deleteAddress(final int position) {
        mProgress.setMessage("Deleting Address...");
        mProgress.show();
        addressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                addressRef.document(addressOptionList.get(position)).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgress.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}