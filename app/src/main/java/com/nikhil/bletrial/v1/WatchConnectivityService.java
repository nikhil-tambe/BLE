package com.nikhil.bletrial.v1;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nikhil.bletrial.bluetooth.utils.BluetoothAdapterSingleton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static com.nikhil.bletrial.v1.ConstantsV1.KEY_CONNECTION_STATE;
import static com.nikhil.bletrial.v1.GattAttributes.CHAR_NOTIFY_GYM;
import static com.nikhil.bletrial.v1.GattAttributes.CHAR_NOTIFY_OFFLINE_LIFE_LOG;
import static com.nikhil.bletrial.v1.GattAttributes.CHAR_NOTIFY_ONLINE_LIFE_LOG;
import static com.nikhil.bletrial.v1.GattAttributes.CHAR_NOTIFY_WALK;
import static com.nikhil.bletrial.v1.GattAttributes.UUID_CHAR_FIRMWARE_REVISION;
import static com.nikhil.bletrial.v1.GattAttributes.UUID_CHAR_WRITE;
import static com.nikhil.bletrial.v1.GattAttributes.UUID_CLIENT_CONFIG;
import static com.nikhil.bletrial.v1.GattAttributes.UUID_SERVICE_DEVICE_INFORMATION;
import static com.nikhil.bletrial.v1.ConstantsV1.*;
import static com.nikhil.bletrial.app.AppConstants.Storage.A_FOLDER;
import static com.nikhil.bletrial.v1.ConstantsV1.COMMANDS.*;
import static com.nikhil.bletrial.v1.GattAttributes.UUID_SERVICE_WORKOUT;

/**
 * Created by Nikhil on 10/2/18.
 */

public class WatchConnectivityService extends IntentService {

    private static final String TAG = "oxs_ActofitService";
    private static HashMap<String, String> notifyCharacteristics = new HashMap<>();

    static {
        notifyCharacteristics.put(COMMAND_GYM_ONLINE_START, CHAR_NOTIFY_GYM);                       // 4
        notifyCharacteristics.put(COMMAND_GYM_OFFLINE_READ, CHAR_NOTIFY_GYM);                       // 4
        notifyCharacteristics.put(COMMAND_LIFE_LOG_ONLINE_START, CHAR_NOTIFY_ONLINE_LIFE_LOG);      // 11
        notifyCharacteristics.put(COMMAND_LIFE_LOG_OFFLINE_READ, CHAR_NOTIFY_OFFLINE_LIFE_LOG);     // 9
        notifyCharacteristics.put(COMMAND_SLEEP_READ, CHAR_NOTIFY_OFFLINE_LIFE_LOG);                // 9
        notifyCharacteristics.put(COMMAND_SESSION_COUNT, CHAR_NOTIFY_WALK);                         // 6
    }

    IBinder iBinder = new LocalBinder();
    BluetoothAdapter adapter;
    BluetoothGatt bluetoothGatt;
    boolean isDeviceConnected = false, isStartCommand = false;
    String FIRMWARE_VERSION;
    BluetoothDevice device;
    ParseBytes parseBytes;
    FileWriter csvFileWriter;
    SimpleDateFormat sdf = new SimpleDateFormat("kkmmss", Locale.ENGLISH);
    private boolean isMediaMounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    //
    private final BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "onConnectionStateChange: STATE_CONNECTED");
                    isDeviceConnected = true;
                    gatt.discoverServices();
                    /*if (wasDisconnected && fromGym){
                        executeCommand(COMMAND_GYM_ONLINE_START, bytes);
                    }*/
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    isDeviceConnected = false;
                    //wasDisconnected = true;
                    Log.d(TAG, "onConnectionStateChange: STATE_DISCONNECTED");
                    break;

                default:
                    Log.d(TAG, "onConnectionStateChange: newState: " + newState);
                    break;
            }
            sendBroadcast(new Intent(ACTION_CONNECTION_STATE)
                    .putExtra(KEY_CONNECTION_STATE, newState));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered: ");
            BluetoothGattCharacteristic characteristic = gatt.getService(UUID_SERVICE_DEVICE_INFORMATION)
                    .getCharacteristic(UUID_CHAR_FIRMWARE_REVISION);
            gatt.readCharacteristic(characteristic);
            bluetoothGatt = gatt;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead: " + characteristic.getValue());
            if (status == GATT_SUCCESS) {
                if (characteristic.getUuid().equals(UUID_CHAR_FIRMWARE_REVISION)) {
                    FIRMWARE_VERSION = characteristic.getStringValue(0);
                    Log.d(TAG, "onCharacteristicRead: Firmware Version: " + FIRMWARE_VERSION);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //listener.onDataReceived(characteristic.getValue().toString());
            handleData(characteristic);
        }
    };

    public WatchConnectivityService() {
        super("ActofitService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void init(String macAddress) {
        if (adapter == null) {
            adapter = BluetoothAdapterSingleton.getAdapter();
        }
        if (device == null) {
            device = adapter.getRemoteDevice(macAddress);
        }
        bluetoothGatt = device.connectGatt(this, false, gattCallBack);
    }

    public void sendStartSessionCommand() {
        String channel = COMMAND_GYM_ONLINE_START;
        String filePath = A_FOLDER + "/" + channel.replace(" ", "_");
        File folder = new File(Environment.getExternalStorageDirectory() + filePath);
        folder.mkdir();
        filePath = filePath + "/" +
                new SimpleDateFormat("yyyyMMddkkmmss", Locale.ENGLISH).format(new Date()) + ".csv";
        Log.d(TAG, "executeCommand: " + channel);
        parseBytes = new ParseBytes(channel);
        File rawFile = new File(Environment.getExternalStorageDirectory() + filePath);
        try {
            csvFileWriter = new FileWriter(rawFile, true);
        } catch (Exception e) {
            e.printStackTrace();
            sendBroadcast(new Intent(ACTION_ERROR_OCCURRED)
                    .putExtra(KEY_ERROR_MESSAGE, e.toString()));
            return;
        }
        isStartCommand = true;
        sendByteCommand(COMMAND_GYM_ONLINE_START, COMMAND_START_SENSOR);
    }

    public void sendStopSessionCommand() {
        isStartCommand = false;
        sendByteCommand(COMMAND_GYM_ONLINE_START, COMMAND_STOP_SENSOR);
        try {
            csvFileWriter.flush();
            csvFileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            //sendBroadcast(new Intent(ACTION_ERROR_OCCURRED).putExtra(KEY_ERROR_MESSAGE, e.toString()));
        }
    }

    public void sendByteCommand(String channel, String s) {
        byte[] bytes = hexStringToByteArray(s);
        executeCommand(channel, bytes);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void executeCommand(String channel, byte[] bytes) {
        if (bluetoothGatt != null) {
            BluetoothGattService service = bluetoothGatt.getService(UUID_SERVICE_WORKOUT);
            if (service != null) {

                BluetoothGattCharacteristic gattCharacteristic = service.getCharacteristic(UUID_CHAR_WRITE);
                gattCharacteristic.setValue(bytes);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH); //CONNECTION_PRIORITY_BALANCED);
                }
                bluetoothGatt.writeCharacteristic(gattCharacteristic);

                UUID channelUUID = UUID.fromString(notifyCharacteristics.get(channel));
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(channelUUID);
                notifyBLE(characteristic, true);
            }
        }
    }

    private void notifyBLE(BluetoothGattCharacteristic notifyCharacteristic, boolean enable) {
        bluetoothGatt.setCharacteristicNotification(notifyCharacteristic, enable);
        BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(UUID_CLIENT_CONFIG);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(ENABLE_NOTIFICATION_VALUE);
        boolean desc = bluetoothGatt.writeDescriptor(descriptor);
        String message = "";
        if (!desc) {
            message = "DESCRIPTOR => False";
        }
        sendBroadcast(new Intent(ACTION_ERROR_OCCURRED).putExtra(KEY_ERROR_MESSAGE, message));

        Log.d(TAG, "writeDescriptor: " + notifyCharacteristic.getUuid() + " : " + desc);
    }

    private void handleData(BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            String frame = stringBuilder.toString();
            String formatted = parseBytes.getParsedBytes(frame);

            //Log.d(TAG, "rawData: " + CHANNEL + " : " + frame);
            writeFile(formatted);
            sendBroadcast(new Intent(ACTION_DATA_RECEIVED)
                    .putExtra(KEY_PROCESSED_DATA, formatted));
        } else {
            Log.d(TAG, "handleData: else");
        }
    }

    private void writeFile(String s) {
        if (isMediaMounted) {
            try {
                csvFileWriter.append(s);
                csvFileWriter.append(",");
                csvFileWriter.append(sdf.format(new Date()));
                csvFileWriter.append("\n");
            } catch (IOException e) {
                Log.d(TAG, "writeFile: " + e.toString());
                sendBroadcast(new Intent(ACTION_ERROR_OCCURRED)
                        .putExtra(KEY_ERROR_MESSAGE, e.toString())
                        .putExtra(KEY_SHOULD_SHOW_MESSAGE, isStartCommand));
            }
        }
    }

    class LocalBinder extends Binder {
        public WatchConnectivityService getService() {
            return WatchConnectivityService.this;
        }
    }
}
