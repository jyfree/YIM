package com.jy.yim.utils;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @description socket数据包服务
 * @date: 2020/4/30 17:01
 * @author: jy
 */
public class SocketDataUtils {

    public static void output(OutputStream outputStream, String content, int size, int dataHeaderLength) throws Exception {
        BufferedOutputStream bops = new BufferedOutputStream(outputStream,
                4 * size);
        // 压缩过后的byte数
        byte[] contentBytes = GZipUtils.compressToBtyes(content
                .getBytes("UTF-8"));
        // 压缩过后 内容的长度
        int length = contentBytes.length;
        // 最后传输的数据
        byte[] data = new byte[length + dataHeaderLength];
        // 内容长度的字节数
        byte[] lengthData = ByteUtil.toByteArray(length, dataHeaderLength);
        // 发给服务器的字节 要先拼了内容长度的字节数 再拼上真实内容的字节数
        for (int i = 0; i < (length + dataHeaderLength); i++) {
            if (i < dataHeaderLength) {
                data[i] = lengthData[i];
            } else {
                data[i] = contentBytes[i - dataHeaderLength];
            }
        }

        bops.write(data);
        bops.flush();
    }

    /**
     * 解socket数据包体
     *
     * @param is
     * @return
     * @throws Exception
     */
    public static String getDataBody(InputStream is, int dataHeaderLength) throws Exception {
        String dataBody = null;
        // 获取头部
        byte[] head = getData(is, dataHeaderLength);
        int dataLength = ByteUtil.toInt(head, dataHeaderLength);

        // 获取数据
        byte[] data = getData(is, dataLength);
        dataBody = GZipUtils.uncompressToString(data);

        return dataBody;
    }

    /**
     * 拆包
     *
     * @param is
     * @param length
     * @return
     * @throws Exception
     */
    private static byte[] getData(InputStream is, int length) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[5120];
        int nIdx = 0; //累计读取了多少位
        int nReadLen = 0; //一次读取了多少位

        while (nIdx < length) { //循环读取足够长度的数据

            if (length - nIdx >= buffer.length) { //剩余数据大于缓存，则全部读取
                nReadLen = is.read(buffer);
            } else { //剩余数据小于缓存，则注意拆分其他包，只取当前包剩余数据
                nReadLen = is.read(buffer, 0, length - nIdx);
            }

            if (nReadLen > 0) {
                baos.write(buffer, 0, nReadLen);
                nIdx = nIdx + nReadLen;
            } else {
                break;
            }

        }

        return baos.toByteArray();
    }

}
