package com.nikhil.bletrial.bluetooth.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nikhil.bletrial.app.AppConstants;
import com.nikhil.bletrial.bluetooth.utils.BLEInteractor;
import com.nikhil.bletrial.bluetooth.utils.OnBLEInteractorFinishedListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import static com.google.android.gms.internal.zzt.TAG;

/**
 * Created by nikhil on 05/04/17.
 */

public class ActofitDevicePresenter implements IActofitDevicePresenter, OnBLEInteractorFinishedListener {

    private BLEInteractor interactor;
    private WeakReference<IActofitDeviceView> view;
    private Context context;

    public ActofitDevicePresenter(IActofitDeviceView view, Context context) {
        this.view = new WeakReference<IActofitDeviceView>(view);
        this.context = context;
        interactor = new BLEInteractor(this, context);
    }

    @Override
    public void connnectDevice(BluetoothDevice device) {
        interactor.connectDevice(device);
    }

    @Override
    public void sendCommand(BluetoothDevice device, int command) {
        //interactor.sendCommand(device, command);
    }

    @Override
    public boolean checkBytes(Object channel, Object bytes) {
        if (bytes != null) {
            return interactor.checkBytes(String.valueOf(channel),
                    bytes.toString().toUpperCase());
        } else {
            return false;
        }
    }

    @Override
    public void onScanSuccess(HashMap<String, ScanResult> scanResultHashMap) {

    }

    @Override
    public void onScanFailure(int FAILURE_CODE) {

    }

    @Override
    public void connectionState(int state) {
        if (view.get() != null) {
            switch (state) {
                case BluetoothProfile.STATE_CONNECTED:
                    view.get().deviceConnected();
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    view.get().deviceDisconnected();
                    break;
            }
        }
    }

    @Override
    public void servicesDiscovered(List<BluetoothGattService> bluetoothGattServiceList) {
        if (view.get() != null) {
            view.get().onServicesDiscovered(bluetoothGattServiceList);
        }
    }

    @Override
    public void onDataReceived(String raw, String formatted) {
        //Log.d(AppConstants.TAG, "ActofitDevicePresenter: onDataReceived: " + raw);
        if (view.get() != null) {
            view.get().onDataReceived(raw, formatted);
        }
    }

    public void initializeChart(LineChart lineChart) {
        //maxValue = 70000;

        //lineChart.setOnChartValueSelectedListener(this);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(true);
        lineChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        lineChart.setData(data);
        Legend l = lineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = lineChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = lineChart.getAxisRight();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(50000);
        leftAxis.setAxisMinimum(25000);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisLeft();
        rightAxis.setEnabled(false);
    }

    public LineDataSet createSet(int i) {
        int color;
        String dataType;
        switch (i) {
            case 0:
                color = ColorTemplate.getHoloBlue();
                dataType = "ax";
                break;
            case 1:
                color = ColorTemplate.MATERIAL_COLORS[0];
                dataType = "ay";
                break;
            case 2:
                color = ColorTemplate.MATERIAL_COLORS[2];
                dataType = "az";
                break;
            case 3:
                color = ColorTemplate.getHoloBlue();
                dataType = "gx";
                break;
            case 4:
                color = ColorTemplate.MATERIAL_COLORS[0];
                dataType = "gy";
                break;
            case 5:
                color = ColorTemplate.MATERIAL_COLORS[2];
                dataType = "gz";
                break;
            default:
                color = ColorTemplate.getHoloBlue();
                dataType = "" + i;
                break;
        }
        LineDataSet set = new LineDataSet(null, dataType);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setColor(color);
        set.setFillColor(color);
        set.setHighlightEnabled(false);
        //set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);

        return set;
    }

}
