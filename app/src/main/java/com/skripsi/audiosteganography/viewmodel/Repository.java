package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.skripsi.audiosteganography.model.FileData;
import com.skripsi.audiosteganography.model.PseudoRandomNumber;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

class Repository {
    public static final String TAG = "REPOSITORY";
    private FileData fileData;

    public FileData getFileData(ContentResolver resolver, Uri uri, String filePath) {
        this.fileData = new FileData(filePath);
        this.fileData.setFileBytes(readByteFile(resolver, uri));
        setFileInfo();
        System.out.println(this.fileData.getFileName());
        return fileData;
    }

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
            Log.e(TAG, "readByteFile: ", e);
            return null;
        }
    }

    void setFileInfo() {
        String ext = "";
        String name = "";
        boolean check = false;
        char tmp;
        if (this.fileData.getFilePath() != null) {
            for (int i = this.fileData.getFilePath().length(); i > 0; i--) {
                tmp = this.fileData.getFilePath().charAt(i - 1);
                if (tmp == '/') i = 0;
                else {
                    if (tmp == '.') check = true;
                    else {
                        if (!check) ext = String.format("%s%s", tmp, ext);
                        else name = String.format("%s%s", tmp, name);
                    }
                }
            }
        }
        this.fileData.setFileExt(ext);
        this.fileData.setFileName(name);
    }

    Integer[] getXN(PseudoRandomNumber randomNumber) {
        Integer[] XN = new Integer[randomNumber.getLength()];
        Map<BigInteger, Boolean> tempXN = new HashMap<>();

        BigInteger xn1 = randomNumber.getX0();
        BigInteger cn1 = randomNumber.getC0();
        for (int i = 0; i < randomNumber.getLength(); i++) {
            BigInteger tmp = randomNumber.getA().multiply(xn1).add(cn1);
            xn1 = tmp.mod(randomNumber.getB());
            cn1 = BigInteger.valueOf(Math.floorDiv(tmp.intValue(), randomNumber.getB().intValue()));

            if (tempXN.size() != 0) {
                while (tempXN.containsKey(xn1)) {
                    xn1 = xn1.add(BigInteger.ONE);
                    if (xn1.equals(randomNumber.getB())) xn1 = BigInteger.ZERO;
                }
            }
            tempXN.put(xn1, true);
            XN[i] = xn1.intValue();
        }
        return XN;
    }

    void saveKey(Context context, PseudoRandomNumber randomNumber) {
        File path = context.getExternalFilesDir(null);
        File file = new File(path, fileData.getFileName() + ".key");
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(randomNumber.getA() + ",");
            bufferedWriter.write(randomNumber.getB() + ",");
            bufferedWriter.write(randomNumber.getC0() + ",");
            bufferedWriter.write(randomNumber.getX0() + ",");
            bufferedWriter.write(randomNumber.getLength() + ",");

            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "saveKey: ", e);
        }
    }
}
