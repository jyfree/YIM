package com.jy.yim.core;

public interface IReceive {
    void onReceiveMsg(String msg);

    void onReceiveFail(Exception e);
}
