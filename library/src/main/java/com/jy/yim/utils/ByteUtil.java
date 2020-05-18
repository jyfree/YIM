package com.jy.yim.utils;

/**
 * @description byte转换工具
 * @date: 2020/5/9 15:29
 * @author: jy
 */
public class ByteUtil {

    /**
     * 将iSource转为长度为iArrayLen的byte数组，字节数组的低位是整型的低字节位
     *
     * @param iSource
     * @param iArrayLen
     * @return
     */
    public static byte[] toByteArray(int iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);

        }
        return bLocalArr;
    }

    /**
     * 将byte数组bRefArr转为一个整数,字节数组的低位是整型的低字节位
     *
     * @param bRefArr
     * @return
     */
    public static int toInt(byte[] bRefArr, int dataHeaderLength) {
        int iOutcome = 0;
        byte bLoop;

        if (bRefArr.length != 0) {
            for (int i = 0; i < dataHeaderLength; i++) {
                bLoop = bRefArr[i];
                iOutcome += (bLoop & 0xFF) << (8 * i);
            }
        }
        return iOutcome;
    }

}
