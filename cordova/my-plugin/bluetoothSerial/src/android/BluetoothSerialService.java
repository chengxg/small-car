package com.chengxg.plugin;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothSerialService{

    public final int REQUEST_ENABLE_BT = 0, REQUEST_CONNECT = 1;
    public UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");// 蓝牙连接需要UUID
    public final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();// 获取蓝牙适配器

    public Activity activity;//当前活动的 activity
    public BroadcastReceiver btFindReceiver = null;//蓝牙搜索 Receiver
    public BroadcastReceiver btStatusReceiver = null;//蓝牙状态 Receiver
    public BluetoothSocket btSocket = null;
    public InputStream btInStream = null;
    public OutputStream btOutStream = null;
    public Thread readDataThread;
    public boolean readThreadState = false;
    public Thread connDeviceThread;

    private static BluetoothSerialService instance = new BluetoothSerialService();

    public BluetoothSerialService() {

    }

    public static BluetoothSerialService getInstance() {
        return instance;
    }

    public void init(Activity activity) {
        this.activity = activity;
    }

    /**
     * 是否支持蓝牙
     * @return
     */
    public boolean isSupportBluetooth() {
        return btAdapter != null;
    }

    public void closeBtSocket() {
        try {
            btSocket.close();
        } catch (Exception ignored) {

        } finally {
            btSocket = null;
        }
    }

    public void cancelDiscovery() {
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        if (btFindReceiver != null) {
            activity.unregisterReceiver(btFindReceiver);
            btFindReceiver = null;
        }
    }
}
