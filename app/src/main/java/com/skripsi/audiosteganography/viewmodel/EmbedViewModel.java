package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skripsi.audiosteganography.model.FileData;
import com.skripsi.audiosteganography.model.PseudoRandomNumber;

public class EmbedViewModel extends ViewModel {
    private MutableLiveData<FileData> fileData = new MutableLiveData<>();
    private MutableLiveData<Integer[]> xnValue = new MutableLiveData<>();
    private String message;

    private Repository repository;

    public EmbedViewModel() {
        repository = new Repository();
    }

    public void setFileData(ContentResolver resolver, Uri uri, String filePath) {
        fileData.setValue(repository.getFileData(resolver, uri, filePath));
    }

    public LiveData<FileData> getFileData() {
        return fileData;
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

    public void setXnValue(PseudoRandomNumber randomNumber) {
        xnValue.setValue(repository.getXN(randomNumber));
    }

    public LiveData<Integer[]> getXnValue() {
        return xnValue;
    }

    public void saveKey(Context context, PseudoRandomNumber randomNumber) {
        repository.saveKey(context, randomNumber);
    }

}
