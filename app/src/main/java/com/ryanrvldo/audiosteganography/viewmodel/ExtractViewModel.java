package com.ryanrvldo.audiosteganography.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ryanrvldo.audiosteganography.model.FileData;
import com.ryanrvldo.audiosteganography.model.Seed;
import com.ryanrvldo.audiosteganography.utils.FileHelper;
import com.ryanrvldo.audiosteganography.utils.MWCGenerator;

import java.io.InputStream;
import java.util.StringTokenizer;

public class ExtractViewModel extends ViewModel {
    private final MutableLiveData<FileData> fileData = new MutableLiveData<>();
    private final MutableLiveData<Integer[]> xnValue = new MutableLiveData<>();
    private Seed randomNumber;

    public void setFileData(InputStream inputStream, String filePath) {
        FileData data = FileHelper.getFileData(inputStream, filePath);
        if (data != null) fileData.postValue(data);
    }

    public LiveData<FileData> getFileData() {
        return fileData;
    }

    public Seed getKey() {
        return randomNumber;
    }

    public void setKey(InputStream inputStream) {
        byte[] byteMessage = FileHelper.readByteFile(inputStream);
        if (byteMessage == null) {
            randomNumber = null;
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (byte msgByte : byteMessage) {
            builder.append((char) msgByte);
        }

        try {
            StringTokenizer tokenizer = new StringTokenizer(builder.toString(), ",");
            int tokenLength = tokenizer.countTokens();
            int[] key = new int[tokenLength];
            for (int i = 0; i < tokenLength; i++) {
                key[i] = Integer.parseInt(tokenizer.nextToken());
            }
            randomNumber = new Seed(key[0], key[1], key[2], key[3], key[4]);
        } catch (NumberFormatException e) {
            Log.e("ReadKey", "error: ", e);
            randomNumber = null;
        }
    }

    public void setXnValue() {
        xnValue.setValue(MWCGenerator.getXN(randomNumber));
    }

    public LiveData<Integer[]> getXnValue() {
        return xnValue;
    }

    public String generateBinaryToString(String binary) {
        int length = binary.length();
        char[] chars = new char[length / 8];
        for (int i = 0; i < length; i += 8) {
            int num = Integer.parseInt(binary.substring(i, i + 8), 2);
            chars[i / 8] = (char) num;
        }
        return new String(chars);
    }
}
