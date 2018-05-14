package site.chengxg.h5plugin;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.dcloud.common.DHInterface.IWebview;
import io.dcloud.common.DHInterface.StandardFeature;
import io.dcloud.common.util.JSUtil;

import static io.dcloud.common.util.ReflectUtils.getApplicationContext;

public class BluetoothPlugin extends StandardFeature {
    private BluetoothManager btManager = BluetoothManager.getInstance();

    public BluetoothPlugin() {
        btManager = BluetoothManager.getInstance();
    }

    public void onStart(Context pContext, Bundle pSavedInstanceState, String[] pRuntimeArgs) {

    }

    /**
     * 打开蓝牙
     *
     * @param pWebview
     * @param array
     */
    public void turnOnBluetooth(final IWebview pWebview, final JSONArray array) {
        String CallBackID = array.optString(0);
        JSONArray paramsArray = new JSONArray();
        try {
            if (btManager.btAdapter == null) {
                JSUtil.execCallback(pWebview, CallBackID, "没有蓝牙", JSUtil.ERROR, false);
                return;
            }
            if (!btManager.btAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (btManager.activity == null) {
                    JSUtil.execCallback(pWebview, CallBackID, "未获取到activity", JSUtil.ERROR, false);
                    return;
                } else {
                    btManager.activity.startActivityForResult(intent, btManager.REQUEST_ENABLE_BT);
                    JSUtil.execCallback(pWebview, CallBackID, "请求打开蓝牙", JSUtil.ERROR, false);
                    return;
                }
            } else {
                JSUtil.execCallback(pWebview, CallBackID, "蓝牙已经打开!", JSUtil.OK, false);
            }
        } catch (Exception e) {
            JSUtil.execCallback(pWebview, CallBackID, "打开蓝牙失败!" + e.getMessage(), JSUtil.ERROR, false);
        }
    }

    /**
     * 关闭蓝牙
     *
     * @param pWebview
     * @param array
     */
    public void turnOffBluetooth(final IWebview pWebview, final JSONArray array) {
        String CallBackID = array.optString(0);

        if (btManager.btFindReceiver != null) {
            try {
                btManager.activity.unregisterReceiver(btManager.btFindReceiver);
            } catch (Exception e) {

            }
            btManager.btFindReceiver = null;
        }
        btManager.cancelDiscovery();
        btManager.readThreadState = false;
        btManager.closeBtSocket();

        if (btManager.btAdapter != null && btManager.btAdapter.isEnabled()) {
            btManager.btAdapter.disable();
            JSUtil.execCallback(pWebview, CallBackID, "蓝牙关闭成功!", JSUtil.OK, false);
        } else {
            JSUtil.execCallback(pWebview, CallBackID, "蓝牙已经关闭!", JSUtil.OK, false);
        }
        btManager.connDeviceThread = null;
    }

    public void listenBluetoothStatus(final IWebview pWebview, final JSONArray array) {
        final String CallBackID = array.optString(0);
        if (btManager.btStatusReceiver != null) {
            try {
                btManager.activity.unregisterReceiver(btManager.btStatusReceiver);
            } catch (Exception e) {

            }
            btManager.btStatusReceiver = null;
        }
        btManager.btStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String retState = "";
                switch (Objects.requireNonNull(intent.getAction())) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                        switch (blueState) {
                            case BluetoothAdapter.STATE_TURNING_ON:
                                retState = "STATE_TURNING_ON";
                                break;
                            case BluetoothAdapter.STATE_ON:
                                retState = "STATE_ON";
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                retState = "STATE_TURNING_OFF";
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                retState = "STATE_OFF";
                                break;
                        }
                        break;
                }
                JSUtil.execCallback(pWebview, CallBackID, retState, JSUtil.OK, true);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        btManager.activity.registerReceiver(btManager.btStatusReceiver, filter);
    }

    /**
     * 得到已经配对的设备
     *
     * @param pWebview
     * @param array
     */
    public void getPairedDevices(final IWebview pWebview, final JSONArray array) {
        Set<BluetoothDevice> connetedDevicesSet = btManager.btAdapter.getBondedDevices();
        JSONArray connetedDeviceList = new JSONArray();
        for (BluetoothDevice device : connetedDevicesSet) {
            Map<String, String> map = new HashMap<>();
            map.put("name", device.getName());
            map.put("address", device.getAddress());

            JSONObject obj = new JSONObject(map);
            connetedDeviceList.put(obj);
        }
        String CallBackID = array.optString(0);
        JSUtil.execCallback(pWebview, CallBackID, connetedDeviceList, JSUtil.OK, false);
    }

    /**
     * 发现新的设备
     *
     * @param pWebview
     * @param array
     */
    public void findingNewDevices(final IWebview pWebview, final JSONArray array) {
        final String CallBackID = array.optString(0);

        if (btManager.btFindReceiver != null) {
            try {
                btManager.activity.unregisterReceiver(btManager.btFindReceiver);
            } catch (Exception e) {

            }
            btManager.btFindReceiver = null;
            btManager.cancelDiscovery();
        }
        //6.0以后的如果需要利用本机查找周围的wifi和蓝牙设备, 申请权限
        if (Build.VERSION.SDK_INT >= 6.0) {
            if (ContextCompat.checkSelfPermission(btManager.activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(btManager.activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        2);
                if (ActivityCompat.shouldShowRequestPermissionRationale(btManager.activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {

                }
                JSUtil.execCallback(pWebview, CallBackID, "请授予相关权限", JSUtil.ERROR, false);
                return;
            }
        }
        btManager.btFindReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {// 找到设备
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    JSONObject map = new JSONObject();
                    try {
                        map.put("name", device.getName());
                        map.put("address", device.getAddress());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSUtil.execCallback(pWebview, CallBackID, map, JSUtil.OK, true);
                }
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { // 搜索完成
                    btManager.cancelDiscovery();
                    JSUtil.execCallback(pWebview, CallBackID, "ACTION_DISCOVERY_FINISHED", JSUtil.OK, true);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        btManager.activity.registerReceiver(btManager.btFindReceiver, filter);
        btManager.btAdapter.startDiscovery(); //开启搜索
    }

    /**
     * 连接到设备
     *
     * @param pWebview
     * @param array
     */
    public void connDevice(final IWebview pWebview, final JSONArray array) {
        final String CallBackID = array.optString(0);
        final String address = array.optString(1);
        if (address == null || address.isEmpty()) {
            JSUtil.execCallback(pWebview, CallBackID, "未获取到蓝牙连接地址", JSUtil.ERROR, false);
            return;
        }
        btManager.cancelDiscovery();
        if (btManager.btSocket != null) {
            btManager.closeBtSocket();
            btManager.btSocket = null;
        }

        if (btManager.connDeviceThread != null) {
            btManager.connDeviceThread.interrupt();
            btManager.connDeviceThread = null;
            btManager.readThreadState = false;
        }

        btManager.connDeviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothDevice device;
                try {
                    device = btManager.btAdapter.getRemoteDevice(address);
                    btManager.btSocket = device.createRfcommSocketToServiceRecord(btManager.MY_UUID);
                } catch (Exception e) {
                    JSUtil.execCallback(pWebview, CallBackID, "创建socket失败", JSUtil.ERROR, false);
                    return;
                }
                try {
                    btManager.btSocket.connect();
                    btManager.btOutStream = btManager.btSocket.getOutputStream();
                    JSUtil.execCallback(pWebview, CallBackID, "连接成功", JSUtil.OK, false);
                } catch (Exception e) {
                    btManager.closeBtSocket();
                    JSUtil.execCallback(pWebview, CallBackID, "连接失败", JSUtil.ERROR, false);
                }
                btManager.connDeviceThread = null;
            }
        });
        btManager.connDeviceThread.start();
    }

    /**
     * 断开连接到设备
     *
     * @param pWebview
     * @param array
     */
    public void disConnDevice(final IWebview pWebview, final JSONArray array) {
        final String CallBackID = array.optString(0);

        if (btManager.btSocket != null) {
            btManager.closeBtSocket();
            btManager.btSocket = null;
        }
        if (btManager.connDeviceThread != null) {
            btManager.connDeviceThread.interrupt();
            btManager.connDeviceThread = null;
            btManager.readThreadState = false;
        }
        JSUtil.execCallback(pWebview, CallBackID, "断开连接成功", JSUtil.OK, false);
    }

    /**
     * 发送数据
     *
     * @param pWebview
     * @param array
     */
    public void sendData(final IWebview pWebview, final JSONArray array) {
        final String CallBackID = array.optString(0);
        byte[] buffer = null;
        try {
            String dataStr = array.optString(1);
            buffer = dataStr.getBytes();
        } catch (Exception e) {
            JSUtil.execCallback(pWebview, CallBackID, "参数解析失败", JSUtil.ERROR, false);
            return;
        }

        if (btManager.btSocket != null) {
            try {
                btManager.btOutStream.write(buffer);
                JSUtil.execCallback(pWebview, CallBackID, "发送成功", JSUtil.OK, false);
            } catch (IOException e) {
                JSUtil.execCallback(pWebview, CallBackID, "发送失败", JSUtil.ERROR, false);
            }
        }
        JSUtil.execCallback(pWebview, CallBackID, "未获取到socket", JSUtil.ERROR, false);
    }

    /**
     * 读取数据
     *
     * @param pWebview
     * @param array
     */
    public void readData(final IWebview pWebview, final JSONArray array) {
        final String CallBackID = array.optString(0);
        final int bufferSize = array.optInt(1);

        try {
            if (btManager.readDataThread != null) {
                btManager.readDataThread.interrupt();
            }
        } catch (Exception ignored) {

        }
        if (btManager.btSocket == null) {
            JSUtil.execCallback(pWebview, CallBackID, "还未连接到蓝牙设备!", JSUtil.ERROR, false);
            return;
        }
        btManager.readDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    btManager.btInStream = btManager.btSocket.getInputStream();
                } catch (IOException e) {
                    JSUtil.execCallback(pWebview, CallBackID, "获取输入流失败", JSUtil.ERROR, false);
                }
                btManager.readThreadState = true;
                long t = System.currentTimeMillis();
                while (btManager.readThreadState) {
                    try {
                        long ct = System.currentTimeMillis();
                        //心跳检测
                        if(ct-t > 1000){
                            btManager.btOutStream.write(0x00);
                            t = ct;
                        }

                        if (btManager.btInStream.available() != 0) {
                            Thread.sleep(10);

                            byte[] buffer = new byte[bufferSize];
                            int len = btManager.btInStream.read(buffer);

                            String dataStr = new String(buffer);
                            dataStr = dataStr.replace("\r","");

                            JSUtil.execCallback(pWebview, CallBackID, dataStr, JSUtil.OK, true);
                        }
                    } catch (Exception ignored) {
                        btManager.readThreadState = false;
                        JSUtil.execCallback(pWebview, CallBackID, "读取数据失败", JSUtil.ERROR, false);
                    }
                }
            }
        });
        btManager.readDataThread.start();
    }

}
