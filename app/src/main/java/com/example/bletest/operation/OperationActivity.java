package com.example.bletest.operation;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.example.bletest.R;
import com.example.bletest.comm.Observer;
import com.example.bletest.comm.ObserverManager;

import java.util.ArrayList;
import java.util.List;

public class OperationActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "OperationActivity";

    private static final int SERVICE_LIST_PAGE = 0;
    private static final int CHAR_LIST_PAGE = 1;
    private static final int CHAR_OPERATION_PAGE = 2;

    public static final String KEY_DATA = "key_data";

    private BleDevice bleDevice;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private int charaProp;

    private android.support.v7.widget.Toolbar toolbar;
    private List<Fragment> fragmentList = new ArrayList<>();
    private int currentPage = 0;
    private String[] titles = new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);

        initData();
        initView();
        initPage();

        ObserverManager.getInstance().addObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 断开BLE连接
        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().disconnect(bleDevice);
        }

        BleManager.getInstance().clearCharacterCallback(bleDevice);

        ObserverManager.getInstance().deleteObserver(this);
    }

    @Override
    public void disConnected(BleDevice device) {
        if ((device != null) && (bleDevice != null) && (device.getKey().equals(bleDevice.getKey()))) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentPage != SERVICE_LIST_PAGE) {
                currentPage--;
                changePage(currentPage);
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(titles[0]);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (currentPage != SERVICE_LIST_PAGE) {
//                    currentPage--;
//                    changePage(currentPage);
//                } else {
//                    finish();
//                }
//            }
//        });
    }

    private void initData() {
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null) {
            finish();
        }

        titles = new String[] {"服务列表","特征值列表","操作控制台"};
    }

    private void initPage() {
        prepareFragment();
        changePage(SERVICE_LIST_PAGE);
    }

    public void changePage(int page) {
        currentPage = page;
        toolbar.setTitle(titles[page]);

        updateFragment(page);

        switch (currentPage) {
            case CHAR_LIST_PAGE:
                ((CharacteristicListFragment) fragmentList.get(CHAR_LIST_PAGE)).showData();
                break;
            case CHAR_OPERATION_PAGE:
                ((CharacteristicOperationFragment) fragmentList.get(CHAR_OPERATION_PAGE)).showData();
                break;
            default:
                break;
        }
    }

    private void prepareFragment() {
        fragmentList.add(new ServiceListFragment());
        fragmentList.add(new CharacteristicListFragment());
        fragmentList.add(new CharacteristicOperationFragment());

        for (Fragment fragment : fragmentList) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment, fragment)
                    .hide(fragment)
                    .commit();
        }

    }

    private void updateFragment(int position) {
        if (position > (fragmentList.size() - 1)) {
            return;
        }

        for (int i = 0; i < fragmentList.size(); i++) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = fragmentList.get(i);
            if (i == position) {
                transaction.show(fragment);
            } else {
                transaction.hide(fragment);
            }
            transaction.commit();
        }
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }

    public void setBluetoothGattService(BluetoothGattService bluetoothGattService) {
        this.bluetoothGattService = bluetoothGattService;
    }

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return bluetoothGattCharacteristic;
    }

    public void setBluetoothGattCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
    }

    public int getCharaProp() {
        return charaProp;
    }

    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }

}
