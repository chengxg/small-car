package com.chengxg.plugin;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class echoes a string called from JavaScript.
 */
public class BluetoothSerial extends CordovaPlugin{
    private BluetoothSerialService btManager = BluetoothSerialService.getInstance();
    private Activity activity;//当前活动的 activity

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView){
        super.initialize(cordova, webView);
        btManager = BluetoothSerialService.getInstance();
        this.activity = cordova.getActivity();
        btManager.activity = this.activity;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException{
        if(action.equals("coolMethod")){
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        boolean ret = false;
        switch(action) {
            case "getBluetoothStatus":
                getBluetoothStatus(args, callbackContext);
                ret = true;
                break;
            case "turnOnBluetooth":
                turnOnBluetooth(args, callbackContext);
                ret = true;
                break;
            case "turnOffBluetooth":
                turnOffBluetooth(args, callbackContext);
                ret = true;
                break;
            case "listenBluetoothStatus":
                listenBluetoothStatus(args, callbackContext);
                ret = true;
                break;
            case "getPairedDevices":
                getPairedDevices(args, callbackContext);
                ret = true;
                break;
            case "findingNewDevices":
                findingNewDevices(args, callbackContext);
                ret = true;
                break;
            case "connDevice":
                connDevice(args, callbackContext);
                ret = true;
                break;
            case "disConnDevice":
                disConnDevice(args, callbackContext);
                ret = true;
                break;
            case "sendData":
                sendData(args, callbackContext);
                ret = true;
                break;
            case "readData":
                readData(args, callbackContext);
                ret = true;
                break;
        }
        return ret;
    }

    private void coolMethod(String message, CallbackContext callbackContext){
        if(message != null && message.length() > 0){
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    /**
     * 打开蓝牙
     *
     * @param pWebview
     * @param array
     */
    private void getBluetoothStatus(JSONArray args, CallbackContext callbackContext){
        if(btManager.btAdapter != null){
            callbackContext.success(btManager.btAdapter.isEnabled() + "");
        } else {
            callbackContext.error("不支持蓝牙");
        }
    }

    /**
     * 打开蓝牙
     *
     * @param pWebview
     * @param array
     */
    private void turnOnBluetooth(JSONArray args, CallbackContext callbackContext){
        try {
            if(btManager.btAdapter == null){
                callbackContext.success("没有蓝牙");
                return;
            }
            if(! btManager.btAdapter.isEnabled()){
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if(btManager.activity == null){
                    callbackContext.error("未获取到activity");
                    return;
                } else {
                    callbackContext.error("请求打开蓝牙");
                    btManager.activity.startActivityForResult(intent, btManager.REQUEST_ENABLE_BT);
                    return;
                }
            } else {
                callbackContext.success("蓝牙已经打开!");
            }
        } catch (Exception e) {
            callbackContext.error("打开蓝牙失败!" + e.getMessage());
        }
    }

    /**
     * 关闭蓝牙
     *
     * @param pWebview
     * @param array
     */
    private void turnOffBluetooth(JSONArray args, CallbackContext callbackContext){

        if(btManager.btFindReceiver != null){
            try {
                btManager.activity.unregisterReceiver(btManager.btFindReceiver);
            } catch (Exception e) {

            }
            btManager.btFindReceiver = null;
        }
        btManager.cancelDiscovery();
        btManager.readThreadState = false;
        btManager.closeBtSocket();

        if(btManager.btAdapter != null && btManager.btAdapter.isEnabled()){
            btManager.btAdapter.disable();
            callbackContext.success("蓝牙关闭成功!");
        } else {
            callbackContext.success("蓝牙已经关闭!");
        }
        btManager.connDeviceThread = null;
    }

    private void listenBluetoothStatus(JSONArray args, CallbackContext callbackContext){
        if(btManager.btStatusReceiver != null){
            try {
                btManager.activity.unregisterReceiver(btManager.btStatusReceiver);
            } catch (Exception e) {

            }
            btManager.btStatusReceiver = null;
        }

        btManager.btStatusReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                String retState = "";
                switch(Objects.requireNonNull(intent.getAction())) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                        switch(blueState) {
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
                // Success return object
                PluginResult result = new PluginResult(PluginResult.Status.OK, retState);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
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
    private void getPairedDevices(JSONArray args, CallbackContext callbackContext){
        Set<BluetoothDevice> connetedDevicesSet = btManager.btAdapter.getBondedDevices();
        JSONArray connetedDeviceList = new JSONArray();
        for(BluetoothDevice device : connetedDevicesSet){
            Map<String, String> map = new HashMap<>();
            map.put("name", device.getName());
            map.put("address", device.getAddress());

            JSONObject obj = new JSONObject(map);
            connetedDeviceList.put(obj);
        }
        callbackContext.success(connetedDeviceList);
    }

    /**
     * 发现新的设备
     *
     * @param pWebview
     * @param array
     */
    private void findingNewDevices(JSONArray args, CallbackContext callbackContext){
        if(btManager.btFindReceiver != null){
            try {
                btManager.activity.unregisterReceiver(btManager.btFindReceiver);
            } catch (Exception e) {

            }
            btManager.btFindReceiver = null;
            btManager.cancelDiscovery();
        }
        //6.0以后的如果需要利用本机查找周围的wifi和蓝牙设备, 申请权限
        if(Build.VERSION.SDK_INT >= 6.0){
            if(PermissionChecker.checkSelfPermission(btManager.activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //请求权限
                ActivityCompat.requestPermissions(btManager.activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                if(ActivityCompat.shouldShowRequestPermissionRationale(btManager.activity, Manifest.permission.ACCESS_COARSE_LOCATION)){

                }
                callbackContext.success("请授予相关权限");
                return;
            }
        }
        btManager.btFindReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){// 找到设备
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    JSONObject map = new JSONObject();
                    try {
                        map.put("name", device.getName());
                        map.put("address", device.getAddress());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    PluginResult result = new PluginResult(PluginResult.Status.OK, map);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                    //callbackContext.success(map);
                }
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){ // 搜索完成
                    btManager.cancelDiscovery();
                    callbackContext.success("ACTION_DISCOVERY_FINISHED");
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
    private void connDevice(JSONArray args, CallbackContext callbackContext){
        final String address = args.optString(0);
        if(address == null || address.isEmpty()){
            callbackContext.success("未获取到蓝牙连接地址");
            return;
        }
        btManager.cancelDiscovery();
        if(btManager.btSocket != null){
            btManager.closeBtSocket();
            btManager.btSocket = null;
        }

        if(btManager.connDeviceThread != null){
            btManager.connDeviceThread.interrupt();
            btManager.connDeviceThread = null;
            btManager.readThreadState = false;
        }

        btManager.connDeviceThread = new Thread(new Runnable(){
            @Override
            public void run(){
                BluetoothDevice device;
                try {
                    device = btManager.btAdapter.getRemoteDevice(address);
                    btManager.btSocket = device.createRfcommSocketToServiceRecord(btManager.MY_UUID);
                } catch (Exception e) {
                    callbackContext.error("创建socket失败");
                    return;
                }
                try {
                    btManager.btSocket.connect();
                    btManager.btOutStream = btManager.btSocket.getOutputStream();
                    callbackContext.success("连接成功");
                } catch (Exception e) {
                    btManager.closeBtSocket();
                    callbackContext.error("连接失败");
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
    private void disConnDevice(JSONArray args, CallbackContext callbackContext){
        if(btManager.btSocket != null){
            btManager.closeBtSocket();
            btManager.btSocket = null;
        }
        if(btManager.connDeviceThread != null){
            btManager.connDeviceThread.interrupt();
            btManager.connDeviceThread = null;
            btManager.readThreadState = false;
        }
        callbackContext.success("断开连接成功");
    }

    /**
     * 发送数据
     *
     * @param pWebview
     * @param array
     */
    private void sendData(JSONArray args, CallbackContext callbackContext){
        byte[] buffer = null;
        try {
            String dataStr = args.optString(0);
            buffer = dataStr.getBytes();
        } catch (Exception e) {
            callbackContext.error("参数解析失败");
            return;
        }

        if(btManager.btSocket != null){
            try {
                btManager.btOutStream.write(buffer);
                callbackContext.success("发送成功");
            } catch (IOException e) {
                callbackContext.error("发送失败");
            }
        } else {
            callbackContext.error("未获取到socket");
        }
    }

    /**
     * 读取数据
     *
     * @param pWebview
     * @param array
     */
    private void readData(JSONArray args, CallbackContext callbackContext){
        int setBufferSize = args.optInt(0);
        if(setBufferSize == 0){
            setBufferSize = 16;
        }
        final int bufferSize = setBufferSize;
        try {
            if(btManager.readDataThread != null){
                btManager.readDataThread.interrupt();
            }
        } catch (Exception ignored) {

        }
        if(btManager.btSocket == null){
            callbackContext.error("还未连接到蓝牙设备");
            return;
        }
        btManager.readDataThread = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    btManager.btInStream = btManager.btSocket.getInputStream();
                } catch (IOException e) {
                    callbackContext.error("获取输入流失败");
                }
                btManager.readThreadState = true;
                long t = System.currentTimeMillis();
                while(btManager.readThreadState){
                    try {
                        long ct = System.currentTimeMillis();
                        //心跳检测
                        if(ct - t > 1000){
                            btManager.btOutStream.write(0x00);
                            t = ct;
                        }

                        if(btManager.btInStream.available() != 0){
                            Thread.sleep(5);

                            byte[] buffer = new byte[bufferSize];
                            int len = btManager.btInStream.read(buffer);
                            if(len == 0){
                                continue;
                            }
                            byte[] subBuffer = subBytes(buffer,0,len);
                            String dataStr = new String(subBuffer);
                            dataStr = dataStr.replace("\r", "");
                            PluginResult result = new PluginResult(PluginResult.Status.OK, dataStr);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }
                    } catch (Exception ignored) {
                        btManager.readThreadState = false;
                        callbackContext.error("读取数据失败");
                    }
                }
            }
        });
        btManager.readDataThread.start();
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    private int indexOfBytes(byte[] src , byte b){
        int len = src.length;
        for(int i=0;i<len;i++){
            if(src[i] == b){
                return i;
            }
        }
        return -1;
    }

}
