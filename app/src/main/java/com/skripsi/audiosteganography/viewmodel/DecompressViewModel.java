package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DecompressViewModel extends ViewModel {

    public static final String TAG = "COMPRESS";

    private Repository repository;
    private MutableLiveData<byte[]> bytesAudioCompressed = new MutableLiveData<>();
    private MutableLiveData<byte[]> bytesAudio = new MutableLiveData<>();

    public DecompressViewModel() {
        repository = new Repository();
    }

    public void setBytesAudioCompressed(ContentResolver resolver, Uri uri) {
        bytesAudioCompressed.setValue(repository.readByteFile(resolver, uri));
    }

    public LiveData<byte[]> getBytesAudioCompressed() {
        return bytesAudioCompressed;
    }

    public void setBytesAudio(byte[] bytes) {
        bytesAudio.setValue(bytes);
    }

    public LiveData<byte[]> getBytesAudio() {
        return bytesAudio;
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