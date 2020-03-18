package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;

public class EmbedViewModel extends ViewModel {
    private static final String TAG = "EMBED";

    private MutableLiveData<byte[]> headerAudio = new MutableLiveData<>();
    private MutableLiveData<byte[]> dataAudio = new MutableLiveData<>();
    private MutableLiveData<Integer[]> xnValue = new MutableLiveData<>();
    private byte[] bytesAudio;

    private String message;
    private Repository repository;

    public EmbedViewModel() {
        repository = new Repository();
    }

    public void setBytesAudio(ContentResolver resolver, Uri uri) {
        bytesAudio = repository.readByteFile(resolver, uri);
        if (bytesAudio != null) {
            setHeaderAudio();
            setDataAudio();
        }
    }

    private void setDataAudio() {
        dataAudio.setValue(Arrays.copyOfRange(bytesAudio, 40, bytesAudio.length));
    }

    public LiveData<byte[]> getDataAudio() {
        return dataAudio;
    }

    private void setHeaderAudio() {
        headerAudio.setValue(Arrays.copyOfRange(bytesAudio, 0, 40));
    }

    public LiveData<byte[]> getHeaderAudio() {
        return headerAudio;
    }

    public void setMessage(ContentResolver contentResolver, Uri uri) {
        byte[] byteMessage = repository.readByteFile(contentResolver, uri);
        StringBuilder builder = new StringBuilder();
        for (byte msgByte : byteMessage) {
            builder.append((char) msgByte);
        }
        message = builder.toString();
    }

    public String getMessage() {
        return message;
    }


    public char[] getCharMessage(String msg) {
        StringBuilder builder = new StringBuilder();
        for (char charMessage : msg.toCharArray()) {
            builder.append(String.format("%8s", Integer.toBinaryString(0xFFFFFF & charMessage)).replaceAll(" ", "0"));
        }
        return builder.toString().toCharArray();
    }

    public void setXnValue(int length, int a, int b, int c0, int x0) {
        repository.setKey(a, b, c0, x0);
        xnValue.setValue(repository.getXN(length));
    }

    public LiveData<Integer[]> getXnValue() {
        return xnValue;
    }

    public void saveKey(Context context) {
        repository.saveKey(context);
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
