package com.opentaxi.android.utils;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/6/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectSerializer {

    public static String serialize(Serializable obj) throws IOException {
        if (obj == null)
            return "";
        ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
        objStream.writeObject(obj);
        objStream.close();
        return encodeBytes(serialObj.toByteArray());
    }

    public static Object deserialize(String str) throws IOException,
            ClassNotFoundException {
        if (str == null || str.length() == 0)
            return null;
        ByteArrayInputStream serialObj = new ByteArrayInputStream(
                decodeBytes(str));
        ObjectInputStream objStream = new ObjectInputStream(serialObj);
        return objStream.readObject();
    }

    private static String encodeBytes(byte[] bytes) {
        StringBuilder strBuf = new StringBuilder();

        for (byte aByte : bytes) {
            strBuf.append((char) (((aByte >> 4) & 0xF) + ((int) 'a')));
            strBuf.append((char) (((aByte) & 0xF) + ((int) 'a')));
        }

        return strBuf.toString();
    }

    private static byte[] decodeBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i += 2) {
            char c = str.charAt(i);
            bytes[i / 2] = (byte) ((c - 'a') << 4);
            c = str.charAt(i + 1);
            bytes[i / 2] += (c - 'a');
        }
        return bytes;
    }
}