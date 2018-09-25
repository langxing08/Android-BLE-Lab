package com.example.bletest;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.example.bletest.adapter.DeviceAdapter;
import com.example.bletest.comm.ObserverManager;
import com.example.bletest.operation.OperationActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private static final int BLE_SCAN_STATUS = 1;
    private static final int BLE_STOP_SCAN_STATUS = 0;
    private int ble_scan_status = BLE_STOP_SCAN_STATUS;

//    private Button btn_scan;
    private ImageView img_loading;

    private MenuItem menuItem_scan;

    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        // BLE 初始化及配置
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        showConnectedDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }


//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.btn_scan:
//                /*
//                if (btn_scan.getText().equals(getString(R.string.start_scan))) { // 开始扫描
//                    checkPermissions();
//                    bleSetScanRule();
//                    bleStartScan();
//                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) { // 停止扫描
//                    BleManager.getInstance().cancelScan();
//                }*/
//
//                if (ble_scan_status == BLE_STOP_SCAN_STATUS) { // 开始扫描
//                    checkPermissions();
//                    bleSetScanRule();
//                    bleStartScan();
//                } else if (ble_scan_status == BLE_SCAN_STATUS) { // 停止扫描
//                    BleManager.getInstance().cancelScan();
//                }
//                break;
//            default:
//                break;
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_toolbar, menu);
        menuItem_scan = menu.findItem(R.id.action_scan);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:

                Toast.makeText(this, menuItem_scan.getTitle(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onOptionsItemSelected: " + menuItem_scan.getTitle());

                /*
                if (btn_scan.getText().equals(getString(R.string.start_scan))) { // 开始扫描
                    checkPermissions();
                    bleSetScanRule();
                    bleStartScan();
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) { // 停止扫描
                    BleManager.getInstance().cancelScan();
                }*/
                if (ble_scan_status == BLE_STOP_SCAN_STATUS) { // 开始扫描
                    checkPermissions();
                    bleSetScanRule();
                    bleStartScan();
                } else if (ble_scan_status == BLE_SCAN_STATUS) { // 停止扫描
                    BleManager.getInstance().cancelScan();
                }
                break;
            case R.id.action_show_log:
                Toast.makeText(this, "show log", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_device);
        setSupportActionBar(toolbar);

//        btn_scan = (Button) findViewById(R.id.btn_scan);
//        btn_scan.setText(getString(R.string.start_scan));
//        btn_scan.setOnClickListener(this);

        img_loading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    bleConnect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    Intent intent = new Intent(MainActivity.this, OperationActivity.class);
                    intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                    startActivity(intent);
                }
            }
        });
        ListView listViewDevice = (ListView) findViewById(R.id.list_ble_device);
        listViewDevice.setAdapter(mDeviceAdapter);
    }

    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "请打开手机蓝牙", Toast.LENGTH_SHORT).show();
        }

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }/*
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }*/
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
    }

    private void bleSetScanRule() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
             //   .setServiceUuids(null)
             //   .setDeviceName(false, null)
             //   .setDeviceMac(null)
                .setAutoConnect(false)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void bleStartScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();

                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);

                ble_scan_status = BLE_SCAN_STATUS;
//                btn_scan.setText(getString(R.string.stop_scan)); // 停止扫描
                menuItem_scan.setTitle(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);

                ble_scan_status = BLE_STOP_SCAN_STATUS;
//                btn_scan.setText(getString(R.string.start_scan)); // 开始扫描
                menuItem_scan.setTitle(getString(R.string.start_scan));
            }
        });
    }

    private void bleConnect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);

                ble_scan_status = BLE_SCAN_STATUS;
//                btn_scan.setText(getString(R.string.start_scan)); // 开始扫描
                menuItem_scan.setTitle(getString(R.string.start_scan));

                progressDialog.dismiss();

                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, "断开了", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "连接断开", Toast.LENGTH_SHORT).show();

                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }
            }
        });
    }

    private boolean checkGpsIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGpsIsOpen()) {
                bleSetScanRule();
                bleStartScan();
            }
        }
    }

}
