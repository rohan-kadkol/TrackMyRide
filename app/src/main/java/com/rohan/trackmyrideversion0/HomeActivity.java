package com.rohan.trackmyrideversion0;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {
    @BindView(R.id.btn_create_ride)
    Button btnCreateRide;
    @BindView(R.id.btn_subscribe_ride)
    Button btnSubscribeRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);
    }


    @OnClick(R.id.btn_create_ride)
    void createRide() {
        Intent intent = new Intent(this, RideCodeActivity.class);
        intent.putExtra(getString(R.string.is_driver), true);
        startActivity(intent);
    }

    @OnClick(R.id.btn_subscribe_ride)
    void subscribeToRide() {
        Intent intent = new Intent(this, RideCodeActivity.class);
        intent.putExtra(getString(R.string.is_driver), false);
        startActivity(intent);
    }
}
