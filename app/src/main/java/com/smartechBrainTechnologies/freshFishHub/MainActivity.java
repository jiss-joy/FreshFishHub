package com.smartechBrainTechnologies.freshFishHub;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "freshfishhubNotifications";
    public static final String CHANNEL_NAME = "FreshFishHub";
    public static final String CHANNEL_DESC = "Welcome Notification";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Fragment selectedFragment;
    private String userID;
    private int fragmentFlag = 1;
    private boolean exitFlag = false;

    private BottomNavigationView bottomNavigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.nav_market:
                            selectedFragment = new MarketFragment();
                            fragmentFlag = 1;
                            break;
                        case R.id.nav_order:
                            selectedFragment = new ConsumerOrdersFragment();
                            fragmentFlag = 2;
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            fragmentFlag = 3;
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initValues();


        bottomNavigationView.setSelectedItemId(R.id.nav_market);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MarketFragment()).commit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void initValues() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, AuthenticationBridgeActivity.class);
            startActivity(intent);
        } else {
            userID = currentUser.getUid();

        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentFlag == 1) {
            confirmExit();
        } else {
            Fragment selectedFragment = new MarketFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_market);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
    }

    private void confirmExit() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.popup_confirm_exit);
        Window window = dialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ExtendedFloatingActionButton confirm_btn = dialog.findViewById(R.id.confirm_exit_confirm);
        ExtendedFloatingActionButton cancel_btn = dialog.findViewById(R.id.confirm_exit_cancel);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void exit() {
        this.finishAffinity();
    }


}