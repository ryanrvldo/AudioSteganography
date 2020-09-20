package com.ryanrvldo.audiosteganography.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ryanrvldo.audiosteganography.model.FileData;
import com.ryanrvldo.audiosteganography.utils.FileHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DecompressViewModel extends ViewModel {
    private MutableLiveData<FileData> fileData = new MutableLiveData<>();

    public void setFileData(InputStream inputStream, String filePath) {
        fileData.setValue(FileHelper.getFileData(inputStream, filePath));
    }

    public LiveData<FileData> getFileData() {
        return fileData;
    }

    public byte[] decompressFile(byte[] initBytes) {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            byte sign = '#';
            StringBuilder charCount = new StringBuilder();
            int count = 0;
            boolean flag = false;
            for (byte current : initBytes) {
                if (!flag) {
                    if (current == sign) {
                        flag = true;
                    } else {
                        byteArray.write(current);
                        charCount = new StringBuilder();
                    }
                } else {
                    if (!Character.isDigit(current)) {
                        if (count > 0) {
                            for (int i = 0; i < count; i++) {
                                byteArray.write(current);
                            }
                            charCount = new StringBuilder();
                            count = 0;
                        } else {
                            byteArray.write(sign);
                            byteArray.write(current);
                        }
                        flag = false;
                    } else {
                        charCount.append((char) current);
                        count = Integer.parseInt(charCount.toString());
                    }
                }
            }
            byteArray.flush();
            return byteArray.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}