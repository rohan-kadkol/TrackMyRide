package com.rohan.trackmyrideversion0;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.api.model.Place;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubscribeRideActivity extends AppCompatActivity {
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.btn_origin_pick)
    Button btnOriginPick;
    @BindView(R.id.btn_destination_pick)
    Button btnDestinationPick;
    @BindView(R.id.tv_origin_address)
    TextView tvOriginAddress;
    @BindView(R.id.tv_destination_address)
    TextView tvDestinationAddress;

    private static final int ORIGIN_PLACE_PICKER = 0;
    private static final int DESTINATION_PLACE_PICKER = 1;

    String rideCode;
    String name;
    Place origin;
    Place destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe_ride);

        ButterKnife.bind(this);

        rideCode = getIntent().getStringExtra(getString(R.string.ride_code));
    }

    @OnClick(R.id.btn_origin_pick)
    public void onOriginClick() {
        showPlacePicker(ORIGIN_PLACE_PICKER);
    }

    @OnClick(R.id.btn_destination_pick)
    public void onDestinationClick() {
        showPlacePicker(DESTINATION_PLACE_PICKER);
    }

    @OnClick(R.id.btn_subscribe_ride)
    public void onSubscribeClick() {
        name = etName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.error_enter_name), Toast.LENGTH_SHORT).show();
        } else if (origin == null) {
            Toast.makeText(this, getString(R.string.error_origin), Toast.LENGTH_SHORT).show();
        } else if (destination == null) {
            Toast.makeText(this, getString(R.string.error_destination), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, RiderActivity.class);
            intent.putExtra(getString(R.string.ride_code), rideCode);
            intent.putExtra(getString(R.string.name), name);
            intent.putExtra(getString(R.string.origin), origin);
            intent.putExtra(getString(R.string.destination), destination);
            finish();
            startActivity(intent);
        }
    }

    private void showPlacePicker(int requestCode) {
        PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
        builder.setAndroidApiKey(BuildConfig.ANDROID_KEY)
                .setMapsApiKey(BuildConfig.GOOGLE_API_KEY);

        // If you want to set a initial location rather then the current device location.
        // NOTE: enable_nearby_search MUST be true.
        // builder.setLatLng(new LatLng(37.4219999, -122.0862462))

        try {
            Intent placeIntent = builder.build(SubscribeRideActivity.this);
            startActivityForResult(placeIntent, requestCode);
        } catch (Exception ex) {
            // Google Play services is not available...
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK && (requestCode == ORIGIN_PLACE_PICKER || requestCode == DESTINATION_PLACE_PICKER)) return;

        Place place = PingPlacePicker.getPlace(data);
        if (place == null) return;
        if (requestCode == ORIGIN_PLACE_PICKER) {
            origin = place;
            tvOriginAddress.setText(place.getAddress());
            tvOriginAddress.setVisibility(View.VISIBLE);
        } else {
            destination = place;
            tvDestinationAddress.setText(place.getAddress());
            tvDestinationAddress.setVisibility(View.VISIBLE);
        }
    }
}
