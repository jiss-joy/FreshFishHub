package com.smartechBrainTechnologies.freshFishHub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditAddressActivity extends AppCompatActivity {

    private EditText name, building, area, landmark, city, pin, state, country;
    private ExtendedFloatingActionButton updateBTN;
    private ProgressDialog mProgress;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private int addressPosition;
    private Map<String, Object> address = new HashMap<>();
    private ArrayList<String> addressOptionList = new ArrayList<>();
    private boolean updateFlag = false;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        Intent intent = getIntent();
        addressPosition = intent.getIntExtra("Address ID", 0);
        addressOptionList = intent.getStringArrayListExtra("Address List");
        System.out.println(addressPosition);

        initValues();

        loadData();

        lockFields();

        updateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateFlag) {
                    verifyFields();
                } else {
                    updateBTN.extend();
                    unlockFields();
                    updateFlag = true;
                }


            }
        });
    }

    private void verifyFields() {
        if (name.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter receiver name", Toast.LENGTH_SHORT).show();
            name.requestFocus();
        } else if (building.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter relevant details", Toast.LENGTH_SHORT).show();
            building.requestFocus();
        } else if (city.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter relevant details", Toast.LENGTH_SHORT).show();
            city.requestFocus();
        } else if (area.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter relevant details", Toast.LENGTH_SHORT).show();
            area.requestFocus();
        } else if (pin.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter Area Pin Code", Toast.LENGTH_SHORT).show();
            pin.requestFocus();
        } else {
            mProgress.setMessage("Updating address...");
            mProgress.show();
            updateAddress();
        }
    }

    private void updateAddress() {
        address.put("addressName", name.getText().toString());
        address.put("addressBuilding", building.getText().toString());
        address.put("addressArea", area.getText().toString());
        if (landmark.getText().toString().isEmpty()) {
            address.put("addressLandmark", "");
        } else {
            address.put("addressLandmark", landmark.getText().toString());
        }
        address.put("addressCity", city.getText().toString());
        address.put("addressPin", pin.getText().toString());
        addressRef.document(addressOptionList.get(addressPosition))
                .update(address).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();
                Toast.makeText(EditAddressActivity.this, "Updated address successfully.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditAddressActivity.this, MyAddressesActivity.class));
            }
        });
    }

    private void unlockFields() {
        name.setEnabled(true);
        building.setEnabled(true);
        area.setEnabled(true);
        landmark.setEnabled(true);
        city.setEnabled(true);
        pin.setEnabled(true);
    }

    private void lockFields() {
        name.setEnabled(false);
        building.setEnabled(false);
        area.setEnabled(false);
        landmark.setEnabled(false);
        city.setEnabled(false);
        pin.setEnabled(false);
    }

    private void loadData() {
        mProgress.setMessage("Loading address...");
        mProgress.show();
        addressRef.document(addressOptionList.get(addressPosition))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                name.setText(documentSnapshot.getString("addressName"));
                building.setText(documentSnapshot.getString("addressBuilding"));
                area.setText(documentSnapshot.getString("addressArea"));
                landmark.setText(documentSnapshot.getString("addressLandmark"));
                city.setText(documentSnapshot.getString("addressCity"));
                pin.setText(documentSnapshot.getString("addressPin"));
                mProgress.dismiss();
            }
        });

    }

    private void initValues() {
        name = findViewById(R.id.edit_address_name);
        building = findViewById(R.id.edit_address_building);
        area = findViewById(R.id.edit_address_area);
        landmark = findViewById(R.id.edit_address_landmark);
        city = findViewById(R.id.edit_address_city);
        pin = findViewById(R.id.edit_address_pin);
        state = findViewById(R.id.edit_address_state);
        country = findViewById(R.id.edit_address_country);
        updateBTN = findViewById(R.id.edit_address_edit_update_btn);
        updateBTN.shrink();
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        addressRef = db.collection("Consumers").document(currentUser.getUid()).collection("My Addresses");
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}