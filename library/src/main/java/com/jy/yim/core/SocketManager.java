package com.jy.yim.core;

import android.os.Handler;
import android.os.Message;

import com.jy.yim.YIMConfig;
import com.jy.yim.utils.MLogUtils;
import com.jy.yim.utils.SocketDataUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class SocketManager implements IReceive, ISend {

    private YIMConfig yimConfig;

    private boolean isConnected = false;//是否已连接
    private boolean isCloseSocket = false;//是否已关闭socket
    private long freeTime = 0; //链路空闲时间

    public int seqID = 0; //消息序列号

    private Socket socket;
    private InputStream inputStream;//输入流
    private OutputStream outputStream;//输出流
    private ReceiveTask receiveTask;//接收线程
    private HeartPackageTask heartPackageTask;//心跳线程

    public MyHandler handler;

    public static final int CONNECT_SUCCEED = 666;//连接成功
    public static final int RECEIVE_MSG = 667;//接收到IM消息


    public SocketManager() {
        handler = new MyHandler(this);
    }


    public void init(YIMConfig yimConfig) {
        this.yimConfig = yimConfig;
    }

    //********************************连接IM*************************************

    /**
     * 开启新线程，连接socket
     */
    public void connect() {
        closeSocket("SocketManager[newConnectTask]");
        isCloseSocket = false;
        MLogUtils.i("启动新线程--连接IM");
        ThreadManager.getInstance().execute(new connectTask());
    }

    /**
     * 连接socket线程
     */
    private class connectTask implements Runnable {

        @Override
        public void run() {
            executeConnect("newConnectTask");
        }
    }


    /**
     * 连接socket，必须执行于子线程
     */
    public synchronized void executeConnect(String from) {
        MLogUtils.i("是否需要连接IM？", !isConnected, "isCloseSocket", isCloseSocket, "from", from);
        while (!isConnected && !isCloseSocket) {
            try {
                // 发送数据包，默认为 false，即客户端发送数据采用 Nagle 算法；
                // 但是对于实时交互性高的程序，建议其改为 true，即关闭 Nagle 算法，客户端每发送一次数据，无论数据包大小都会将这些数据发送出去
                socket = new Socket();
                socket.setTcpNoDelay(true);
                socket.connect(new InetSocketAddress(yimConfig.ip, yimConfig.port), yimConfig.connectTimeout);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                if (null != inputStream && null != outputStream) {
                    MLogUtils.i("连接IM成功");
                    isConnected = true;
                    handler.sendEmptyMessage(CONNECT_SUCCEED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MLogUtils.e("连接IM失败", e.getMessage());
                isConnected = false;
            }
            try {
                //重连
                Thread.sleep(yimConfig.reconnectionTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否已经连接IM
     *
     * @return
     */
    public boolean isConnected() {
        return isConnected;
    }

    //*************************接收IM消息***************************************

    /**
     * 启动接收线程
     */
    private void startReceive() {
        MLogUtils.i("启动接收IM线程");
        receiveTask = new ReceiveTask(inputStream, yimConfig.dataHeaderLength, this);
        ThreadManager.getInstance().execute(receiveTask);
    }

    /**
     * 接收到消息，分发给主线程
     * 此方法为子线程的回调（接收线程）
     *
     * @param msg
     */
    @Override
    public void onReceiveMsg(String msg) {
        Message message = handler.obtainMessage();
        message.what = SocketManager.RECEIVE_MSG;
        message.obj = msg;
        handler.dispatchMessage(message);
    }


    /**
     * 接收到数据（主线程）
     *
     * @param msg
     */
    public abstract void onReceiveData(String msg);

    /**
     * 接收失败
     *
     * @param e
     */
    @Override
    public void onReceiveFail(Exception e) {
        isConnected = false;
        //重连IM
        if (!isCloseSocket) {
            executeConnect("onReceiveFail");
        } else {
            MLogUtils.e("socket is closed");
        }
    }

    //*****************************发送IM消息*************************************************

    /**
     * 发送消息
     *
     * @param msg 消息内容
     */
    public void sendMsg(String msg) {
        //发送数据，链路空闲时间置为0
        freeTime = 0;
        ThreadManager.getInstance().execute(new SendTask(outputStream, msg, yimConfig.dataHeaderLength, this));
        autoSeqID();
    }

    /**
     * 发送成功
     *
     * @param content 消息内容
     */
    @Override
    public void onSendSucceed(String content) {

    }

    /**
     * 发送失败
     *
     * @param e
     */
    @Override
    public void onSendFail(Exception e) {
        isConnected = false;
        //发送IM消息失败，重新连接IM
        if (!isCloseSocket) {
            executeConnect("onSendFail");
        } else {
            MLogUtils.e("socket is closed");
        }
    }

    //***************************心跳包*******************************

    /**
     * 启动心跳包线程
     */
    public void startHeartPackageTask() {
        heartPackageTask = new HeartPackageTask();
        ThreadManager.getInstance().execute(heartPackageTask);
    }

    /**
     * 关闭心跳包线程
     */
    public void closeHeartPackageTask() {
        if (heartPackageTask != null) {
            heartPackageTask.stop();
        }
    }

    /**
     * 心跳包线程
     */
    private class HeartPackageTask implements Runnable {

        private boolean isStop = false;
        private int SIZE = 512;

        @Override
        public void run() {
            while (!isStop) {
                if (freeTime >= yimConfig.maxFreeTime) {
                    try {
                        String content = getHeartPackageData();
                        MLogUtils.i("send HeartPackage", content);
                        SocketDataUtils.output(outputStream, content, SIZE, yimConfig.dataHeaderLength);
                        //发送心跳包数据成功，链路空闲时间置为0
                        MLogUtils.i("send HeartPackage complete", content);
                        freeTime = 0;
                        autoSeqID();
                    } catch (Exception e) {
                        e.printStackTrace();
                        MLogUtils.e("send HeartPackage Exception", e.getMessage());
                        stop();
                        return;
                    }
                }
                int time = 5 * 1000;
                try {
                    Thread.sleep(time);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                freeTime += time;
            }
        }


        public void stop() {
            isStop = true;
            isConnected = false;
            //发送心跳包异常，重连IM
            if (!isCloseSocket) {
                executeConnect("HeartPackageTask[close]");
            } else {
                MLogUtils.e("socket is closed");
            }
        }

    }

    public abstract String getHeartPackageData();


    //*****************************handler***********************************************

    public static class MyHandler extends Handler {
        WeakReference<SocketManager> managerWeakReference;

        public MyHandler(SocketManager socketManager) {
            managerWeakReference = new WeakReference<>(socketManager);
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            final SocketManager socketManager = managerWeakReference.get();
            if (socketManager == null) {
                return;
            }
            switch (msg.what) {
                case CONNECT_SUCCEED:  //IM连接成功
                    //启动接收线程
                    socketManager.startReceive();
                    socketManager.connectSucceed();
                    break;
                case RECEIVE_MSG://处理接收到IM消息
                    socketManager.onReceiveData((String) msg.obj);
                    break;
                default:
                    socketManager.handlerMessage(msg);
                    break;
            }
        }
    }


    public abstract void connectSucceed();

    public abstract void handlerMessage(Message msg);

    //**********************************关闭IM连接*****************************************

    /**
     * 关闭连接
     */
    public void closeSocket(String from) {
        MLogUtils.e("关闭socket", from);
        isConnected = false;
        isCloseSocket = true;
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (receiveTask != null) {
                receiveTask.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            closeHeartPackageTask();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * seqID自增
     */
    private void autoSeqID() {
        if (seqID < Integer.MAX_VALUE) {
            seqID++;
        } else {
            seqID = 0;
        }
    }

}
