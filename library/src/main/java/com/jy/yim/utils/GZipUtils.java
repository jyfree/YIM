package com.jy.yim.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Administrator
 * created at 2016/2/29 10:21
 * TODO:Gzip管理类
 */
public class GZipUtils {

    /**
     * 解压
     *
     * @param data
     * @return
     */
    public static String uncompressToString(byte[] data) {
        String result = null;
        if (null == data || data.length <= 0) {
            return null;
        }
        // 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        // 使用默认缓冲区大小创建新的输入流
        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n = 0;
            while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组
                // 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流
                out.write(buffer, 0, n);
            }
            // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
            result = out.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            MLogUtils.e("e", e.getMessage());
        } finally {
            try {
                out.flush();
                out.close();

                in.close();

                if (null != gzip) {
                    gzip.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }


    /**
     * @param data 需要压缩的内容
     */
    public static byte[] compressToBtyes(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 压缩
        compress(bais, baos);

        byte[] output = baos.toByteArray();

        baos.flush();
        baos.close();

        bais.close();

        return output;

    }

    /**
     * 数据压缩
     *
     * @param is
     * @param os
     * @throws Exception
     */
    public static void compress(InputStream is, OutputStream os)
            throws IOException {
        GZIPOutputStream gos = new GZIPOutputStream(os);

        int count;
        byte data[] = new byte[1024];
        int num = 0;
        while ((count = is.read(data, 0, data.length)) != -1) {
            gos.write(data, 0, count);
        }
        gos.finish();
        // gos.flush();//4.4.2会出错
        gos.close();
    }

}
