package com.skripsi.audiosteganography.utils;

import android.content.Context;
import android.net.Uri;

import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

public class FileHelper implements PickiTCallbacks {
    private PickiT pick;
    private String filePath;

    public FileHelper(Context context) {
        pick = new PickiT(context, this);
    }

    public void setPick(Uri uri, int apiLevel) {
        pick.getPath(uri, apiLevel);
    }

    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
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
