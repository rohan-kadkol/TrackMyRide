package com.rohan.trackmyrideversion0;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RideCodeActivity extends AppCompatActivity {
    @BindView(R.id.et_ride_code)
    EditText etRideCode;
    @BindView(R.id.tv_label_ride_code)
    TextView tvLabelRideCode;
    @BindView(R.id.pb_loading)
    ProgressBar pbLoading;
    @BindView(R.id.btn_create_subscribe_ride)
    Button btnCreateSubscribeRide;

    private boolean isDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_code);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        isDriver = intent.getBooleanExtra(getString(R.string.is_driver), false);

        if (isDriver) {
            tvLabelRideCode.setText(R.string.prompt_ride_code_driver);
            btnCreateSubscribeRide.setText(R.string.create_ride);
        } else {
            tvLabelRideCode.setText(R.string.prompt_ride_code_rider);
            btnCreateSubscribeRide.setText(R.string.subscribe_to_a_ride);
        }
    }

    @OnClick(R.id.btn_create_subscribe_ride)
    public void onClick() {
        pbLoading.setVisibility(View.VISIBLE);
        String rideCode = etRideCode.getText().toString();
        checkIfRiderCodeIsUnique(rideCode);
    }

    public void processResult(String rideCode, boolean isRiderCodeUnique) {
        pbLoading.setVisibility(View.GONE);
        if (isDriver) {
            if (isRiderCodeUnique) {
                Intent intent = new Intent(this, DriverActivity.class);
                intent.putExtra(getString(R.string.ride_code), rideCode);
                finish();
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.error_ride_code_not_unique), Toast.LENGTH_LONG).show();
            }
        } else {
            if (isRiderCodeUnique) {
                Toast.makeText(this, getString(R.string.error_invalid_ride_code), Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(this, SubscribeRideActivity.class);
                intent.putExtra(getString(R.string.ride_code), rideCode);
                finish();
                startActivity(intent);
            }
        }
    }

    private void checkIfRiderCodeIsUnique(String rideCode) {
        final String path = "rides/" + rideCode;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                processResult(rideCode, !dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ref.addListenerForSingleValueEvent(postListener);
    }
}
