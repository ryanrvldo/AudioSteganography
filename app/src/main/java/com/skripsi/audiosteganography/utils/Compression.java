package com.skripsi.audiosteganography.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Compression {

    public static byte[] encode(byte[] bytes) throws IOException {
        if (bytes == null) return null;

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        byte sign = '~';
        byte current = bytes[0];
        int count = 1;

        for (int i = 1; i < bytes.length; i++) {
            if (current == bytes[i]) {
                count++;
            } else {
                if (count >= 3) {
                    byteArray.write(sign);
                    byteArray.write(count);
                    byteArray.write(current);
                } else {
                    for (int j = 0; j < count; j++) {
                        byteArray.write(current);
                    }
                }
                current = bytes[i];
                count = 1;
            }
        }
        if (count > 1) byteArray.write(current);
        byteArray.write(current);
        byteArray.flush();
        byteArray.close();
        return byteArray.toByteArray();
    }

    public static String decode(String string) {
        if (string == null || string.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        char[] chars = string.toCharArray();
        boolean preIsDigit = false;
        StringBuilder digitString = new StringBuilder();
        for (char current : chars) {
            if (!Character.isDigit(current)) {
                if (preIsDigit) {
                    String multipleString = new String(new char[Integer.parseInt(digitString.toString())]).replace("\0", current + "");
                    builder.append(multipleString);
                    preIsDigit = false;
                    digitString = new StringBuilder();
                } else {
                    builder.append(current);
                }
            } else {
                digitString.append(current);
                preIsDigit = true;
            }
        }
        return builder.toString().replaceAll("~", "");
    }
}
