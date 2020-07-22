package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skripsi.audiosteganography.model.FileData;
import com.skripsi.audiosteganography.model.PseudoRandomNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ExtractViewModel extends ViewModel {
    private MutableLiveData<FileData> fileData = new MutableLiveData<>();
    private MutableLiveData<Integer[]> xnValue = new MutableLiveData<>();
    private PseudoRandomNumber randomNumber;

    private Repository repository;

    public ExtractViewModel() {
        repository = new Repository();
    }

    public void setFileData(ContentResolver resolver, Uri uri, String filePath) {
        fileData.postValue(repository.getFileData(resolver, uri, filePath));
    }

    public LiveData<FileData> getFileData() {
        return fileData;
    }

    public void setKey(ContentResolver contentResolver, Uri uri) {
        byte[] byteMessage = repository.readByteFile(contentResolver, uri);
        StringBuilder builder = new StringBuilder();
        for (byte msgByte : byteMessage) {
            builder.append((char) msgByte);
        }
        StringTokenizer tokenizer = new StringTokenizer(builder.toString(), ",");
        int[] key = new int[5];
        for (int i = 0; i < 5; i++) {
            key[i] = Integer.parseInt(tokenizer.nextToken());
        }
        randomNumber = new PseudoRandomNumber(key[0], key[1], key[2], key[3], key[4]);
    }

    public PseudoRandomNumber getKey() {
        return randomNumber;
    }

    public void setXnValue() {
        xnValue.setValue(repository.getXN(randomNumber));
    }

    public LiveData<Integer[]> getXnValue() {
        return xnValue;
    }

    public String[] generateBinaryToString(String binaryMessage) {
        List<String> parts = new ArrayList<>();

        int length = binaryMessage.length();
        for (int i = 0; i < length; i += 8) {
            parts.add(binaryMessage.substring(i, Math.min(length, i + 8)));
        }
        return parts.toArray(new String[0]);
    }
}
