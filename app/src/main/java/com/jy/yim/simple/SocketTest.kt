package com.jy.yim.simple

import android.os.Message
import com.jy.yim.core.SocketManager

class SocketTest : SocketManager() {


    override fun connectSucceed() {
    }
    override fun onReceiveData(msg: String?) {

    }

    override fun getHeartPackageData(): String {
        return ""
    }

    override fun handlerMessage(msg: Message?) {
    }


}