package com.jy.yim.core;


import androidx.annotation.NonNull;

import com.jy.yim.utils.MLogUtils;
import com.jy.yim.utils.SocketDataUtils;

import java.io.IOException;
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

    public SendTask(OutputStream outputStream, @NonNull String content, ISend callback) {
        this.outputStream = outputStream;
        this.content = content;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {

            MLogUtils.i("send the content", content);
            SocketDataUtils.output(outputStream, content, SIZE);
            MLogUtils.i("send the content complete", content);

            callback.onSendSucceed(content);

        } catch (IOException e) {
            // 这里要处理发送失败
            e.printStackTrace();
            MLogUtils.e("send the content Exception", e.getMessage());
            callback.onSendFail(e);
        }
    }

}
