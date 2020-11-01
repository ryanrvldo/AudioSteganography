package com.ryanrvldo.audiosteganography.viewmodel;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ryanrvldo.audiosteganography.model.FileData;
import com.ryanrvldo.audiosteganography.model.Seed;
import com.ryanrvldo.audiosteganography.utils.FileHelper;
import com.ryanrvldo.audiosteganography.utils.MWCGenerator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class EmbedViewModel extends ViewModel {
    private final MutableLiveData<FileData> fileData = new MutableLiveData<>();
    private final MutableLiveData<Integer[]> xnValue = new MutableLiveData<>();
    private String message;

    public void setFileData(InputStream inputStream, String filePath) {
        FileData data = FileHelper.getFileData(inputStream, filePath);
        if (data != null) fileData.postValue(data);
    }

    public LiveData<FileData> getFileData() {
        return fileData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(InputStream inputStream) {
        byte[] byteMessages = FileHelper.readByteFile(inputStream);
        if (byteMessages == null) {
            message = null;
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (byte byteMessage : byteMessages) {
            builder.append((char) byteMessage);
        }
        message = builder.toString();
    }

    @SuppressLint("DefaultLocale")
    public char[] getBinaryMessage(String msg) {
        StringBuilder builder = new StringBuilder();
        for (char charMessage : msg.toCharArray()) {
            String charBin = Integer.toString((int) charMessage, 2);
            builder.append(String.format("%08d", Integer.parseInt(charBin)));
        }
        return builder.toString().toCharArray();
    }

    public LiveData<Integer[]> getXnValue() {
        return xnValue;
    }

    public void setXnValue(Seed randomNumber) {
        xnValue.setValue(MWCGenerator.getXN(randomNumber));
    }

    public byte[] embedMessage(byte[] initBytes, char[] charsMessage, Integer[] xn) {
        byte[] resultBytes = initBytes.clone();
        int length = charsMessage.length;
        for (int i = 0; i < length; i++) {
            if (charsMessage[i] == '1' && ((Math.abs(resultBytes[xn[i]])) % 2 == 0)) {
                resultBytes[xn[i]] += 1;
            } else if (charsMessage[i] == '0' && ((Math.abs(resultBytes[xn[i]])) % 2 == 1)) {
                resultBytes[xn[i]] -= 1;
            }
        }
        return resultBytes;
    }

    public Map<String, Double> getEmbeddingResult(byte[] init, byte[] result) {
        Map<String, Double> resultMap = new HashMap<>();
        int length;
        if (init.length == result.length) {
            length = init.length;
        } else {
            length = result.length;
        }
        int temp = 0;
        for (int i = 0; i < length; i++) {
            temp += Math.pow(Math.abs(result[i] - init[i]), 2);
        }
        double MSE = ((double) temp) / length;
        double PSNR = 10 * Math.log10(Math.pow(256, 2) / MSE);

        resultMap.put("MSE", MSE);
        resultMap.put("PSNR", PSNR);
        return resultMap;
    }

    public boolean saveKey(OutputStream outputStream, Seed seed) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bufferedWriter.write(seed.getA() + ",");
            bufferedWriter.write(seed.getB() + ",");
            bufferedWriter.write(seed.getC0() + ",");
            bufferedWriter.write(seed.getX0() + ",");
            bufferedWriter.write(seed.getLength() + ",");
            bufferedWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
