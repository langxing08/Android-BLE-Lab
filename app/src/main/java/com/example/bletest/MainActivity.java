package com.example.bletest;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
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
import java.util.UUID;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private static final int BLE_SCAN_STATUS = 1;
    private static final int BLE_STOP_SCAN_STATUS = 0;
    private int bleScanStatus = BLE_STOP_SCAN_STATUS;   // BLE扫描状态标志位

    private DrawerLayout mDrawerLayout;
    private NavigationView navView;         // 侧滑菜单

    private LinearLayout settingLayout;     // BLE扫描和连接设置Layout
    private EditText setNameEdit, setMacEdit, setUuidEdit;  // BLE扫描规则:包括Name、MAC、UUID 等3个Edit
    private Switch setAutoConnectSw;        // BLE连接规则:自动重连使能开关

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
            case android.R.id.home:  // 侧滑菜单
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.device_drawer_layout);

        // BLE扫描和连接Layout
        settingLayout = (LinearLayout) findViewById(R.id.ble_setting_layout);
        settingLayout.setVisibility(View.GONE);
        setNameEdit = (EditText) findViewById(R.id.set_ble_scan_name_et);
        setMacEdit = (EditText) findViewById(R.id.set_ble_scan_mac_et);
        setUuidEdit = (EditText) findViewById(R.id.set_ble_scan_uuid_et);
        setAutoConnectSw = (Switch) findViewById(R.id.set_ble_auto_reconnect_sw);

        // 下拉刷新
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.device_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新实现BLE扫描功能
                bleSetScanRule();
                bleStartScan();
            }
        });

        // 侧滑菜单
        navView = (NavigationView) findViewById(R.id.nav_view);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);  // 显示导航按钮
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);  // 设置导航按钮图标
        }
        navView.setCheckedItem(R.id.nav_setting);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // 侧滑菜单的菜单项选择事件处理
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                switch (menuItem.getItemId()) {
                    case R.id.nav_setting:
                        Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                        settingLayout.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                        break;
                    case R.id.nav_device_info:
                        Toast.makeText(MainActivity.this, "Device Information", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_about:
                        // 弹出AlertDialog
                        dialog.setTitle(R.string.about);
                        dialog.setIcon(R.mipmap.icon);
                        dialog.setMessage(R.string.about_details);
                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        dialog.show();

                        break;
                    case R.id.nav_denote:
                        Toast.makeText(MainActivity.this, "Denote", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_feedback:
                        // 弹出AlertDialog
                        dialog.setTitle(R.string.feedback);
                        dialog.setIcon(R.mipmap.icon);
                        dialog.setMessage(R.string.feedback_details);
                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        dialog.show();
                        break;
                    default:
                        break;
                }

                mDrawerLayout.closeDrawers();
                return true;
            }
        });


        progressDialog = new ProgressDialog(this);



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
            Toast.makeText(this, "You should open Bluetooth", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "App has obtained the required permissions", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "You should agree all the needed permissions if you want to use this App.",
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
        Toast.makeText(this, "Success obtain permissions: " + perms, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Fail obtain permissions:" + perms, Toast.LENGTH_SHORT).show();

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }


    /**
     * 设置BLE扫描规则
     */
    private void bleSetScanRule() {
        // UUID
        String[] uuids;
        String str_uuid = setUuidEdit.getText().toString();
        if (TextUtils.isEmpty(str_uuid)) {
            uuids = null;
        } else {
            uuids = str_uuid.split(",");
        }
        UUID[] serviceUuids = null;
        if (uuids != null && uuids.length > 0) {
            serviceUuids = new UUID[uuids.length];
            for (int i = 0; i < uuids.length; i++) {
                String name = uuids[i];
                String[] components = name.split("-");
                if (components.length != 5) {
                    serviceUuids[i] = null;
                } else {
                    serviceUuids[i] = UUID.fromString(uuids[i]);
                }
            }
        }

        // Name
        String[] names;
        String str_name = setNameEdit.getText().toString();
        if (TextUtils.isEmpty(str_name)) {
            names = null;
        } else {
            names = str_name.split(",");
        }

        // MAC
        String mac = setMacEdit.getText().toString();

        // AutoConnect
        boolean isAutoConnect = setAutoConnectSw.isChecked();

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备,可选
                .setDeviceName(true, names)    // 只扫描指定广播名的设备,可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备,可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数,可选,默认false
                .setScanTimeOut(10000)              // 扫描超时时间,可选,默认10秒
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

                bleDeviceList.clear();

                mDeviceAdapter.clear();
                mDeviceAdapter.notifyDataSetChanged();

                recyclerView.removeAllViews();

                settingLayout.setVisibility(View.GONE);

                swipeRefreshLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(true);

                scanMenuItem.setTitle(getString(R.string.stop_scan));

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
                progressDialog.setMessage("Connecting");  // 设置BLE连接ProgressDialog提示文本
                progressDialog.show();  // 显示BLE连接ProgressDialog
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                scanMenuItem.setTitle(getString(R.string.start_scan));

                progressDialog.dismiss();  // 隐藏BLE连接ProgressDialog

                bleScanStatus = BLE_SCAN_STATUS;

                Toast.makeText(MainActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();  // 隐藏BLE连接ProgressDialog

                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                Intent intent = new Intent(MainActivity.this, OperationActivity.class);
                intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                startActivity(intent);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();  // 隐藏BLE连接ProgressDialog

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, "You disconnect BLE", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "BLE was disconnected", Toast.LENGTH_SHORT).show();

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
