package com.ryanrvldo.audiosteganography.utils;

import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.ryanrvldo.audiosteganography.model.FileData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileHelper implements PickiTCallbacks {
    private static FileData fileData;
    private PickiT pick;
    private String filePath;

    public FileHelper(FragmentActivity activity) {
        pick = new PickiT(activity, this, activity);
    }

    public static FileData getFileData(InputStream inputStream, String filePath) {
        fileData = new FileData(filePath);
        fileData.setFileBytes(readByteFile(inputStream));
        setFileInfo();
        System.out.println(fileData.getFileName());
        return fileData;
    }

    public static byte[] readByteFile(InputStream inputStream) {
        try {
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

    static void setFileInfo() {
        String ext = "";
        String name = "";
        boolean check = false;
        char tmp;
        if (fileData.getFilePath() != null) {
            for (int i = fileData.getFilePath().length(); i > 0; i--) {
                tmp = fileData.getFilePath().charAt(i - 1);
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

    public void setPick(Uri uri, int apiLevel) {
        pick.getPath(uri, apiLevel);
    }

    public String getFilePath() {
        return filePath;
    }

    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void PickiTonUriReturned() {

    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        setFilePath(path);
    }
}
