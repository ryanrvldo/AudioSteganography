package com.skripsi.audiosteganography.viewmodel;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skripsi.audiosteganography.model.FileData;

public class CompressViewModel extends ViewModel {
    private MutableLiveData<FileData> fileData = new MutableLiveData<>();
    private Repository repository;

    public CompressViewModel() {
        repository = new Repository();
    }

    public void setFileData(ContentResolver resolver, Uri uri, String filePath) {
        fileData.setValue(repository.getFileData(resolver, uri, filePath));
    }

    public LiveData<FileData> getFileData() {
        return fileData;
    }
}