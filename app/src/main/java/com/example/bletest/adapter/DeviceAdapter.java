package com.example.bletest.adapter;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.example.bletest.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>{

    private List<BleDevice> mBleDeviceList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View deviceView;

        ImageView img_blue;

        TextView txt_name;
        TextView txt_mac;
        TextView txt_rssi;

        Button btn_connect;

        ViewHolder(View view) {
            super(view);

            deviceView = view;

            img_blue = (ImageView) view.findViewById(R.id.img_bluetooth);
            txt_name = (TextView) view.findViewById(R.id.txt_device_name);
            txt_mac = (TextView) view.findViewById(R.id.txt_mac);
            txt_rssi = (TextView) view.findViewById(R.id.txt_rssi);
            btn_connect = (Button) view.findViewById(R.id.btn_connect);
        }
    }

    public DeviceAdapter(List<BleDevice> bleDeviceList) {
        mBleDeviceList = bleDeviceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                BleDevice bleDevice = mBleDeviceList.get(position);
                Toast.makeText(view.getContext(), "click view " + bleDevice.getName() , Toast.LENGTH_SHORT).show();

                if (mListener != null) {
                    mListener.onDetail(bleDevice);
                }
            }
        });

        holder.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                BleDevice bleDevice = mBleDeviceList.get(position);
                Toast.makeText(view.getContext(), "click button " + bleDevice.getName() , Toast.LENGTH_SHORT).show();

                if (mListener != null) {
                    mListener.onConnect(bleDevice);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BleDevice bleDevice = mBleDeviceList.get(position);

        String name = bleDevice.getName();
        String mac = bleDevice.getMac();
        int rssi = bleDevice.getRssi();

        holder.img_blue.setImageResource(R.mipmap.ic_blue_remote);
        // 设备名称为空时, 强制将设备名称设置为N/A
        holder.txt_name.setText((name == null) || (name.length() == 0) ? "N/A" : name);
        holder.txt_mac.setText(mac);
        holder.txt_rssi.setText(String.valueOf(rssi));
    }

    @Override
    public int getItemCount() {
        return mBleDeviceList.size();
    }

    public void addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        mBleDeviceList.add(bleDevice);
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < mBleDeviceList.size(); i++) {
            BleDevice device = mBleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                mBleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < mBleDeviceList.size(); i++) {
            BleDevice device = mBleDeviceList.get(i);
            if (BleManager.getInstance().isConnected(device)) {
                mBleDeviceList.remove(i);
            }
        }
    }

    public void clearScanDevice() {
        for (int i = 0; i < mBleDeviceList.size(); i++) {
            BleDevice device = mBleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device)) {
                mBleDeviceList.remove(i);
            }
        }
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);
        void onDetail(BleDevice bleDevice);
    }

    private OnDeviceClickListener mListener;

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }
}
