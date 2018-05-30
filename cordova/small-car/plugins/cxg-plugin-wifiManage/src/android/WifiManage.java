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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class echoes a string called from JavaScript.
 */
public class WifiManage extends CordovaPlugin{

    private Socket socket = null;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread readDataThread;
    private boolean readThreadState;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView){
        super.initialize(cordova, webView);

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
            case "connectToTcpServer":
                connectToTcpServer(args, callbackContext);
                ret = true;
                break;
            case "disconnectTcpServer":
                disconnectTcpServer(args, callbackContext);
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

    private void connectToTcpServer(JSONArray args, CallbackContext callbackContext){
        try {
            readThreadState = false;
            if(readDataThread != null){
                readDataThread.interrupt();
            }
        } catch (Exception ignored) {

        }
        socket = null;
        try {
            final String address = args.optString(0);
            final String port = args.optString(1);
            final int portInt = Integer.parseInt(port);
            InetAddress ipAddress = InetAddress.getByName(address);
            socket = new Socket(ipAddress, portInt);
            callbackContext.success("连接成功");
        } catch (Exception e) {
            callbackContext.error("连接失败," + e.getMessage());
        }
    }

    private void disconnectTcpServer(JSONArray args, CallbackContext callbackContext){
        if(socket == null){
            callbackContext.error("您还未连接");
            return;
        }
        try {
            readThreadState = false;
            if(readDataThread != null){
                readDataThread.interrupt();
            }
        } catch (Exception ignored) {

        }
        try {
            socket.close();//关闭连接
            socket = null;
            callbackContext.success("断开连接成功");
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("断开连接失败," + e.getMessage());
        }
    }

    private void sendData(JSONArray args, CallbackContext callbackContext){
        final String dataStr = args.optString(0);
        try {
            //获取输出流
            outputStream = socket.getOutputStream();
            //发送数据
            outputStream.write(dataStr.getBytes());
            callbackContext.success("发送成功");
        } catch (Exception e) {
            callbackContext.error("发送失败," + e.getMessage());
        }
    }

    /**
     * 读取数据
     */
    private void readData(JSONArray args, CallbackContext callbackContext){
        int setBufferSize = args.optInt(0);
        if(setBufferSize == 0){
            setBufferSize = 512;
        }
        final int bufferSize = setBufferSize;
        try {
            if(readDataThread != null){
                readDataThread.interrupt();
            }
        } catch (Exception ignored) {

        }
        if(socket == null){
            callbackContext.error("您还未连接到tcp服务器");
            return;
        }
        readDataThread = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    callbackContext.error("获取输入流失败");
                }
                readThreadState = true;
                long t = System.currentTimeMillis();
                while(readThreadState){
                    try {
                        long ct = System.currentTimeMillis();
                        //心跳检测
                        if(ct - t > 1000){
                            outputStream.write(0x00);
                            t = ct;
                        }

                        if(inputStream.available() != 0){
                            Thread.sleep(5);

                            byte[] buffer = new byte[bufferSize];
                            int len = inputStream.read(buffer);
                            if(len == 0){
                                continue;
                            }
                            byte[] subBuffer = subBytes(buffer, 0, len);
                            String dataStr = new String(subBuffer);
                            dataStr = dataStr.replace("\r", "");
                            PluginResult result = new PluginResult(PluginResult.Status.OK, dataStr);
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }
                    } catch (Exception ignored) {
                        readThreadState = false;
                        callbackContext.error("读取数据失败");
                    }
                }
            }
        });
        readDataThread.start();
    }

    private static byte[] subBytes(byte[] src, int begin, int count){
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    private int indexOfBytes(byte[] src, byte b){
        int len = src.length;
        for(int i = 0; i < len; i++){
            if(src[i] == b){
                return i;
            }
        }
        return - 1;
    }

}
