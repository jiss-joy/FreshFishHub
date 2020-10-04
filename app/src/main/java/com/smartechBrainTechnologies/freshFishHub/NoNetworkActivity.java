package com.smartechBrainTechnologies.freshFishHub;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class NoNetworkActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private ExtendedFloatingActionButton retryBTN;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);

        retryBTN = findViewById(R.id.no_network_retry_btn);
        relativeLayout = findViewById(R.id.no_network_layout);

        retryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternetConnection();
            }
        });

    }

    private void checkInternetConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();

        if (isConnected) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            showSnackBar();
        }


    }

    private void showSnackBar() {
        Snackbar snackbar = Snackbar.make(relativeLayout, "No network was detected!", Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.color_red));
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        MyApp.getInstance().setConnectivityListener(this);

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onBackPressed() {

    }
}