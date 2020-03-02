package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CompressViewModel extends ViewModel {
    public static final String TAG = "COMPRESS";

    private Repository repository;
    private MutableLiveData<byte[]> bytesAudio = new MutableLiveData<>();
    private MutableLiveData<byte[]> bytesAudioCompressed = new MutableLiveData<>();

    public CompressViewModel() {
        repository = new Repository();
    }

    public void setByteAudio(ContentResolver contentResolver, Uri uri) {
        bytesAudio.setValue(repository.readByteFile(contentResolver, uri));
    }

    public LiveData<byte[]> getBytesAudio() {
        return bytesAudio;
    }

    public void setBytesAudioCompressed(byte[] bytes) {
        bytesAudioCompressed.setValue(bytes);
    }

    public LiveData<byte[]> getBytesAudioCompressed() {
        return bytesAudioCompressed;
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