package com.smartechBrainTechnologies.freshFishHub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderPlacedActivity extends AppCompatActivity {

    private TextView toolbarTitle;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_placed);

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Order Placed");

        linearLayout = findViewById(R.id.order_placed_goToMarket);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(OrderPlacedActivity.this, MainActivity.class));
            }
        });
    }
}