package com.nikhil.bletrial.bluetooth.device;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nikhil.bletrial.R;

import java.util.List;

import static com.nikhil.bletrial.app.AppConstants.TAG;


/**
 * Created by nikhil on 17/04/17.
 */

public class BLEServicesListAdapter extends RecyclerView.Adapter<BLEServicesListAdapter.ServicesViewHolder> {

    private LayoutInflater inflater;
    private List<BluetoothGattService> gattServices;

    public BLEServicesListAdapter(Context context, List<BluetoothGattService> gattServices) {
        this.inflater = LayoutInflater.from(context);
        this.gattServices = gattServices;
        Log.d(TAG, "BLEServicesListAdapter: " + this.gattServices.size());
    }

    @Override
    public ServicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_gatt_services, parent, false);
        return new ServicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServicesViewHolder holder, int position) {
        BluetoothGattService service = gattServices.get(position);
        Log.d(TAG, "onBindViewHolder: " + position + " : " + service.getUuid());
        holder.serviceValue_TextView.setText(service.getUuid().toString());
    }

    @Override
    public int getItemCount() {
        return gattServices.size();
    }

    class ServicesViewHolder extends RecyclerView.ViewHolder {

        TextView serviceName_TextView;
        TextView serviceValue_TextView;

        ServicesViewHolder(View itemView) {
            super(itemView);
            serviceName_TextView = (TextView) itemView.findViewById(R.id.serviceName_TextView);
            serviceValue_TextView = (TextView) itemView.findViewById(R.id.serviceValue_TextView);
        }
    }
}
