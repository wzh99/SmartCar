package com.example.mounted;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 蓝牙服务类，包括蓝牙连接监听线程、连接线程、已连接线程
 *
 */
public class BluetoothService {

    private static final String TAG = "Service";
    private static final boolean DEBUG = true;

    // 蓝牙端口名
    private static final String BT_NAME = "HC06";

    // 获取设备UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int BtState;
    //蓝牙状态常量
    private static final int IDLE = 0;       // 闲置
    private static final int LISTENING = 1;  // 监听
    private static final int CONNECTING = 2; // 正在连接
    private static final int CONNECTED = 3;  // 已连接

    /**
     * @param handler  在线程与UI间通讯
     */
    public BluetoothService(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        BtState = IDLE;
        mHandler = handler;
    }

    /**
     * 设置当前蓝牙状态
     * @param state  当前蓝牙状态
     */
    private synchronized void setState(int state) {
        BtState = state;
    }

    /**
     * 启动本地蓝牙接收监听
     */
    private synchronized void acceptWait() {
        if (DEBUG) Log.e(TAG, "进入acceptWait");
        // 开启外主蓝牙接收监听线程
        if (mAcceptThread == null&&mConnectedThread==null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(LISTENING);
    }

    /**
     * 开启连接线程方法
     * @param device  欲连接的设备
     */
    public synchronized void connect(BluetoothDevice device) {
        if (DEBUG) Log.e(TAG, "正在连接" + device);

        //关闭所有可能的蓝牙服务线程以便开启连接线程
        cancelAllBtThread();
        // 开启连接线程
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(CONNECTING);
    }

    /**
     * 开启已连接线程的方法
     * @param socket  已建立连接的蓝牙端口
     * @param device  已连接的蓝牙设备
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (DEBUG) Log.e(TAG, "connected");

        //关闭所有可能的蓝牙服务线程以便开启已连接线程
        cancelAllBtThread();

        // 开启已连接线程
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        //发送已连接设备名回UI
        sendString2UI(MainActivity.CONNECTED_DEVICE_NAME,
                MainActivity.DEVICE_NAME,device.getName());
        setState(CONNECTED);
        write("P");
    }

    /**
     * 关闭所有蓝牙服务线程
     */
    public synchronized void cancelAllBtThread() {
        if (DEBUG) Log.e(TAG, "cancelAllBtThread方法");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(IDLE);
    }

    /**
     * 写输出数据
     * @param msg 输出字节流
     * @see ConnectedThread#write(byte[])
     */
    public void write(String msg) {
        //                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              object
        ConnectedThread r;
        // 同步
        synchronized (this) {
            if (BtState != CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(msg.getBytes());
        Log.d(this.getClass().getName(), msg);
    }

    /**
     * 连接失败处理方法
     */
    private void connectionFailed() {
        setState(LISTENING);
        mConnectedThread=null;
        BluetoothService.this.acceptWait();
        //向UI发送连接失败通知
        sendString2UI(MainActivity.CONNECTION_TOAST,MainActivity.TOAST,"连接失败");
    }
    /**
     * 发送字符串会UI
     * @param what 什么类型
     * @param key  关键字
     * @param str  字符串
     */
    private void sendString2UI(int what,String key,String str){
        Message msg = mHandler.obtainMessage(what);
        Bundle bundle = new Bundle();
        bundle.putString(key, str);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * 监听外部主蓝牙设备线程
     */
    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mBtServSocket;

        AcceptThread() {
            BluetoothServerSocket bss = null;
            // 获取蓝牙监听端口
            try {
                bss = mAdapter.listenUsingRfcommWithServiceRecord(BT_NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mBtServSocket = bss;
        }

        public void run() {
            if (DEBUG) Log.e(TAG, "Begin mAcceptThread");
            setName("AcceptThread");
            BluetoothSocket socket;
            // 监听端口直到连接上
            while (BtState != CONNECTED) {
                try {
                    //成功连接时退出循环
                    socket = mBtServSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }
                // 成功接收主设备
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (BtState) {
                            case LISTENING:
                            case CONNECTING:
                                // 启动已连接线程
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case IDLE:
                            case CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (DEBUG) Log.e(TAG, "End mAcceptThread");
        }

        void cancel() {
            if (DEBUG) Log.e(TAG, "cancel " + this);
            try {
                mBtServSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }
    /**
     * 连接蓝牙设备的线程
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mBtSocket;
        private final BluetoothDevice mBtDevice;

        ConnectThread(BluetoothDevice device) {
            mBtDevice = device;
            BluetoothSocket bs = null;

            // 根据UUID获取欲连接设备
            try {
                bs = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mBtSocket = bs;
        }

        public void run() {
            if (DEBUG) Log.e(TAG, "Begin mConnectThread");
            setName("ConnectThread");
            // 尝试连接蓝牙端口
            try {
                mBtSocket.connect();
            } catch (IOException e) {
                // 当连接失败或异常
                connectionFailed();
                try {
                    mBtSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "close() fail", e2);
                }
                // 重新开启连接监听线程并退出连接线程
                BluetoothService.this.acceptWait();
                if (DEBUG) Log.d(TAG, "End mConnectThread");
                return;
            }

            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            // 启动已连接线程
            connected(mBtSocket, mBtDevice);
            if (DEBUG) Log.d(TAG, "End mConnectThread");
        }

        void cancel() {
            if (DEBUG) Log.e(TAG, "cancel " + this);
            try {
                mBtSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() fail", e);
            }
        }
    }

    /**
     * 已连接的相关处理线程
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mBtSocket;
        private final OutputStream mOutputStream;

        ConnectedThread(BluetoothSocket socket) {
            if (DEBUG) Log.d(TAG, "construct ConnectedThread");
            mBtSocket = socket;
            // InputStream is = null;
            OutputStream os = null;

            // 获取输入输出流
            try {
                // is = socket.getInputStream();
                os = socket.getOutputStream();
            } catch (IOException e) {
                Log.i(TAG, "get Stream fail", e);
            }

            mOutputStream = os;
        }

        public void run() {
            if (DEBUG) Log.i(TAG, "Begin mConnectedThread");

            // 监听输入流以备获取数据
            while (true) {
                if (mBtSocket.isConnected()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else break;
            }
            if (DEBUG) Log.i(TAG, "End mConnectedThread");
        }

        /**
         * 写输出流以发送数据
         * @param buffer 欲输出字节流
         */
        void write(byte[] buffer) {
            try {
                mOutputStream.write(buffer);
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        void cancel() {
            if (DEBUG) Log.e(TAG, "cancel " + this);
            try {
                mBtSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() fail", e);
            }
        }
    }
}
