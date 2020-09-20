package com.ryanrvldo.audiosteganography.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ryanrvldo.audiosteganography.model.FileData;
import com.ryanrvldo.audiosteganography.utils.FileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CompressViewModel extends ViewModel {
    private MutableLiveData<FileData> fileData = new MutableLiveData<>();

    public void setFileData(InputStream inputStream, String filePath) {
        fileData.setValue(FileHelper.getFileData(inputStream, filePath));
    }

    public LiveData<FileData> getFileData() {
        return fileData;
    }

    public byte[] compressFile(byte[] initBytes) {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            StringBuilder strCount = new StringBuilder();
            byte sign = '#';
            byte current = initBytes[0];
            int count = 1;
            for (int i = 1; i < initBytes.length; i++) {
                if (current == initBytes[i]) {
                    count++;
                } else {
                    if (count > 2) {
                        strCount.append(count);
                        byteArray.write(sign);
                        byteArray.write(strCount.toString().getBytes());
                        byteArray.write(current);
                    } else {
                        for (int j = 0; j < count; j++) {
                            byteArray.write(current);
                        }
                    }
                    current = initBytes[i];
                    strCount = new StringBuilder();
                    count = 1;
                }
            }
            if (count > 2) {
                strCount.append(count);
                byteArray.write(sign);
                byteArray.write(strCount.toString().getBytes());
            }
            byteArray.write(current);
            byteArray.flush();
            return byteArray.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}