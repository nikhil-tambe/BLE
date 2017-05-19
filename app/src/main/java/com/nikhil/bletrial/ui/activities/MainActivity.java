package com.nikhil.bletrial.ui.activities;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nikhil.bletrial.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nikhil.bletrial.app.AppConstants.Storage.ACTOFIT_FOLDER;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.ble_button)
    Button ble_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            startActivity(new Intent(this, DeviceScanActivity.class));
            finish();
        }
    }

    @OnClick(R.id.ble_button)
    public void checkBLESupport(View view) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            startActivity(new Intent(this, DeviceScanActivity.class));
        } else {
            Toast.makeText(MainActivity.this, "BLE not supported", Toast.LENGTH_SHORT).show();
            ble_button.setVisibility(View.GONE);
        }
    }
}
