package com.nikhil.bletrial.bluetooth.scan;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nikhil.bletrial.bluetooth.utils.manufacturer_id.ManufacturerId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nikhil.bletrial.app.AppConstants.TAG;

/**
 * Created by nikhil on 29/03/17.
 */

public class DeviceListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ScanResult> deviceArrayList;

    public DeviceListViewAdapter(Context context, HashMap<String, ScanResult> scanResultHashMap) {
        this.context = context;
        this.deviceArrayList = new ArrayList<>(scanResultHashMap.values());
        Log.d(TAG, "onScanSuccess: ======================== Device List =============================");
        for (Map.Entry entry : scanResultHashMap.entrySet()) {
            ScanResult scanResult = (ScanResult) entry.getValue();
            BluetoothDevice device = scanResult.getDevice();
            Log.d(TAG, "onScanSuccess: " + entry.getKey() + " : " + scanResult.getRssi() + " : "
                    + device.getName() + " : " + new ManufacturerId().getManufacturerId(scanResult));
        }
        Log.d(TAG, "onScanSuccess: ================================================================\n");
    }

    @Override
    public int getCount() {
        return deviceArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = ((Activity) context).getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        ScanResult scanResult = (ScanResult) getItem(position);
        BluetoothDevice device = scanResult.getDevice();
        holder.text1.setText(device.getName());
        holder.text2.setText(device.getAddress());

        return view;
    }

    public static class ViewHolder {
        @BindView(android.R.id.text1)
        TextView text1;
        @BindView(android.R.id.text2)
        TextView text2;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
