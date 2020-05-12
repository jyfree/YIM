package com.jy.yim.core;

import com.jy.yim.utils.MLogUtils;
import com.jy.yim.utils.SocketDataUtils;

import java.io.InputStream;


/**
 * @description 接收线程
 * @date: 2020/4/30 16:51
 * @author: jy
 */
public class ReceiveTask implements Runnable {


    private InputStream inputStream;
    private boolean isClose = false;
    private IReceive callback;

    public ReceiveTask(InputStream inputStream, IReceive callback) {
        this.inputStream = inputStream;
        this.callback = callback;
    }

    @Override
    public void run() {
        while (!isClose) {
            try {
                //这里进行字节流的处理  不再使用字符流
                String msg = SocketDataUtils.getDataBody(inputStream);

                if (null != msg && !msg.trim().isEmpty()) {
                    MLogUtils.i("receive new message", msg);
                    callback.onReceiveMsg(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                close();
                MLogUtils.e("receive message Exception", e.getMessage());
                callback.onReceiveFail(e);
                break;
            }

        }


    }

    public boolean isClose() {
        return isClose;
    }

    public void close() {
        isClose = true;
    }
}
