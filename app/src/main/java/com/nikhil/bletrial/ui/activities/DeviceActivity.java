package com.nikhil.bletrial.ui.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.nikhil.bletrial.R;
import com.nikhil.bletrial.bluetooth.device.DevicePresenter;
import com.nikhil.bletrial.bluetooth.device.IDeviceView;
import com.nikhil.bletrial.bluetooth.utils.BluetoothAdapterSingleton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_MAC;
import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_MANUFACTURER_ID;
import static com.nikhil.bletrial.app.AppConstants.IntentKeys.KEY_DEVICE_NAME;
import static com.nikhil.bletrial.app.AppConstants.PermissionRequests.PERMISSION_REQUEST_CALENDER_ACCESS;
import static com.nikhil.bletrial.app.AppConstants.PermissionRequests.PERMISSION_REQUEST_CALL_ACCESS;
import static com.nikhil.bletrial.app.AppConstants.PermissionRequests.PERMISSION_REQUEST_CONTACTS_ACCESS;
import static com.nikhil.bletrial.app.AppConstants.PermissionRequests.PERMISSION_REQUEST_SMS_ACCESS;
import static com.nikhil.bletrial.app.AppConstants.SharedPreferences.SPF_DEVICE;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_GYM_OFFLINE_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_GYM_ONLINE_START;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_LIFE_LOG_OFFLINE_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_LIFE_LOG_ONLINE_START;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_SESSION_COUNT;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_SLEEP_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.ENABLE_BT_REQUEST;

/**
 * Created by nikhil on 05/04/17.
 */

public class DeviceActivity extends AppCompatActivity implements IDeviceView {

    /**
     * Views from layout
     */
    @BindView(R.id.deviceName_TextView)
    TextView deviceName_TextView;
    @BindView(R.id.deviceMac_TextView)
    TextView deviceMac_TextView;
    @BindView(R.id.deviceManufacturer_TextView)
    TextView deviceManufacturer_TextView;
    @BindView(R.id.onlineGymStart_Button)
    Button onlineGymStart_Button;
    @BindView(R.id.writeBytes_EditText)
    EditText writeBytes_EditText;
    @BindView(R.id.logs_TextView)
    TextView logs_TextView;
    @BindView(R.id.channelSpinner)
    Spinner channelSpinner;
    @BindView(R.id.connectivityIndication_ImageView)
    ImageView connectivityIndication_ImageView;
    @BindView(R.id.unpair_ImageView)
    ImageView unpair_ImageView;
    @BindView(R.id.accelerometer_LineChart)
    LineChart accelerometer_LineChart;
    @BindView(R.id.gyroscope_LineChart)
    LineChart gyroscope_LineChart;
    //@BindView(R.id.service_list)
    //RecyclerView service_list;

    /**
     * Objects
     */
    BluetoothAdapter bluetoothAdapter;
    DevicePresenter presenter;
    String deviceName, deviceMac, deviceManufacturerId;
    BluetoothDevice device;
    private boolean isDeviceConnected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_device);
        ButterKnife.bind(this);

        /*service_list = (RecyclerView) findViewById(R.id.service_list);
        service_list.setLayoutManager(new LinearLayoutManager(this));*/

        presenter = new DevicePresenter(this, this);
        bluetoothAdapter = BluetoothAdapterSingleton.getAdapter();

        SharedPreferences spf_device = getSharedPreferences(SPF_DEVICE, MODE_PRIVATE);

        deviceName = spf_device.getString(KEY_DEVICE_NAME, "");
        deviceMac = spf_device.getString(KEY_DEVICE_MAC, "");
        deviceManufacturerId = spf_device.getString(KEY_DEVICE_MANUFACTURER_ID, "");

        String macString = getResources().getString(R.string.mac_address) + " " + deviceMac;
        String manufacturerString = getResources().getString(R.string.manufacturer_number) + " " + deviceManufacturerId;
        deviceName_TextView.setText(deviceName);
        deviceMac_TextView.setText(macString);
        deviceManufacturer_TextView.setText(manufacturerString);
        setSpinner();

        //presenter.initializeChart(accelerometer_LineChart);
        //presenter.initializeChart(gyroscope_LineChart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter == null) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, ENABLE_BT_REQUEST);
        } else {
            bluetoothAdapter = BluetoothAdapterSingleton.getAdapter();
            connectDevice(bluetoothAdapter);
        }

        /*if (permissionAllowed(Manifest.permission.READ_CONTACTS, PERMISSION_REQUEST_CONTACTS_ACCESS)){
            //Toast.makeText(this, "Contacts", Toast.LENGTH_SHORT).show();
        }

        if (permissionAllowed(Manifest.permission.RECEIVE_SMS, PERMISSION_REQUEST_SMS_ACCESS)){
            //Toast.makeText(this, "SMS", Toast.LENGTH_SHORT).show();
        }

        if (permissionAllowed(Manifest.permission.READ_PHONE_STATE, PERMISSION_REQUEST_CALL_ACCESS)){
            //Toast.makeText(this, "Calls", Toast.LENGTH_SHORT).show();
        }

        if (permissionAllowed(Manifest.permission.READ_CALENDAR, PERMISSION_REQUEST_CALENDER_ACCESS)){
            //Toast.makeText(this, "Calender", Toast.LENGTH_SHORT).show();
        }*/

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //presenter.sendCommand(device, COMMAND_CLOSE_GATT);
        finish();
    }

    @Override
    protected void onDestroy() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        manager.getAdapter().disable();
        super.onDestroy();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_device, menu);
        return true;
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ENABLE_BT_REQUEST:
                connectDevice(bluetoothAdapter);
                break;

            /*case PERMISSION_REQUEST_SMS_ACCESS:
                if (resultCode == RESULT_OK){
                    Toast.makeText(this, "SMS Access Allowed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "SMS Access Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case PERMISSION_REQUEST_CALL_ACCESS:
                if (resultCode == RESULT_OK){
                    Toast.makeText(this, "Call Access Allowed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Call Access Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case PERMISSION_REQUEST_CALENDER_ACCESS:
                if (resultCode == RESULT_OK){
                    Toast.makeText(this, "Calender Access Allowed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Calender Access Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case PERMISSION_REQUEST_CONTACTS_ACCESS:
                if (resultCode == RESULT_OK){
                    Toast.makeText(this, "Contacts Access Allowed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Contacts Access Denied", Toast.LENGTH_SHORT).show();
                }
                break;*/

        }
    }

    @OnClick(R.id.onlineGymStart_Button)
    public void getOnlineGymData() {
        if (device != null) {
            Object channel = channelSpinner.getSelectedItem().toString();

            Object bytes = writeBytes_EditText.getText();

            if (bytes.toString().trim().equals("")) {
                logs_TextView.setText(R.string.cannot_be_blank);
            } else if (!presenter.checkBytes(channel, bytes)) {
                logs_TextView.setText(R.string.invalid_frame);
            }

            //presenter.sendCommand(device, COMMAND_GYM_ONLINE_START);
        }
    }

    @OnClick(R.id.unpair_ImageView)
    public void unPairDevice() {
        SharedPreferences spf_device = getSharedPreferences(SPF_DEVICE, MODE_PRIVATE);
        SharedPreferences.Editor editor = spf_device.edit();
        editor.putString(KEY_DEVICE_NAME, "");
        editor.putString(KEY_DEVICE_MAC, "");
        editor.putString(KEY_DEVICE_MANUFACTURER_ID, "");
        editor.apply();
        finish();
    }

    @OnClick(R.id.connectivityIndication_ImageView)
    public void indicatorClick(){
        if (!isDeviceConnected){
            connectivityIndication_ImageView.setBackgroundResource(R.drawable.dot_orange);
            connectDevice(bluetoothAdapter);
        }
    }

    @Override
    public void deviceConnected() {
        isDeviceConnected = true;
        connectivityIndication_ImageView.setBackgroundResource(R.drawable.dot_green);
    }

    @Override
    public void deviceDisconnected() {
        isDeviceConnected = false;
        connectivityIndication_ImageView.setBackgroundResource(R.drawable.dot_red);
    }

    @Override
    public void onServicesDiscovered(List<BluetoothGattService> gattServices) {
        //service_list.setAdapter(new BLEServicesListAdapter(this, gattServices));
    }

    @Override
    public void onDataReceived(String raw, String formatted) {
        try {
            logs_TextView.setText(raw);
            /*if (formatted != null) {
                addEntry(accelerometer_LineChart, formatted.split(","),3 , 0, 3);
                //addEntry(gyroscope_LineChart, formatted.split(","),3 , 3, 6);
            }*/
        } catch (Exception e) {
            //
        }
    }

    private void connectDevice(BluetoothAdapter adapter) {
        if (adapter == null){
            adapter = BluetoothAdapterSingleton.getAdapter();
        }
        device = adapter.getRemoteDevice(deviceMac);
        if (device != null) {
            presenter.connnectDevice(device);
            //service_list.setAdapter(new BLEServicesListAdapter(this, BLEInteractor.bluetoothGatt.getServices()));
        } else {
            Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean permissionAllowed(String permissionName, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(permissionName)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permissionName}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * Method to add entries to the real-time graph.
     */
    private void addEntry(LineChart lineChart, String[] formatted, int modValue, int start, int end) {
        try {
            LineData data = lineChart.getData();
            if (data != null) {
                for (int i = start; i < end; i++) {
                    ILineDataSet set = data.getDataSetByIndex(i % modValue);
                    if (set == null) {
                        set = presenter.createSet(i);
                        data.addDataSet(set);
                    }
                    data.addEntry(new Entry(set.getEntryCount(), Float.parseFloat(formatted[i])), i % modValue);
                    data.notifyDataChanged();
                }
                lineChart.notifyDataSetChanged();
                lineChart.setVisibleXRangeMaximum(140);
                lineChart.moveViewToX(data.getEntryCount());
            }
        } catch (Exception e) {
            //
        }
    }

    private void setSpinner() {
        List<String> list = new ArrayList<>();
        list.add(COMMAND_GYM_ONLINE_START);
        list.add(COMMAND_GYM_OFFLINE_READ);
        list.add(COMMAND_LIFE_LOG_ONLINE_START);
        list.add(COMMAND_LIFE_LOG_OFFLINE_READ);
        list.add(COMMAND_SLEEP_READ);
        list.add(COMMAND_SESSION_COUNT);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        channelSpinner.setAdapter(adapter);
    }

}
