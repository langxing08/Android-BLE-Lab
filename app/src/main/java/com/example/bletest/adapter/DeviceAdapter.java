package com.example.bletest.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.example.bletest.Device;
import com.example.bletest.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter{

    private Context context;
    private int resourceId;
    private List<BleDevice> mBleDeviceList;

    public DeviceAdapter(Context context, int resourceId, List<BleDevice> objects) {
        this.context = context;
        this.resourceId = resourceId;
        this.mBleDeviceList = objects;
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

    @Override
    public int getCount() {
        return mBleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        if (position > mBleDeviceList.size()) {
            return null;
        }
        return mBleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BleDevice bleDevice = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.img_blue = (ImageView) view.findViewById(R.id.img_bluetooth);
            viewHolder.txt_name = (TextView) view.findViewById(R.id.txt_device_name);
            viewHolder.txt_mac = (TextView) view.findViewById(R.id.txt_mac);
            viewHolder.txt_rssi = (TextView) view.findViewById(R.id.txt_rssi);
            viewHolder.btn_connect = (Button) view.findViewById(R.id.btn_connect);

            view.setTag(viewHolder);  // 将ViewHolder存储在View中
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();  // 重新获取ViewHolder
        }

        if (bleDevice != null) {
            String name = bleDevice.getName();
            String mac = bleDevice.getMac();
            int rssi = bleDevice.getRssi();

            viewHolder.img_blue.setImageResource(R.mipmap.ic_blue_remote);
            // 设备名称为空时, 强制将设备名称设置为N/A
            viewHolder.txt_name.setText((name == null) || (name.length() == 0) ? "N/A" : name);
            viewHolder.txt_mac.setText(mac);
            viewHolder.txt_rssi.setText(String.valueOf(rssi));
        }

        return view;
    }

    class ViewHolder {
        ImageView img_blue;

        TextView txt_name;
        TextView txt_mac;
        TextView txt_rssi;

        Button btn_connect;
    }
}
