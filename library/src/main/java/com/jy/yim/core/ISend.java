package com.jy.yim.core;

public interface ISend {
    void onSendSucceed(String content);

    void onSendFail(Exception e);
}
