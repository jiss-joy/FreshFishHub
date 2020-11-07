package com.smartechBrainTechnologies.freshFishHub;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetLocationActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    private static final int ACCESS_LOCATION_REQUEST_CODE = 1001;
    private static final double DELIVERY_DISTANCE = 11.0;
    private static final double NO_CHARGE_DELIVERY_DISTANCE = 5.5;
    private final String distance = "";
    private final Map<String, String> address = new HashMap<>();
    float[] results = new float[10];
    float[] correctedResult = new float[10];
    private ExtendedFloatingActionButton confirmBTN;
    private TextView address_tv;
    private ProgressDialog mProgress;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private CollectionReference userRef;
    private CollectionReference sellerAddressRef;
    private String status = "";
    private String address_delivery_status = "";
    private String location_address;
    private String location_latitude;
    private String location_longitude;
    private LatLng searchLatLng;

    private GoogleMap mMap;
    private Geocoder geocoder;
    private Marker marker;
    private MarkerOptions options;
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.get_location_map);
        mapFragment.getMapAsync(this);

//        Intent intent = getIntent();
//        searchLatLng = intent.getParcelableExtra("LatLng");
//        Toast.makeText(this, searchLatLng.toString(), Toast.LENGTH_LONG).show();

        initValues();

        confirmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDistance();
            }
        });

    }

    private void calculateDistance() {
        sellerAddressRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    String sourceLatitude = documentSnapshot.getString("addressLatitude");
                    String sourceLongitude = documentSnapshot.getString("addressLongitude");
                    Location.distanceBetween(Double.parseDouble(sourceLatitude), Double.parseDouble(sourceLongitude),
                            Double.parseDouble(location_latitude), Double.parseDouble(location_longitude), results);

                    //Dividing by 1000 to get the result in Kilo-Meters.
                    //Multiplying by 1.365 to get the corrected result.
                    //Here the result is 30% less accurate. Hence the correction.
                    correctedResult[0] = (float) (results[0] * 0.001365);
                    if (correctedResult[0] > DELIVERY_DISTANCE) {
                        address_delivery_status = "No Delivery";
                    } else if ((correctedResult[0] < DELIVERY_DISTANCE) && (correctedResult[0] > NO_CHARGE_DELIVERY_DISTANCE)) {
                        address_delivery_status = "Delivery Charge";
                    } else if (correctedResult[0] < NO_CHARGE_DELIVERY_DISTANCE) {
                        address_delivery_status = "Free Delivery";
                    }
                    checkForFirstAddress();
                }
            }
        });

    }

    private void updateAddress() {
        userRef.document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DecimalFormat df = new DecimalFormat("#.##");
                String distance = df.format(correctedResult[0]);

                address.put("addressDistance", distance);
                address.put("addressDeliveryStatus", address_delivery_status);
                address.put("addressName", task.getResult().getString("consumerName"));
                address.put("addressContact", task.getResult().getString("consumerPhone"));
                address.put("address", location_address);
                address.put("addressStatus", status);
                address.put("addressLatitude", location_latitude);
                address.put("addressLongitude", location_longitude);
                addressRef.add(address)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                mProgress.dismiss();
                                finish();
                            }
                        });
            }
        });

    }

    private void checkForFirstAddress() {
        addressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot.isEmpty()) {
                    status = "Default";
                    mProgress.setMessage("Please wait...");
                    mProgress.show();
                    updateAddress();
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
        ExtendedFloatingActionButton confirm_btn = dialog.findViewById(R.id.address_confirmation_yes);
        ExtendedFloatingActionButton cancel_btn = dialog.findViewById(R.id.address_confirmation_no);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "Default";
                mProgress.setMessage("Please wait...");
                mProgress.show();
                changeOtherAddressStatus();
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "Not Default";
                mProgress.setMessage("Please wait...");
                mProgress.show();
                updateAddress();
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
                updateAddress();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
            zoomToUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, ACCESS_LOCATION_REQUEST_CODE);
            }
        }
    }

    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> locationTask = client.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                options = new MarkerOptions().position(latLng)
                        .draggable(true);
                marker = mMap.addMarker(options);
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    location_address = addresses.get(0).getAddressLine(0);
                    address_tv.setText(location_address);
                    location_latitude = String.valueOf(latLng.latitude);
                    location_longitude = String.valueOf(latLng.longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        marker.remove();
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            options = new MarkerOptions().position(latLng)
                    .draggable(true);
            location_address = addresses.get(0).getAddressLine(0);
            address_tv.setText(location_address);
            location_latitude = String.valueOf(latLng.latitude);
            location_longitude = String.valueOf(latLng.longitude);

        } catch (IOException e) {
            e.printStackTrace();
        }
        marker = mMap.addMarker(options);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        try {
            LatLng latLng = marker.getPosition();
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            location_address = addresses.get(0).getAddressLine(0);
            address_tv.setText(location_address);
            location_latitude = String.valueOf(latLng.latitude);
            location_longitude = String.valueOf(latLng.longitude);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                zoomToUserLocation();
            } else {

            }
        }
    }

    private void initValues() {
        geocoder = new Geocoder(this);
        address_tv = (TextView) findViewById(R.id.get_location_address);
        client = LocationServices.getFusedLocationProviderClient(this);
        confirmBTN = (ExtendedFloatingActionButton) findViewById(R.id.get_location_confirm);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        addressRef = db.collection("Consumers").document(currentUser.getUid()).collection("My Addresses");
        userRef = db.collection("Consumers");
        sellerAddressRef = db.collection("Sellers").document("Contact Us")
                .collection("Address");
    }
}
