package com.example.bletest.operation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.example.bletest.ChatMsg;
import com.example.bletest.R;
import com.example.bletest.adapter.ChatMsgAdapter;
import com.example.bletest.tool.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicOperationFragment extends Fragment {

    private static final String TAG = "CharacteristicOperation";

    // Property
    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;

    // Notify and Indicate Property
    private static final int CHAR_PROPERTY_NO_NOTIFY_OR_INDICATE_SELECTED = 0;
    private static final int CHAR_PROPERTY_NOTIFY_SELECTED = 1;
    private static final int CHAR_PROPERTY_INDICATE_SELECTED = 2;

    private static final int CHAR_PROPERTY_NO_READ_SELECTED = 0;
    private static final int CHAR_PROPERTY_READ_SELECTED = 1;

    // Notify or Indicate select(如果同时存在, 则为Indicate)
    private static int charaPropNotifyOrIndicateSelect;
    private static int charaPropReadSelect;

    // Date Format
    private static final int DATA_FMT_STR = 0;  // UTF-8字符串格式
    private static final int DATA_FMT_HEX = 1;  // 十六进制格式
    private static final int DATA_FMT_DEC = 2;  // 十进制格式

    // 初始化控件
    private List<ChatMsg> mChatMsgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private ChatMsgAdapter adapter;
    private LinearLayoutManager layoutManager;

    private ArrayAdapter<String> fmtAdapter;    // 数据格式Adapter
    private static final String FMT_SELECT[] = { "Str", "Hex", "Dec" };
    private int charRecvFmtInt; // 接收数据格式
    private int charSendFmtInt; // 发送数据格式

    private RelativeLayout charReceivableLayout;// 接收区域Layout
    private Spinner charRecvFmtSelect;          // 读数据格式选择
    private ToggleButton charNotifyIndicateEnableBtn; // 通知/指示 使能按钮
    private Button charClearBtn;                // 清屏按钮
    private Button charReadBtn;                 // 读数据按钮

    private LinearLayout charSendableLayout;    // 发送区域Layout
    private CheckBox charSendOnTimeCheckbox;    // 定时发送复选框
    private EditText charSendOnTimeEdit;        // 定时发送时间
    private Spinner charSendFmtSelect;         // 写数据格式选择
    private EditText charWriteStringEdit;       // 写数据输入框(UTF-8格式)
    private EditText charWriteHexEdit;          // 写数据输入框(十六进制格式)
    private EditText charWriteDecEdit;          // 写数据输入框(十进制格式)
    private Button charWriteBtn;                // 写数据按钮

    private AlertDialog.Builder builder = null;
    private AlertDialog dialog = null;                 // Notify数据弹框

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_characteristic_operation, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {

        // Receive 区域, 包括数据格式选择、Notify/Indicate 使能、清屏、读取
        charReceivableLayout = (RelativeLayout) view.findViewById(R.id.char_receivable_layout);
        charRecvFmtSelect = (Spinner) view.findViewById(R.id.char_recv_fmt_select);
        charNotifyIndicateEnableBtn = (ToggleButton) view.findViewById(R.id.char_notify_indicate_enable_btn);
        charClearBtn = (Button) view.findViewById(R.id.char_clear_btn);
        charReadBtn = (Button) view.findViewById(R.id.char_read_btn);

        // 聊天信息区域
        msgRecyclerView = (RecyclerView) view.findViewById(R.id.char_msg_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatMsgAdapter(mChatMsgList);
        msgRecyclerView.setAdapter(adapter);

        // Send区域, 包括发送定时、发送按钮
        charSendableLayout = (LinearLayout) view.findViewById(R.id.char_sendable_layout);
        charSendOnTimeCheckbox = (CheckBox) view.findViewById(R.id.char_send_onTime_checkbox);
        charSendOnTimeEdit = (EditText) view.findViewById(R.id.char_send_onTime_et);
        charSendFmtSelect = (Spinner) view.findViewById(R.id.char_send_fmt_select);
        charWriteStringEdit = (EditText) view.findViewById(R.id.char_write_string_et);
        charWriteHexEdit = (EditText) view.findViewById(R.id.char_write_hex_et);
        charWriteDecEdit = (EditText) view.findViewById(R.id.char_write_dec_et);
        charWriteBtn = (Button) view.findViewById(R.id.char_write_btn);

        // 初始化接收数据格式
        fmtAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, FMT_SELECT);
        fmtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        charRecvFmtSelect.setAdapter(fmtAdapter);
        charRecvFmtSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                charRecvFmtInt = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 初始化发送数据格式
        charSendFmtSelect.setAdapter(fmtAdapter);
        charSendFmtSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                charSendFmtInt = i;

                switch (charSendFmtInt) {
                    case DATA_FMT_STR:
                        charWriteStringEdit.setVisibility(View.VISIBLE);
                        charWriteHexEdit.setVisibility(View.GONE);
                        charWriteDecEdit.setVisibility(View.GONE);

                        charWriteStringEdit.setFocusable(true);
                        charWriteStringEdit.setFocusableInTouchMode(true);
                        charWriteStringEdit.requestFocus();
                        break;

                    case DATA_FMT_HEX:
                        charWriteStringEdit.setVisibility(View.GONE);
                        charWriteHexEdit.setVisibility(View.VISIBLE);
                        charWriteDecEdit.setVisibility(View.GONE);

                        charWriteHexEdit.setFocusable(true);
                        charWriteHexEdit.setFocusableInTouchMode(true);
                        charWriteHexEdit.requestFocus();
                        break;

                    case DATA_FMT_DEC:
                        charWriteStringEdit.setVisibility(View.GONE);
                        charWriteHexEdit.setVisibility(View.GONE);
                        charWriteDecEdit.setVisibility(View.VISIBLE);

                        charWriteDecEdit.setFocusable(true);
                        charWriteDecEdit.setFocusableInTouchMode(true);
                        charWriteDecEdit.requestFocus();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void showData() {
        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        final BluetoothGattCharacteristic characteristic = ((OperationActivity) getActivity()).getBluetoothGattCharacteristic();

        // Property
        int charaProp = characteristic.getProperties();
        charaPropNotifyOrIndicateSelect = CHAR_PROPERTY_NO_NOTIFY_OR_INDICATE_SELECTED;
        charaPropReadSelect = CHAR_PROPERTY_NO_READ_SELECTED;

        // 根据Property判断UI上需要显示的功能
        // Write or Write_no_response
        if (((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                || ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0)) {
            charSendableLayout.setVisibility(View.VISIBLE);
        } else {
            charSendableLayout.setVisibility(View.GONE);
        }

        // Read
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            charaPropReadSelect = CHAR_PROPERTY_READ_SELECTED;
        }

        // Notify or Indicate
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            charaPropNotifyOrIndicateSelect = CHAR_PROPERTY_NOTIFY_SELECTED;
        }
        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            charaPropNotifyOrIndicateSelect = CHAR_PROPERTY_INDICATE_SELECTED;
        }
        if ((charaPropReadSelect > 0) || (charaPropNotifyOrIndicateSelect > 0)) {
            // 初始化接收区域
            charReceivableLayout.setVisibility(View.VISIBLE);

            // 初始化Read Button
            if (charaPropReadSelect > 0) {
                charReadBtn.setVisibility(View.VISIBLE);
            } else {
                charReadBtn.setVisibility(View.GONE);
            }

            // 初始化Notify or Indicate Enable/Disable ToggleButton
            switch (charaPropNotifyOrIndicateSelect) {
                case CHAR_PROPERTY_NO_NOTIFY_OR_INDICATE_SELECTED:
                    charNotifyIndicateEnableBtn.setVisibility(View.GONE);
                    break;
                case CHAR_PROPERTY_NOTIFY_SELECTED:
                    charNotifyIndicateEnableBtn.setVisibility(View.VISIBLE);
                    charNotifyIndicateEnableBtn.setText(getActivity().getString(R.string.notify));
                    break;
                case CHAR_PROPERTY_INDICATE_SELECTED:
                    charNotifyIndicateEnableBtn.setVisibility(View.VISIBLE);
                    charNotifyIndicateEnableBtn.setText(getActivity().getString(R.string.indicate));
                    break;
                default:
                    break;
            }
        } else {
            // 初始化接收区域
            charReceivableLayout.setVisibility(View.GONE);
        }

        // 初始化时清屏
        charDisplayClear();

        // 发送(Write)数据 Button
        charWriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final byte[] sendMsg = getSendData();
                if (sendMsg == null) {
                    return;
                }

                BleManager.getInstance().write(
                        bleDevice,
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        sendMsg,
                        new BleWriteCallback() {
                            @Override
                            public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        charDisplaySendData(sendMsg);
                                    }
                                });
                            }

                            @Override
                            public void onWriteFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }
                        }
                );
            }
        });

        // 读取(Read)数据 Button
        charReadBtn.setOnClickListener(new View.OnClickListener() {
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
                                        charDisplayRecvData(data, false, characteristic.getUuid().toString());
                                    }
                                });
                            }

                            @Override
                            public void onReadFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }
                        });
            }
        });

        // 通知(Notify) or 指示(Indicate) ToggleButton
        charNotifyIndicateEnableBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (charaPropNotifyOrIndicateSelect == CHAR_PROPERTY_NOTIFY_SELECTED) {
                        // Notify
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
                                            }
                                        });
                                    }

                                    @Override
                                    public void onNotifyFailure(final BleException exception) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCharacteristicChanged(byte[] data) {
                                        charDisplayRecvData(data, true, characteristic.getUuid().toString());
                                    }
                                }
                        );
                    } else if (charaPropNotifyOrIndicateSelect == CHAR_PROPERTY_INDICATE_SELECTED){
                        // Indicate
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
                                            }
                                        });
                                    }

                                    @Override
                                    public void onIndicateFailure(final BleException exception) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCharacteristicChanged(byte[] data) {
                                        charDisplayRecvData(data, true, characteristic.getUuid().toString());
                                    }

                                }
                        );
                    }

                } else {
                    if (charaPropNotifyOrIndicateSelect == CHAR_PROPERTY_NOTIFY_SELECTED) {
                        // Stop Notify
                        BleManager.getInstance().stopNotify(
                                bleDevice,
                                characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString());
                    } else if (charaPropNotifyOrIndicateSelect == CHAR_PROPERTY_INDICATE_SELECTED) {
                        // Stop Indicate
                        BleManager.getInstance().stopIndicate(
                                bleDevice,
                                characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString());
                    }

                } // end if (isChecked)
            }
        });

        // 清屏
        charClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                charDisplayClear();
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && (getActivity() != null)) {
            getActivity().runOnUiThread(runnable);
        }
    }

    /**
     * RecyclerView中清屏
     */
    private void charDisplayClear() {
        mChatMsgList.clear();
        adapter.notifyDataSetChanged();
        msgRecyclerView.removeAllViews();
    }

    /**
     * 从EditText获取要发送的数据
     * @return
     */
    private byte[] getSendData() {
        String content = "";
        byte[] sendMsgByte = null;

        switch (charSendFmtInt) {
            case DATA_FMT_STR:  // 字符串
                content = charWriteStringEdit.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return null;
                }

                sendMsgByte = content.getBytes();
                break;
            case DATA_FMT_HEX:  // 16进制
                content = charWriteHexEdit.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return null;
                }

                sendMsgByte = HexUtil.hexStringToBytes(content);
                break;
            case DATA_FMT_DEC:  // 10进制
                content = charWriteDecEdit.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return null;
                }

                int dataInteger = Integer.parseInt(content);
                int byteSize;
                for (byteSize = 0; dataInteger != 0; byteSize++) {  // 计算占用字节数
                    dataInteger /= 256;
                }
                sendMsgByte = new byte[byteSize];

                dataInteger = Integer.parseInt(content);
                for (int i = 0; i < byteSize; i++) {
                sendMsgByte[i] = (byte) (0xFF & (dataInteger % 256));
                dataInteger /= 256;
            }
                break;
        }

        return sendMsgByte;
    }

    /**
     * RecyclerView中显示发送的数据
     * @param data  需要显示的数值
     */
    private void charDisplaySendData(final byte[] data) {

        String content = "";

        switch (charSendFmtInt) {
            case DATA_FMT_STR:  // 字符串
                content = charWriteStringEdit.getText().toString();
                charWriteStringEdit.setText("");  // 清空输入框中的内容
                break;
            case DATA_FMT_HEX:  // 16进制
                content = charWriteHexEdit.getText().toString();
                charWriteHexEdit.setText("");
                break;
            case DATA_FMT_DEC:  // 10进制
                content = charWriteDecEdit.getText().toString();
                charWriteDecEdit.setText("");
                break;
        }

        ChatMsg chatMsg = new ChatMsg(content, ChatMsg.TYPE_SENT);
        mChatMsgList.add(chatMsg);
        int position = mChatMsgList.size() - 1;     // 获取mChatMsgList最后一行的坐标
        adapter.notifyItemInserted(position);       // 当有新消息时, 刷新RecyclerView中的显示
        msgRecyclerView.scrollToPosition(position); // 将RecyclerView定位到最后一行
    }

    /**
     * RecyclerView中显示接收的数据
     * @param data  需要显示的数值
     * @param toastEnable  Toast使能
     */
    private void charDisplayRecvData(byte[] data, boolean toastEnable, String uuid) {
        String tmp = "";

        switch (charRecvFmtInt) {
            case DATA_FMT_STR:  // 字符串
                tmp = new String(data);
                break;
            case DATA_FMT_HEX:  // 16进制
                tmp = HexUtil.formatHexString(data, true);
                break;
            case DATA_FMT_DEC:  // 10进制
                int count = 0;
                for (int i = 0; i < data.length; i++) {
                    count *= 256;
                    count += (data[data.length - 1 - i] & 0xFF);
                }
                tmp = Integer.toString(count);
                break;
        }

        ChatMsg chatMsg = new ChatMsg(tmp, ChatMsg.TYPE_RECEIVED);
        mChatMsgList.add(chatMsg);
        int position = mChatMsgList.size() - 1;     // 获取mChatMsgList最后一行的坐标
        adapter.notifyItemInserted(position);       // 当有新消息时, 刷新RecyclerView中的显示
        msgRecyclerView.scrollToPosition(position); // 将RecyclerView定位到最后一行

        // Toast 显示接收到的数值以及接收数值的UUID
        if (toastEnable) {
            Utils.showToast(getActivity(), uuid + ":\n" + tmp);
        }
    }

}
