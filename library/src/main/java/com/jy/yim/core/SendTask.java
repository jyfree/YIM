package com.jy.yim.core;


import com.jy.yim.utils.MLogUtils;
import com.jy.yim.utils.SocketDataUtils;

import java.io.OutputStream;


/**
 * @description 发生消息
 * @date: 2020/4/30 17:03
 * @author: jy
 */
public class SendTask implements Runnable {

    private int SIZE = 1024;
    private OutputStream outputStream;
    private String content;
    private ISend callback;
    private int dataHeaderLength;

    public SendTask(OutputStream outputStream, String content, int dataHeaderLength, ISend callback) {
        this.outputStream = outputStream;
        this.content = content;
        this.callback = callback;
        this.dataHeaderLength = dataHeaderLength;
    }

    @Override
    public void run() {
        try {

            MLogUtils.i("send the content", content);
            SocketDataUtils.output(outputStream, content, SIZE, dataHeaderLength);
            MLogUtils.i("send the content complete", content);

            callback.onSendSucceed(content);

        } catch (Exception e) {
            // 这里要处理发送失败
            e.printStackTrace();
            MLogUtils.e("send the content Exception", e.getMessage());
            callback.onSendFail(e);
        }
    }

}
