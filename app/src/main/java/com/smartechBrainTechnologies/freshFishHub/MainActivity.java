package com.smartechBrainTechnologies.freshFishHub;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private final static int ID_MARKET = 1;
    private final static int ID_ORDERS = 2;
    private final static int ID_PROFILE = 3;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private MeowBottomNavigation meowBottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initValues();

        meowBottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.market));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.orders));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.username));

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MarketFragment()).commit();

        meowBottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {

            }
        });

        meowBottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                Fragment selectedFragment = null;
                switch (item.getId()) {
                    case ID_MARKET:
                        selectedFragment = new MarketFragment();
                        break;
                    case ID_ORDERS:
                        selectedFragment = new ConsumerOrdersFragment();
                        break;
                    case ID_PROFILE:
                        selectedFragment = new ProfileFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
        });

        meowBottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {
            }
        });

//        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MarketFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_market:
                            selectedFragment = new MarketFragment();
                            break;
                        case R.id.nav_order:
                            selectedFragment = new ConsumerOrdersFragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                    return true;
                }
            };


    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, AuthenticationBridgeActivity.class);
            startActivity(intent);
        } else {

        }
    }

    private void initValues() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        meowBottomNavigation = (MeowBottomNavigation) findViewById(R.id.bottom_navigation);
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }
}