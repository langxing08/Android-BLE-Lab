package com.example.bletest.operation;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicOperationFragment extends Fragment {

    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;

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
    private int charReadFmtInt;     // 接收数据格式
    private int charWriteFmtInt;    // 发送数据格式

    private Spinner charReadFmtSelect;          // 读数据格式选择
    private Button charNotifyIndicateEnableBtn; // 通知/指示 使能按钮
    private Button charClearBtn;                // 清屏按钮
    private Button charReadBtn;                 // 读数据按钮

    private LinearLayout charWritableLayout;    // 写区域Layout
    private CheckBox sendOnTimeCheckbox;        // 定时发送复选框
    private EditText sendOnTimeEdit;            // 定时发送时间

    private Spinner charWriteFmtSelect;         // 写数据格式选择
    private EditText charWriteStringEdit;       // 写数据输入框(UTF-8格式)
    private EditText charWriteHexEdit;          // 写数据输入框(十六进制格式)
    private EditText charWriteDecEdit;          // 写数据输入框(十进制格式)
    private Button charWriteBtn;                // 写数据按钮

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_characteristic_operation, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {



        // Read 区域, 包括数据格式选择、Notify/Indicate 使能、清屏、读取
        charReadFmtSelect = (Spinner) view.findViewById(R.id.char_read_fmt_select);
        charNotifyIndicateEnableBtn = (Button) view.findViewById(R.id.char_notify_indicate_enable_btn);
        charClearBtn = (Button) view.findViewById(R.id.char_clear_btn);
        charReadBtn = (Button) view.findViewById(R.id.char_read_btn);

        // 聊天信息区域
        msgRecyclerView = (RecyclerView) view.findViewById(R.id.char_msg_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatMsgAdapter(mChatMsgList);
        msgRecyclerView.setAdapter(adapter);

        // Write区域, 包括发送定时、发送按钮
        charWritableLayout = (LinearLayout) view.findViewById(R.id.char_writable_layout);
        sendOnTimeCheckbox = (CheckBox) view.findViewById(R.id.send_onTime_checkbox);
        sendOnTimeEdit = (EditText) view.findViewById(R.id.send_onTime_et);
        charWriteFmtSelect = (Spinner) view.findViewById(R.id.char_write_fmt_select);
        charWriteStringEdit = (EditText) view.findViewById(R.id.char_write_string_et);
        charWriteHexEdit = (EditText) view.findViewById(R.id.char_write_hex_et);
        charWriteDecEdit = (EditText) view.findViewById(R.id.char_write_dec_et);
        charWriteBtn = (Button) view.findViewById(R.id.char_write_btn);

        // 初始化接收数据格式
        fmtAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, FMT_SELECT);
        fmtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        charReadFmtSelect.setAdapter(fmtAdapter);
        charReadFmtSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                charReadFmtInt = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 初始化发送数据格式
        charWriteFmtSelect.setAdapter(fmtAdapter);
        charWriteFmtSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                charWriteFmtInt = i;

                switch (charWriteFmtInt) {
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
        final int charaProp = ((OperationActivity) getActivity()).getCharaProp();
        final String child = characteristic.getUuid().toString() + String.valueOf(charaProp);

        // 发送(Write)数据
        charWriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String hex = charWriteStringEdit.getText().toString();
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
                                        charDisplaySendData(hex);
                                        charWriteStringEdit.setText("");    // 清空输入框中的内容
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

        // 读取(Read)数据
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
//                                        charDisplayRecvData(new String(data));    // UTF-8
                                        charDisplayRecvData(HexUtil.formatHexString(data, true));   // HEX
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

        // 清屏
        charClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChatMsgList.clear();
                adapter.notifyDataSetChanged();
                msgRecyclerView.removeAllViews();
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && (getActivity() != null)) {
            getActivity().runOnUiThread(runnable);
        }
    }


    /**
     * RecyclerView中显示发送的数据
     * @param content
     */
    private void charDisplaySendData(String content) {
        ChatMsg chatMsg = new ChatMsg(content, ChatMsg.TYPE_SENT);
        mChatMsgList.add(chatMsg);
        int position = mChatMsgList.size() - 1;     // 获取mChatMsgList最后一行的坐标
        adapter.notifyItemInserted(position);       // 当有新消息时, 刷新RecyclerView中的显示
        msgRecyclerView.scrollToPosition(position); // 将RecyclerView定位到最后一行
    }

    /**
     * RecyclerView中显示接收的数据
     * @param content
     */
    private void charDisplayRecvData(String content) {
        ChatMsg chatMsg = new ChatMsg(content, ChatMsg.TYPE_RECEIVED);
        mChatMsgList.add(chatMsg);
        int position = mChatMsgList.size() - 1;     // 获取mChatMsgList最后一行的坐标
        adapter.notifyItemInserted(position);       // 当有新消息时, 刷新RecyclerView中的显示
        msgRecyclerView.scrollToPosition(position); // 将RecyclerView定位到最后一行
    }

}
