package com.nikhil.bletrial.ui.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.nikhil.bletrial.R;
import com.nikhil.bletrial.app.AppConstants;
import com.nikhil.bletrial.bluetooth.scan.DeviceListViewAdapter;
import com.nikhil.bletrial.bluetooth.scan.DeviceScanPresenter;
import com.nikhil.bletrial.bluetooth.scan.IDeviceScanView;
import com.nikhil.bletrial.bluetooth.utils.BLEInteractor;
import com.nikhil.bletrial.bluetooth.utils.BluetoothAdapterSingleton;
import com.nikhil.bletrial.bluetooth.utils.manufacturer_id.ManufacturerId;
import com.nikhil.bletrial.utils.CheckConnection;

import java.io.File;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_MAC;
import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_MANUFACTURER_ID;
import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_NAME;
import static com.nikhil.bletrial.app.AppConstants.PermissionRequests.PERMISSION_REQUEST_COARSE_LOCATION;
import static com.nikhil.bletrial.app.AppConstants.PermissionRequests.PERMISSION_REQUEST_WRITE_STORAGE;
import static com.nikhil.bletrial.app.AppConstants.SharedPreferences.SPF_DEVICE;
import static com.nikhil.bletrial.app.AppConstants.Storage.ACTOFIT_FOLDER;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.CONNECTION_ATTEMPT_FAILED;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.ENABLE_BT_REQUEST;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.SCAN_FAILED;

/**
 * Created by nikhil on 22/03/17.
 */

public class DeviceScanActivity extends AppCompatActivity implements IDeviceScanView,
        AdapterView.OnItemClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Views from layout
     */
    @BindView(R.id.scanDevice_Button)
    Button scanDevice_Button;
    @BindView(R.id.scanning_text)
    TextView scanning_text;
    @BindView(R.id.deviceList_ListView)
    ListView deviceList_ListView;
    /**
     * Objects
     */
    BluetoothAdapter bluetoothAdapter;
    DeviceScanPresenter deviceScanPresenter;
    String deviceMac;
    GoogleApiClient googleApiClient;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        ButterKnife.bind(this);

        deviceScanPresenter = new DeviceScanPresenter(this, this);
        bluetoothAdapter = BluetoothAdapterSingleton.getAdapter();
        deviceList_ListView.setOnItemClickListener(this);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (new CheckConnection().isInternetAvailable(this)) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                googleApiClient.connect();
            } else {
                //Log.d(TAG, "onResume: No Net");
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
                //finish();
            }
        }

        if (checkLocationPermission()) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                deviceMac = getSharedPreferences(SPF_DEVICE, MODE_PRIVATE).getString(KEY_DEVICE_MAC, "");
                if (deviceMac.equals("")) {
                    scanForBleDevice();
                } else {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceMac);
                    if (device != null) {
                        deviceScanPresenter.connectDevice(device);
                        startActivity(new Intent(this, ActofitDeviceActivity.class));
                        finish();
                    } else {
                        scanForBleDevice();
                    }
                }
            } else {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, ENABLE_BT_REQUEST);
            }
        }

        if (checkStoragePermission()) {
            File file = new File(Environment.getExternalStorageDirectory(), ACTOFIT_FOLDER);
            if (!file.exists()) {
                Log.d(AppConstants.TAG, "onResume: " + file.mkdir());
            }
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_STORAGE);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ENABLE_BT_REQUEST:
                if (resultCode == RESULT_OK) {
                    bluetoothAdapter = BluetoothAdapterSingleton.getAdapter();
                } else {
                    Toast.makeText(this, "Bluetooth Required to Scan", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "Requires Location for BLE", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case PERMISSION_REQUEST_WRITE_STORAGE:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "Requires storage permissions to save data.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public void onListLoadedSuccessfully(HashMap<String, ScanResult> hashMap) {
        progressDialog.dismiss();
        scanDevice_Button.setClickable(true);
        scanning_text.setText(R.string.select_device_text);
        deviceList_ListView.setAdapter(new DeviceListViewAdapter(this, hashMap));
        deviceList_ListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onListLoadedFailed(int FAILURE_CODE) {
        scanDevice_Button.setClickable(true);
        switch (FAILURE_CODE) {
            case SCAN_FAILED:
                progressDialog.dismiss();
                scanning_text.setText(R.string.no_device_found);
                deviceList_ListView.setVisibility(View.GONE);
                break;

            case CONNECTION_ATTEMPT_FAILED:
                scanning_text.setText(R.string.connection_attempt_failed);
                break;
        }
    }

    @OnClick(R.id.scanDevice_Button)
    public void scanForBleDevice() {
        scanDevice_Button.setClickable(false);
        scanning_text.setText(R.string.scanning_text);
        deviceScanPresenter.initiateScanProcess(bluetoothAdapter);
        //deviceList_ListView.setVisibility(View.GONE);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Scanning For Devices...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanResult scanResult = (ScanResult) parent.getAdapter().getItem(position);
        BluetoothDevice device = scanResult.getDevice();
        String manufacturerID = new ManufacturerId().getManufacturerId(scanResult);
        Log.d(AppConstants.TAG, "onItemClick: " + manufacturerID);

        if (manufacturerID.trim().startsWith("0476")) {
            BLEInteractor.DEVICE_MAC = device.getAddress();
            deviceScanPresenter.connectDevice(scanResult.getDevice());

            SharedPreferences spf_device = getSharedPreferences(SPF_DEVICE, MODE_PRIVATE);
            SharedPreferences.Editor editor = spf_device.edit();
            editor.putString(KEY_DEVICE_NAME, device.getName());
            editor.putString(KEY_DEVICE_MAC, device.getAddress());
            editor.putString(KEY_DEVICE_MANUFACTURER_ID, manufacturerID);
            editor.apply();

            startActivity(new Intent(this, ActofitDeviceActivity.class)
                    .putExtra(KEY_DEVICE_NAME, device.getName())
                    .putExtra(KEY_DEVICE_MAC, device.getAddress())
                    .putExtra(KEY_DEVICE_MANUFACTURER_ID, manufacturerID)
            );

            finish();
        } else {
            Toast.makeText(this, "Select Actofit Device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /*mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);*/

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .setAlwaysShow(true)
                .setNeedBle(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        checkLocationPermission();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    DeviceScanActivity.this,
                                    PERMISSION_REQUEST_COARSE_LOCATION);

                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        // Can cause when on Flight Mode.
                        Toast.makeText(DeviceScanActivity.this, "Phone On Flight Mode", Toast.LENGTH_LONG).show();
                        //finish();
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Log.d(TAG, "onConnectionFailed: " + connectionResult.toString());
    }
}
