package com.nikhil.bletrial.bluetooth.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static com.nikhil.bletrial.app.AppConstants.Storage.A_FOLDER;
import static com.nikhil.bletrial.app.AppConstants.TAG;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_GYM_OFFLINE_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_GYM_ONLINE_START;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_LIFE_LOG_OFFLINE_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_LIFE_LOG_ONLINE_START;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_SESSION_COUNT;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.COMMANDS.COMMAND_SLEEP_READ;
import static com.nikhil.bletrial.bluetooth.utils.BTConstants.SCAN_FAILED;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.CHAR_NOTIFY_GYM;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.CHAR_NOTIFY_OFFLINE_LIFE_LOG;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.CHAR_NOTIFY_ONLINE_LIFE_LOG;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.CHAR_NOTIFY_WALK;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.UUID_CHAR_FIRMWARE_REVISION;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.UUID_CHAR_WRITE;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.UUID_CLIENT_CONFIG;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.UUID_SERVICE_DEVICE_INFORMATION;
import static com.nikhil.bletrial.bluetooth.utils.GattAttributes.UUID_SERVICE_WORKOUT;

/**
 * Created by nikhil on 27/03/17.
 */

public class BLEInteractor {

    private static final long SCAN_TIMEOUT_INTERVAL = 10000;
    public static String DEVICE_MAC = "mac";
    private static BluetoothGatt bluetoothGatt;
    private static HashMap<String, String> notifyCharacteristics = new HashMap<>();
    private static String CHANNEL = "";
    private static String filePath;
    public static String FIRMWARE_VERSION;

    static {
        notifyCharacteristics.put(COMMAND_GYM_ONLINE_START, CHAR_NOTIFY_GYM);                       // 4
        notifyCharacteristics.put(COMMAND_GYM_OFFLINE_READ, CHAR_NOTIFY_GYM);                       // 4
        notifyCharacteristics.put(COMMAND_LIFE_LOG_ONLINE_START, CHAR_NOTIFY_ONLINE_LIFE_LOG);      // 11
        notifyCharacteristics.put(COMMAND_LIFE_LOG_OFFLINE_READ, CHAR_NOTIFY_OFFLINE_LIFE_LOG);     // 9
        notifyCharacteristics.put(COMMAND_SLEEP_READ, CHAR_NOTIFY_OFFLINE_LIFE_LOG);                // 9
        notifyCharacteristics.put(COMMAND_SESSION_COUNT, CHAR_NOTIFY_WALK);                         // 6
    }

    FileWriter csvFileWriter;
    private Context context;
    private OnBLEInteractorFinishedListener listener;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    private List<ScanFilter> scanFilterList;
    private HashMap<String, ScanResult> scannedResultHashMap;
    //private BluetoothGattCharacteristic gattCharacteristic;
    private String DEVICE_NAME = "name";
    private boolean isMediaMounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    private byte[] bytes;
    private boolean wasDisconnected, fromGym;
    /**
     * ScanCallback returns a list of devices within the range of the phone.
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            scannedResultHashMap.put(device.getAddress(), result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            listener.onScanFailure(SCAN_FAILED);
        }
    };
    private File rawFile;
    ParseBytes parseBytes;
    /**
     * device.connectGatt(context, false, gattCallBack) refers to this gattCallBack.
     * <p>
     * BluetoothGattCallback provides following methods:
     * onConnectionStateChange  : changes in Connection state.
     * onServicesDiscovered     : gives list of services provided by the connected device.
     * onCharacteristicRead     : is called when readCharacteristics is called on a characteristic provided by service.
     * onCharacteristicChanged  : when a characteristic changes.
     */
    private final BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "onConnectionStateChange: STATE_CONNECTED");
                    gatt.discoverServices();
                    if (wasDisconnected && fromGym){
                        executeCommand(COMMAND_GYM_ONLINE_START, bytes);
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    wasDisconnected = true;
                    Log.d(TAG, "onConnectionStateChange: STATE_DISCONNECTED");
                    break;

                default:
                    Log.d(TAG, "onConnectionStateChange: newState: " + newState);
                    break;
            }
            listener.connectionState(newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            /*List<BluetoothGattService> gattServiceList = bluetoothGatt.getServices();
            listener.servicesDiscovered(gattServiceList);*/
            Log.d(TAG, "onServicesDiscovered: ");
            BluetoothGattCharacteristic characteristic = gatt.getService(UUID_SERVICE_DEVICE_INFORMATION)
                    .getCharacteristic(UUID_CHAR_FIRMWARE_REVISION);
            gatt.readCharacteristic(characteristic);
            bluetoothGatt = gatt;
            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: called");
                    checkBytes(COMMAND_GYM_ONLINE_START, "AA0055AA");
                }
            }, 5000);*/

            // 17:28:57 => 17:46:20

            /*Log.d(TAG, "onServicesDiscovered: **************** UUIDs for services ***************");
            for (BluetoothGattService s : gatt.getServices()) {
                Log.d(TAG, "onServicesDiscovered: " + s.getUuid().toString());
            }
            Log.d(TAG, "onServicesDiscovered: ***************************************************");*/

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

    public BLEInteractor(OnBLEInteractorFinishedListener listener, Context context) {
        this.context = context;
        this.listener = listener;
    }

    private void notifyBLE(BluetoothGattCharacteristic notifyCharacteristic, boolean enable) {
        bluetoothGatt.setCharacteristicNotification(notifyCharacteristic, enable);
        BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(UUID_CLIENT_CONFIG);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(ENABLE_NOTIFICATION_VALUE);
        Log.d(TAG, "writeDescriptor: " + notifyCharacteristic.getUuid() + " : " + bluetoothGatt.writeDescriptor(descriptor));
    }

    public void initiateScanProcess(BluetoothAdapter bluetoothAdapter) {
        scannedResultHashMap = new HashMap<>();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanFilterList = new ArrayList<>();
        scanLeDevice(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scannedResultHashMap.size() > 0) {
                    listener.onScanSuccess(scannedResultHashMap);
                }
            }
        }, SCAN_TIMEOUT_INTERVAL);
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothLeScanner.stopScan(scanCallback);
                }
            }, SCAN_TIMEOUT_INTERVAL);
            bluetoothLeScanner.startScan(scanFilterList, scanSettings, scanCallback);
        } else {
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    public void connectDevice(BluetoothDevice device) {
        DEVICE_NAME = device.getName();
        DEVICE_MAC = device.getAddress();
        bluetoothGatt = device.connectGatt(context, false, gattCallBack);
    }

    public boolean checkBytes(String channel, String s) {
        if (s == null) {
            return false;
        } else if (s.length() % 2 != 0) {
            return false;
        } else {
            bytes = hexStringToByteArray(s);
            executeCommand(channel, bytes);
            return true;
        }
    }

    private void executeCommand(String channel, byte[] bytes) {
        filePath = A_FOLDER + "/" + channel.replace(" ", "_");
        File folder = new File(Environment.getExternalStorageDirectory() + filePath);
        folder.mkdir();
        filePath = filePath + "/" +
                new SimpleDateFormat("yyyyMMddkkmmss", Locale.ENGLISH).format(new Date()) + ".csv";
        CHANNEL = channel;
        Log.d(TAG, "executeCommand: " + CHANNEL);
        parseBytes = new ParseBytes(channel);

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

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
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
            listener.onDataReceived(frame, formatted);
            writeFile(formatted);

        } else {
            Log.d(TAG, "handleData: else");
        }
    }

    private void writeFile(String s) {
        if (isMediaMounted) {
            try {
                rawFile = new File(Environment.getExternalStorageDirectory() + filePath);
                csvFileWriter = new FileWriter(rawFile, true);
                csvFileWriter.append(s + "," + new SimpleDateFormat("kkmmss", Locale.ENGLISH).format(new Date()) + "\n");
                csvFileWriter.close();
            } catch (IOException e) {
                Log.d(TAG, "writeFile: " + e.toString());
                //e.printStackTrace();
            }
        }
    }

    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    /*public void connectDevice(ScanResult scanResult) {
        BluetoothDevice device = scanResult.getDevice();
        connectDevice(device);
    }*/

    /*public void sendCommand(BluetoothDevice device, int command) {
        Log.d(TAG, "======================== sendCommand: " + command + " ========================");
        COMMAND = command;

        switch (COMMAND) {
            case COMMAND_CLOSE_GATT:
                close();
                return;

            case COMMAND_GYM_ONLINE_START:
                //fromGym = true;
                bytes = new byte[]{(byte) 0xAA, (byte) 0x00, (byte) 0x55, (byte) 0xAA};
                break;

            case COMMAND_GYM_ONLINE_STOP:
                bytes = new byte[]{(byte) 0xAA, (byte) 0x00, (byte) 0x66, (byte) 0xAA};
                break;

            case COMMAND_LIFE_LOG_OFFLINE_READ:
                bytes = new byte[]{(byte) 0xAF, (byte) 0x00, (byte) 0x01, (byte) 0xAF};
                break;

            case COMMAND_LIFE_LOG_OFFLINE_ERASE:
                //bytes = new byte[]{(byte) 0xCE, (byte) 0x00, (byte) 0x01, (byte) 0xAF};
                break;

            case COMMAND_LIFE_LOG_ONLINE_START:
                bytes = new byte[]{(byte) 0xAF, (byte) 0x00, (byte) 0x55, (byte) 0xAF};
                break;

            case COMMAND_LIFE_LOG_ONLINE_STOP:
                bytes = new byte[]{(byte) 0xAF, (byte) 0x00, (byte) 0x66, (byte) 0xAF};
                break;
        }
        sendCommand(device);
    }

    private void sendCommand(BluetoothDevice device) {
        if (bluetoothGatt == null) {
            Log.d(TAG, "sendCommand: bluetoothGatt == null");
            bluetoothGatt = device.connectGatt(context.getApplicationContext(), false, gattCallBack);
        } else {
            Log.d(TAG, "sendCommand: bluetoothGatt != null");
            if (bluetoothGatt.connect()) {
                executeCommand();
            } else {
                listener.connectionState(BluetoothProfile.STATE_DISCONNECTED);
            }
        }
    }

    private void executeCommand() {
        BluetoothGattService service = bluetoothGatt.getService(UUID_SERVICE_WORKOUT);

        BluetoothGattCharacteristic gattCharacteristic = service.getCharacteristic(UUID_CHAR_WRITE);
        gattCharacteristic.setValue(bytes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
        }
        bluetoothGatt.writeCharacteristic(gattCharacteristic);

        BluetoothGattCharacteristic notifyCharacteristic = bluetoothGatt
                .getService(UUID_SERVICE_WORKOUT).getCharacteristic(UUID_CHAR_NOTIFY_GYM);
        Log.d(TAG, "onServicesDiscovered: " + notifyCharacteristic.getUuid());
        notifyBLE(notifyCharacteristic, true);
    }*/

}


/*  Here is the general pattern for how things need to work with BLE on Android:
        1. You try to connect
        2. You get a callback indicating it is connected
        3. You discover services
        4. You are told services are discovered
        5. You get the characteristics
        6. For each characteristic you get the descriptors
        7. For the descriptor you set it to enable notification/indication with
           BluetoothGattDescriptor.setValue()
        8. You write the descriptor with BluetoothGatt.writeDescriptor()
        9. You enable notifications for the characteristic locally with
           BluetoothGatt.setCharacteristicNotification(). Without this you won't get called back.
       10. You get notification that the descriptor was written
       11. Now you can write data to the characteristic. All of the characteristic and
           descriptor configuration has do be done before anything is written to any characteristic.
*/