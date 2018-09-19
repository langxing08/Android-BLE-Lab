package com.example.bletest;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class CharacteristicOperationFragment extends Fragment {

    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;

    private LinearLayout layout_container;
    private List<String> childList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_characteristic_operation, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        layout_container = (LinearLayout) view.findViewById(R.id.layout_container);
    }

    private void showData() {
        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        final BluetoothGattCharacteristic characteristic = ((OperationActivity) getActivity()).getBluetoothGattCharacteristic();
        final int charaProp = ((OperationActivity) getActivity()).getCharaProp();
        String child = characteristic.getUuid().toString() + String.valueOf(charaProp);

        for (int i = 0; i < layout_container.getChildCount(); i++) {
            layout_container.getChildAt(i)
                    .setVisibility(View.GONE);
        }
        if (childList.contains(child)) {
            layout_container.findViewWithTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp)
                    .setVisibility(View.VISIBLE);
        } else {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteristic_operation, null);
            view.setTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp);
            LinearLayout layoutAdd = (LinearLayout) view.findViewById(R.id.layout_add);
            final TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
            txtTitle.setText(String.valueOf(characteristic.getUuid().toString() + "的数据变化: "));
            final TextView txt = (TextView) view.findViewById(R.id.txt);
            txt.setMovementMethod(ScrollingMovementMethod.getInstance());






        }
    }
}
