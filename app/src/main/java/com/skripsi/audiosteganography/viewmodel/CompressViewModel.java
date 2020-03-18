package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CompressViewModel extends ViewModel {
    public static final String TAG = "COMPRESS";

    private Repository repository;
    private MutableLiveData<byte[]> initBytes = new MutableLiveData<>();

    public CompressViewModel() {
        repository = new Repository();
    }

    public void setInitBytes(ContentResolver contentResolver, Uri uri) {
        initBytes.setValue(repository.readByteFile(contentResolver, uri));
    }

    public LiveData<byte[]> getInitBytes() {
        return initBytes;
    }


    public void setFileInfo(String file) {
        repository.setFileInfo(file);
    }

    public String getFileExt() {
        return repository.getFileExt();
    }

    public String getFileName() {
        return repository.getFileName();
    }
}