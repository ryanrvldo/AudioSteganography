package com.ryanrvldo.audiosteganography.utils;

import android.util.Log;

import com.ryanrvldo.audiosteganography.model.FileData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileHelper {

    private static final String TAG = "FileHelper";

    public static FileData getFileData(InputStream inputStream, String filePath) {
        FileData fileData = new FileData(filePath);

        byte[] fileBytes = readByteFile(inputStream);
        if (fileBytes == null) return null;

        fileData.setFileBytes(fileBytes);
        setFileInfo(fileData);
        return fileData;
    }

    public static byte[] readByteFile(InputStream inputStream) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[1000000];
            int bytesRead;
            if (inputStream != null) {
                while ((bytesRead = inputStream.read(bytes)) != -1) {
                    bos.write(bytes, 0, bytesRead);
                }
            }
            inputStream.close();
            return bos.toByteArray();
        } catch (IOException e) {
            Log.d(TAG, "readByteFile: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    static void setFileInfo(FileData fileData) {
        String ext = "";
        String name = "";
        boolean check = false;
        char tmp;
        if (fileData.getFilePath() != null) {
            for (int i = fileData.getFilePath().length() - 1; i > 0; i--) {
                tmp = fileData.getFilePath().charAt(i);
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
        fileData.setFileExt(ext);
        fileData.setFileName(name);
    }

}
