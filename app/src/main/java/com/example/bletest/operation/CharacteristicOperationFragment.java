package com.example.bletest.operation;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.example.bletest.R;

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

    public void showData() {
        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        final BluetoothGattCharacteristic characteristic = ((OperationActivity) getActivity()).getBluetoothGattCharacteristic();
        final int charaProp = ((OperationActivity) getActivity()).getCharaProp();
        final String child = characteristic.getUuid().toString() + String.valueOf(charaProp);

        for (int i = 0; i < layout_container.getChildCount(); i++) {
            layout_container.getChildAt(i)
                    .setVisibility(View.GONE);
        }
        if (childList.contains(child)) {
            layout_container.findViewWithTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp)
                    .setVisibility(View.VISIBLE);
        } else {
            childList.add(child);

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteristic_operation, null);
            view.setTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp);
            LinearLayout layoutAdd = (LinearLayout) view.findViewById(R.id.layout_add);
            final TextView txtTitle = (TextView) view.findViewById(R.id.txt_title);
            txtTitle.setText(String.valueOf(characteristic.getUuid().toString() + "的数据变化: "));
            final TextView txt = (TextView) view.findViewById(R.id.txt);
            txt.setMovementMethod(ScrollingMovementMethod.getInstance());

            switch (charaProp) {
                case PROPERTY_READ: {
                    View viewAdd = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteristic_operation_button, null);
                    Button btn = (Button) viewAdd.findViewById(R.id.btn);
                    btn.setText(getString(R.string.read)); // 读
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            BleManager.getInstance().read(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    new BleReadCallback() {
                                        @Override
                                        public void onReadSuccess(final byte[] data) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, HexUtil.formatHexString(data, true));  // HEX Display
                                                    addText(txt, new String(data)); // ASCII Display
                                                }
                                            });
                                        }

                                        @Override
                                        public void onReadFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    layoutAdd.addView(viewAdd);
                }
                break;

                case PROPERTY_WRITE: {
                    View viewAdd = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteristic_operation_et, null);
                    final EditText et = (EditText) viewAdd.findViewById(R.id.et);
                    Button btn = (Button) viewAdd.findViewById(R.id.btn);
                    btn.setText(getString(R.string.write)); // 写
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String hex = et.getText().toString();
                            if (TextUtils.isEmpty(hex)) {
                                return;
                            }
                            BleManager.getInstance().write(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    HexUtil.hexStringToBytes(hex),
                                    new BleWriteCallback() {
                                        @Override
                                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, "write success, current: " + current
                                                            + " total: " + total
                                                            + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                                }
                                            });
                                        }

                                        @Override
                                        public void onWriteFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    }
                            );
                        }
                    });
                    layoutAdd.addView(viewAdd);
                }
                break;

                case PROPERTY_WRITE_NO_RESPONSE: {
                    View viewAdd = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteristic_operation_et, null);
                    final EditText et = (EditText) viewAdd.findViewById(R.id.et);
                    Button btn = (Button) viewAdd.findViewById(R.id.btn);
                    btn.setText(getString(R.string.write)); // 写
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String hex = et.getText().toString();
                            if (TextUtils.isEmpty(hex)) {
                                return;
                            }
                            BleManager.getInstance().write(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    HexUtil.hexStringToBytes(hex),
                                    new BleWriteCallback() {
                                        @Override
                                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, "write success, current: " + current
                                                            + " total: " + total
                                                            + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                                }
                                            });
                                        }

                                        @Override
                                        public void onWriteFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    }
                            );
                        }
                    });
                    layoutAdd.addView(viewAdd);
                }
                break;

                case PROPERTY_NOTIFY: {
                    View viewAdd = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteristic_operation_button, null);
                    final Button btn = (Button) viewAdd.findViewById(R.id.btn);
                    btn.setText(getString(R.string.notify_enable)); // 打开通知
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (btn.getText().toString().equals(getString(R.string.notify_enable))) { // 打开通知
                                btn.setText(getString(R.string.notify_disable)); // 关闭通知
                                BleManager.getInstance().notify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleNotifyCallback() {
                                            @Override
                                            public void onNotifySuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, "notify success");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onNotifyFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(byte[] data) {
                                                addText(txt, HexUtil.formatHexString(characteristic.getValue()));
                                            }
                                        }
                                );
                            } else {
                                btn.setText(getString(R.string.notify_enable)); // 打开通知
                                BleManager.getInstance().stopNotify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });
                    layoutAdd.addView(viewAdd);
                }
                break;

                case PROPERTY_INDICATE: {
                    View viewAdd = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteristic_operation_button, null);
                    final Button btn = (Button) viewAdd.findViewById(R.id.btn);
                    btn.setText(getString(R.string.indicate_enable)); // 打开指示
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (btn.getText().toString().equals(getString(R.string.indicate_enable))) { // 打开指示
                                btn.setText(getString(R.string.indicate_disable)); // 关闭指示
                                BleManager.getInstance().indicate(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleIndicateCallback() {
                                            @Override
                                            public void onIndicateSuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, "indicate success");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onIndicateFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(byte[] data) {
                                                addText(txt, HexUtil.formatHexString(characteristic.getValue()));
                                            }
                                        }
                                );
                            } else {
                                btn.setText(getString(R.string.indicate_enable)); // 打开指示
                                BleManager.getInstance().stopIndicate(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });
                    layoutAdd.addView(viewAdd);
                }
                break;
            }

        layout_container.addView(view);
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && (getActivity() != null)) {
            getActivity().runOnUiThread(runnable);
        }
    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");

        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

}