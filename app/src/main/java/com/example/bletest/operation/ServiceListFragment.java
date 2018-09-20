package com.example.bletest.operation;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.example.bletest.R;
import com.example.bletest.operation.OperationActivity;

import java.util.ArrayList;
import java.util.List;

public class ServiceListFragment extends Fragment {

    private TextView txtServiceName;
    private TextView txtServiceMac;

    private ResultAdapter mResultAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_list, null);
        initView(view);
        showData();

        return view;
    }

    private void initView(View view) {
        txtServiceName = (TextView) view.findViewById(R.id.txt_service_device_name);
        txtServiceMac = (TextView)view.findViewById(R.id.txt_service_mac);

        mResultAdapter = new ResultAdapter(getActivity());

        ListView listViewDevice = (ListView) view.findViewById(R.id.list_service);
        listViewDevice.setAdapter(mResultAdapter);
        listViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BluetoothGattService service = mResultAdapter.getItem(position);
                ((OperationActivity) getActivity()).setBluetoothGattService(service);
                ((OperationActivity) getActivity()).changePage(1);
            }
        });
    }

    private void showData() {
        BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();

        String name = bleDevice.getName();
        String mac = bleDevice.getMac();

        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);

        txtServiceName.setText(String.valueOf("设备广播名: " + name));
        txtServiceMac.setText(String.valueOf("MAC: " + mac));

        mResultAdapter.clear();
        for (BluetoothGattService service : gatt.getServices()) {
            mResultAdapter.addResult(service);
        }
        mResultAdapter.notifyDataSetChanged();
    }

    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<BluetoothGattService> bluetoothGattServiceList;

        private ResultAdapter(Context context) {
            this.context = context;
            bluetoothGattServiceList = new ArrayList<>();
        }

        private void addResult(BluetoothGattService service) {
            bluetoothGattServiceList.add(service);
        }

        private void clear() {
            bluetoothGattServiceList.clear();
        }

        @Override
        public int getCount() {
            return bluetoothGattServiceList.size();
        }

        @Override
        public BluetoothGattService getItem(int position) {
            if (position > bluetoothGattServiceList.size()) {
                return null;
            }
            return bluetoothGattServiceList.get(position);
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
                view = View.inflate(context, R.layout.adapter_service, null);
                holder = new ViewHolder();
                view.setTag(holder);

                holder.txt_service_name = (TextView) view.findViewById(R.id.txt_service_name);
                holder.txt_service_uuid = (TextView) view.findViewById(R.id.txt_service_uuid);
                holder.txt_service_type = (TextView) view.findViewById(R.id.txt_service_type);
            }

            BluetoothGattService service = bluetoothGattServiceList.get(position);
            String uuid = service.getUuid().toString();

            holder.txt_service_name.setText(String.valueOf(getActivity().getString(R.string.service) + "(" + position + ")"));
            holder.txt_service_uuid.setText(uuid);
            holder.txt_service_type.setText(getActivity().getString(R.string.type));

            return view;
        }

        class ViewHolder {
            TextView txt_service_name;
            TextView txt_service_uuid;
            TextView txt_service_type;
        }
    }
}
