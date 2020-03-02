package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ExtractViewModel extends ViewModel {
    private MutableLiveData<byte[]> bytesAudio = new MutableLiveData<>();
    private MutableLiveData<Integer[]> xnValue = new MutableLiveData<>();

    private int[] key = new int[5];
    private Repository repository;

    public ExtractViewModel() {
        repository = new Repository();
    }

    public void setBytesAudio(ContentResolver contentResolver, Uri uri) {
        bytesAudio.setValue(repository.readByteFile(contentResolver, uri));
    }

    public LiveData<byte[]> getBytesAudio() {
        return bytesAudio;
    }

    public void setKey(ContentResolver contentResolver, Uri uri) {
        byte[] byteMessage = repository.readByteFile(contentResolver, uri);
        StringBuilder builder = new StringBuilder();
        for (byte msgByte : byteMessage) {
            builder.append((char) msgByte);
        }
        StringTokenizer tokenizer = new StringTokenizer(builder.toString(), ",");
        for (int i = 0; i < key.length; i++) {
            key[i] = Integer.parseInt(tokenizer.nextToken());
        }
    }

    public int[] getKey() {
        return key;
    }

    public void setXnValue(int length, int a, int b, int c0, int x0) {
        repository.setKey(a, b, c0, x0);
        xnValue.setValue(repository.getXN(length));
    }

    public LiveData<Integer[]> getXnValue() {
        return xnValue;
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

    public String[] generateBinaryToMessage(String binaryMessage) {
        List<String> parts = new ArrayList<>();

        int length = binaryMessage.length();
        for (int i = 0; i < length; i += 8) {
            parts.add(binaryMessage.substring(i, Math.min(length, i + 8)));
        }
        return parts.toArray(new String[0]);
    }
}
