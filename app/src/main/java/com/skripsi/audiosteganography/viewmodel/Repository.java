package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class Repository {

    private int a;
    private int b;
    private int c0;
    private int x0;
    private int lengthNumbers;

    private String fileExt = "";
    private String fileName = "";

    byte[] readByteFile(ContentResolver contentResolver, Uri uri) {
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] bytes = new byte[1000000];
            int bytesRead;
            if (inputStream != null) {
                while ((bytesRead = inputStream.read(bytes)) != -1) {
                    bos.write(bytes, 0, bytesRead);
                }
            }
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    void setKey(int a, int b, int c0, int x0) {
        this.a = a;
        this.b = b;
        this.c0 = c0;
        this.x0 = x0;
    }

    Integer[] getXN(int length) {
        Integer[] XN = new Integer[length];
        Map<Integer, Boolean> tempXN = new HashMap<>();

        int xn1 = x0;
        int cn1 = c0;
        lengthNumbers = 0;
        while (lengthNumbers < length) {
            int temp = (a * xn1) + cn1;
            xn1 = temp % b;
            cn1 = temp / b;

            if (tempXN.size() != 0) {
                while (tempXN.containsKey(xn1)) {
                    xn1++;
                    if (xn1 == b) xn1 += 5;
                }
            }
            tempXN.put(xn1, true);
            XN[lengthNumbers] = xn1;
            lengthNumbers++;
        }
        return XN;
    }

    void saveKey(Context context) {
        File path = context.getExternalFilesDir(null);
        File file = new File(path, "stego_key.txt");
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(a + ",");
            bufferedWriter.write(b + ",");
            bufferedWriter.write(c0 + ",");
            bufferedWriter.write(x0 + ",");
            bufferedWriter.write(lengthNumbers + ",");

            bufferedWriter.flush();
            bufferedWriter.close();
            Log.d("REPO", "saveKey: SUCCESS");
        } catch (IOException e) {
            Log.d("REPO", "saveKey: FAILED");
        }
    }

    void setFileInfo(String file) {
        String ext = "";
        String name = "";
        boolean check = false;
        char tmp;
        if (file != null) {
            for (int i = file.length(); i > 0; i--) {
                tmp = file.charAt(i - 1);
                if (tmp == '/') {
                    i = 0;
                } else {
                    if (tmp == '.') {
                        check = true;
                    } else {
                        if (!check) {
                            ext = String.format("%s%s", tmp, ext);
                        } else {
                            name = String.format("%s%s", tmp, name);
                        }
                    }
                }
            }
        }
        fileExt = ext;
        fileName = name;
    }

    String getFileExt() {
        return fileExt;
    }

    String getFileName() {
        return fileName;
    }
}
