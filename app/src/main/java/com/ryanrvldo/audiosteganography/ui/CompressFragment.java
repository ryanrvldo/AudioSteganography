package com.ryanrvldo.audiosteganography.ui;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ryanrvldo.audiosteganography.R;
import com.ryanrvldo.audiosteganography.databinding.FragmentCompressBinding;
import com.ryanrvldo.audiosteganography.model.FileData;
import com.ryanrvldo.audiosteganography.utils.FileHelper;
import com.ryanrvldo.audiosteganography.viewmodel.CompressViewModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class CompressFragment extends BaseFragment {

    private FragmentCompressBinding binding;
    private CompressViewModel viewModel;

    private FileHelper fileHelperAudio;
    private FileData fileData;

    private byte[] initBytes;
    private byte[] resultBytes;
    private double runningTime;

    public CompressFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnSelectFile.setOnClickListener(this);
        binding.btnCompress.setOnClickListener(this);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(CompressViewModel.class);
        fileHelperAudio = new FileHelper(requireActivity());

        viewModel.getFileData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                this.fileData = data;
                this.initBytes = data.getFileBytes();
                binding.tvFilePath.setText(String.format("%s.%s", fileData.getFileName(), fileData.getFileExt()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_file:
                selectContent("audio/*");
                break;
            case R.id.btn_compress:
                if (initBytes != null) {
                    compressFile();
                    mCreateAudioFile.launch(fileData.getFileName() + "[2]." + fileData.getFileExt());
                } else {
                    showSnackbar(R.string.input_audio_warning);
                }
                break;
        }
    }

    @Override
    public void selectAudioFileCallback(Uri result) throws FileNotFoundException {
        fileHelperAudio.setPick(result, Build.VERSION.SDK_INT);
        viewModel.setFileData(requireContext().getContentResolver().openInputStream(result), fileHelperAudio.getFilePath());
        showSnackbar(R.string.read_audio_success);
        binding.tvStatus.setText(R.string.audio_file_selected);
    }

    @Override
    public void saveAudioFileCallback(Uri result) {
        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(result)) {
            if (outputStream != null) {
                outputStream.write(resultBytes);
            }
            showSnackbar(R.string.success_save_file);
        } catch (IOException e) {
            showSnackbar(R.string.failed_save_file);
        }
    }

    private void compressFile() {
        long startTime = System.nanoTime();
        resultBytes = viewModel.compressFile(initBytes);
        long totalTime = System.nanoTime() - startTime;

        if (resultBytes != null) {
            runningTime = (double) totalTime / 1_000_000_000;
            showResult();
        } else {
            showSnackbar(R.string.process_failed_warning);
        }
    }

    private void showResult() {
        double CR = ((double) initBytes.length) / resultBytes.length;
        double SS = (1 - ((double) resultBytes.length / initBytes.length)) * 100;
        double BR = (double) resultBytes.length / 16;
        binding.tvStatus.setText(String.format(getString(R.string.compress_process_finished), CR, SS, BR, runningTime));
    }

    @Override
    public int nextNavigationId() {
        return R.id.action_compress_to_decompress;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}