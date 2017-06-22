package com.example.administrator.xflytest.tranlate;

import java.security.MessageDigest;


/**
 * MD5编码相关的类
 *
 */
public class MD5 {

    public static String encode(String string) throws Exception {
        //string.getBytes("UTF-8") 将string编码成utf-8的字节数组
        byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
       
    }
 
}
