package com.smartechBrainTechnologies.freshFishHub;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AddNewAddressActivity extends AppCompatActivity {

    private EditText addressName, addressBuilding, addressCity, addressLandmark, addressArea,
            addressPin, addressState, addressCountry;
    private ExtendedFloatingActionButton submitBTN;
    private ProgressDialog mProgress;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private Map<String, String> address = new HashMap<>();
    private String status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_address);

        initValues();

        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAddress();
            }
        });
    }

    private void verifyAddress() {
        if (addressName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter receiver name", Toast.LENGTH_SHORT).show();
            addressName.requestFocus();
        } else if (addressBuilding.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter relevant details", Toast.LENGTH_SHORT).show();
            addressBuilding.requestFocus();
        } else if (addressCity.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter relevant details", Toast.LENGTH_SHORT).show();
            addressCity.requestFocus();
        } else if (addressArea.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter relevant details", Toast.LENGTH_SHORT).show();
            addressArea.requestFocus();
        } else if (addressPin.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter Area Pin Code", Toast.LENGTH_SHORT).show();
            addressPin.requestFocus();
        } else {
            checkForFirstAddress();
        }
    }

    private void checkForFirstAddress() {

        addressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot.isEmpty()) {
                    status = "Default";
                    mProgress.setMessage("Adding address...");
                    mProgress.show();
                    addAddress();
                } else {
                    confirmDefault();
                }
            }
        });


    }

    private void confirmDefault() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_address_confirmation);
        Window window = dialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ExtendedFloatingActionButton confirm_btn = (ExtendedFloatingActionButton) dialog.findViewById(R.id.address_confirmation_yes);
        ExtendedFloatingActionButton cancel_btn = (ExtendedFloatingActionButton) dialog.findViewById(R.id.address_confirmation_no);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "Default";
                mProgress.setMessage("Adding address...");
                mProgress.show();
                changeOtherAddressStatus();
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "Not Default";
                mProgress.setMessage("Adding address...");
                mProgress.show();
                addAddress();
            }
        });
        dialog.show();
    }

    private void changeOtherAddressStatus() {
        addressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    addressRef.document(documentSnapshot.getId()).update("addressStatus", "Not Default");
                }
                addAddress();
            }
        });
    }


    private void addAddress() {
        address.put("addressArea", addressArea.getText().toString());
        address.put("addressBuilding", addressBuilding.getText().toString());
        address.put("addressCity", addressCity.getText().toString());
        address.put("addressCountry", "India");
        if (addressLandmark.getText().toString().isEmpty()) {
            address.put("addressLandmark", "");
        } else {
            address.put("addressLandmark", addressLandmark.getText().toString());
        }
        address.put("addressName", addressName.getText().toString());
        address.put("addressPin", addressPin.getText().toString());
        address.put("addressState", "Kerala");
        address.put("addressStatus", status);
        addressRef.add(address)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        mProgress.dismiss();
                        Toast.makeText(AddNewAddressActivity.this, "Address added successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void initValues() {
        addressName = (EditText) findViewById(R.id.add_new_address_name);
        addressBuilding = (EditText) findViewById(R.id.add_new_address_building);
        addressCity = (EditText) findViewById(R.id.add_new_address_city);
        addressLandmark = (EditText) findViewById(R.id.add_new_address_landmark);
        addressArea = (EditText) findViewById(R.id.add_new_address_area);
        addressPin = (EditText) findViewById(R.id.add_new_address_pin);
        addressState = (EditText) findViewById(R.id.add_new_address_state);
        addressState.setText("Kerala");
        addressState.setEnabled(false);
        addressCountry = (EditText) findViewById(R.id.add_new_address_country);
        addressCountry.setText("India");
        addressCountry.setEnabled(false);
        submitBTN = (ExtendedFloatingActionButton) findViewById(R.id.add_new_address_submit_btn);
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
    }
}