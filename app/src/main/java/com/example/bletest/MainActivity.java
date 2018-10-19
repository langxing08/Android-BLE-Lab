package com.example.bletest;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private static final int BLE_SCAN_STATUS = 1;
    private static final int BLE_STOP_SCAN_STATUS = 0;
    private int bleScanStatus = BLE_STOP_SCAN_STATUS;   // BLE扫描状态标志位

    private MenuItem scanMenuItem;          // 工具栏中的菜单

    public DeviceAdapter mDeviceAdapter;    // 设备适配器

    private ProgressDialog progressDialog;  // 设备连接进度条

    private SwipeRefreshLayout swipeRefreshLayout;  // 下拉刷新Layout, 用于下拉扫描BLE设备
    private RecyclerView recyclerView;      // RecyclerView, 用于显示扫描到的BLE设备

    private List<BleDevice> bleDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_toolbar, menu);
        scanMenuItem = menu.findItem(R.id.action_scan_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan_item:
                if (bleScanStatus == BLE_STOP_SCAN_STATUS) { // 开始扫描
                    bleSetScanRule();
                    bleStartScan();
                } else if (bleScanStatus == BLE_SCAN_STATUS) { // 停止扫描
                    BleManager.getInstance().cancelScan();
                }
                break;
            case R.id.action_show_log_item:
                Toast.makeText(this, "show log", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * View初始化
     */
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.device_toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.device_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bleSetScanRule();
                bleStartScan();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.device_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mDeviceAdapter = new DeviceAdapter(bleDeviceList);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    bleConnect(bleDevice);
                }

                Log.d(TAG, "onConnect: " + bleDevice.getName());
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                // 解析广播包

                Log.d(TAG, "onDetail: " + bleDevice.getName());
            }
        });
        recyclerView.setAdapter(mDeviceAdapter);
    }

    /**
     * 显示可连接的BLE设备
     */
    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }

    /**
     * 检查App运行所需的权限是否已经获取
     */
    private void checkPermissions() {

        // 检查BLE是否打开
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "请打开手机蓝牙", Toast.LENGTH_SHORT).show();
        }

        // 所要申请的权限
        String[] perms = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        // 检查是否已获取该权限
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "已获取所需权限", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "必须同意所有的权限才能使用本程序",
                    0, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "获取成功的权限" + perms, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "获取失败的权限" + perms, Toast.LENGTH_SHORT).show();

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }


    /**
     * 设置BLE扫描规则
     */
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

    /**
     * 开始BLE扫描
     */
    private void bleStartScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();

                scanMenuItem.setTitle(getString(R.string.stop_scan));

                swipeRefreshLayout.setRefreshing(true);

                bleScanStatus = BLE_SCAN_STATUS;
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
                scanMenuItem.setTitle(getString(R.string.start_scan));

                swipeRefreshLayout.setRefreshing(false);

                bleScanStatus = BLE_STOP_SCAN_STATUS;
            }
        });
    }

    /**
     * 连接BLE设备
     * @param bleDevice
     */
    public void bleConnect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                scanMenuItem.setTitle(getString(R.string.start_scan));

                progressDialog.dismiss();

                bleScanStatus = BLE_SCAN_STATUS;

                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                Intent intent = new Intent(MainActivity.this, OperationActivity.class);
                intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                startActivity(intent);
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

    /**
     * 检查手机GPS是否打开
     * @return
     */
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
