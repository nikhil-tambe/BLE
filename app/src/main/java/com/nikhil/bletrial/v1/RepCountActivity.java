package com.nikhil.bletrial.v1;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nikhil.bletrial.R;
import com.nikhil.bletrial.ui.activities.DeviceScanActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_MAC;
import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_MANUFACTURER_ID;
import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_NAME;
import static com.nikhil.bletrial.app.AppConstants.SharedPreferences.SPF_DEVICE;
import static com.nikhil.bletrial.v1.ConstantsV1.*;

/**
 * Created by Nikhil on 9/2/18.
 */

public class RepCountActivity extends AppCompatActivity {

    private static final String TAG = "NNN_RepCountActivity";
    //
    @BindView(R.id.rawData_TextView)
    TextView rawData_TextView;
    @BindView(R.id.errorMessage_TextView)
    TextView errorMessage_TextView;
    @BindView(R.id.startGym_Button)
    Button startGym_Button;
    @BindView(R.id.stopGym_Button)
    Button stopGym_Button;
    //
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_CONNECTION_STATE:
                        toggleConnectionState(intent.getIntExtra(KEY_CONNECTION_STATE,
                                BluetoothProfile.STATE_DISCONNECTED));
                        break;

                    case ACTION_DATA_RECEIVED:
                        onDataReceived(intent.getStringExtra(KEY_PROCESSED_DATA));
                        break;

                    case ACTION_REP_DETECTED:
                        Log.d(TAG, "onReceive: Rep Detected");
                        break;

                    case ACTION_ERROR_OCCURRED:
                        String errorMessage = intent.getStringExtra(KEY_ERROR_MESSAGE);
                        boolean shouldShowMessage = intent.getBooleanExtra(KEY_SHOULD_SHOW_MESSAGE, false);
                        if (shouldShowMessage) {
                            setErrorMessage(errorMessage);
                        }
                        break;

                    default:
                        Log.e(TAG, "onReceive: " + intent.getAction());
                }
            }
        }
    };
    String deviceName, deviceMac, deviceManufacturerId;
    SharedPreferences spf_device;
    MenuItem menuItem_Disconnect;
    WatchConnectivityService service;
    boolean isServiceBound = false, isDeviceConnected = false;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: ");
            WatchConnectivityService.LocalBinder binder = (WatchConnectivityService.LocalBinder) iBinder;
            service = binder.getService();
            isServiceBound = true;
            service.init(deviceMac);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep_count);
        ButterKnife.bind(this);

        spf_device = getSharedPreferences(SPF_DEVICE, MODE_PRIVATE);
        deviceName = spf_device.getString(KEY_DEVICE_NAME, "");
        deviceMac = spf_device.getString(KEY_DEVICE_MAC, "");
        deviceManufacturerId = spf_device.getString(KEY_DEVICE_MANUFACTURER_ID, "");

        if (deviceMac.equals("")) {
            showToast("Null MacAddress");
            finish();
        }
        registerServiceAndReceiver();
    }

    private void registerServiceAndReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONNECTION_STATE);
        intentFilter.addAction(ACTION_DATA_RECEIVED);
        intentFilter.addAction(ACTION_REP_DETECTED);
        intentFilter.addAction(ACTION_ERROR_OCCURRED);
        registerReceiver(receiver, intentFilter);

        Intent intent = new Intent(this, WatchConnectivityService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        try {
            BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            manager.getAdapter().disable();
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_device, menu);
        menuItem_Disconnect = menu.findItem(R.id.disconnectDevice);
        menuItem_Disconnect.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menuItem_Disconnect selection
        switch (item.getItemId()) {
            case R.id.disconnectDevice:
                spf_device.edit().clear().apply();
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.startGym_Button)
    public void sendStartCommand() {
        if (isServiceBound) {
            if (isDeviceConnected) {
                String s = "Gym Started";
                rawData_TextView.setText(s);
                setErrorMessage(s);
                service.sendStartSessionCommand();
            } else {
                setErrorMessage("Device Disconnected");
            }
        }
    }

    @OnClick(R.id.stopGym_Button)
    public void sendStopCommand() {
        if (isServiceBound) {
            if (isDeviceConnected) {
                String s = "Gym Stopped";
                rawData_TextView.setText(s);
                setErrorMessage(s);
                service.sendStopSessionCommand();
            } else {
                setErrorMessage("Device Disconnected");
            }
        }
    }

    private void toggleConnectionState(int state) {
        String message;
        isDeviceConnected = false;
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                message = "CONNECTED";
                menuItem_Disconnect.setTitle("Disconnect");
                menuItem_Disconnect.setVisible(true);
                isDeviceConnected = true;
                break;

            case BluetoothProfile.STATE_DISCONNECTED:
                message = "DISCONNECTED";
                menuItem_Disconnect.setTitle("Re-Connect");
                menuItem_Disconnect.setVisible(true);
                break;

            default:
                message = "" + state;
                menuItem_Disconnect.setVisible(false);
        }
        rawData_TextView.setText(message);
        showToast(message);
    }

    private void onDataReceived(String formatted) {
        //Log.d(TAG, "onDataReceived: " + formatted);
        rawData_TextView.setText(formatted);
    }

    private void setErrorMessage(String errorMessage) {
        errorMessage_TextView.setText(errorMessage);
        showToast(errorMessage);
        Log.e(TAG, "onReceive: ERROR: " + errorMessage);
    }

    private void showToast(String message) {
        Log.d(TAG, "showToast: " + message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
