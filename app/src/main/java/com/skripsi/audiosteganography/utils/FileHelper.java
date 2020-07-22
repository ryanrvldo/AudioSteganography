package com.skripsi.audiosteganography.utils;

import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

public class FileHelper implements PickiTCallbacks {
    private PickiT pick;
    private String filePath;

    public FileHelper(FragmentActivity activity) {
        pick = new PickiT(activity, this, activity);
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
