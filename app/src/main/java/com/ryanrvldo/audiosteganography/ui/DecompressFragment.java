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
import com.ryanrvldo.audiosteganography.databinding.FragmentDecompressBinding;
import com.ryanrvldo.audiosteganography.model.FileData;
import com.ryanrvldo.audiosteganography.utils.FileHelper;
import com.ryanrvldo.audiosteganography.viewmodel.DecompressViewModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class DecompressFragment extends BaseFragment {

    private FragmentDecompressBinding binding;
    private DecompressViewModel viewModel;

    private FileHelper fileHelperAudio;

    private FileData fileData;
    private byte[] initBytes;
    private byte[] resultBytes;

    public DecompressFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDecompressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnSelectFile.setOnClickListener(this);
        binding.btnDecompress.setOnClickListener(this);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory())
                .get(DecompressViewModel.class);
        fileHelperAudio = new FileHelper(requireActivity());

        viewModel.getFileData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                this.fileData = data;
                this.initBytes = data.getFileBytes();
                binding.tvFilePath.setText(String.format("%s.%s", data.getFileName(), data.getFileExt()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_file:
                selectContent("audio/*");
                break;
            case R.id.btn_decompress:
                if (initBytes != null) {
                    decompressFile();
                    mCreateAudioFile.launch(fileData.getFileName() + "[3]." + fileData.getFileExt());
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
        binding.tvStatus.setText(getResources().getString(R.string.audio_file_selected));
    }

    @Override
    public void saveAudioFileCallback(Uri result) {
        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(result)) {
            if (outputStream != null) {
                outputStream.write(resultBytes);
                showSnackbar(R.string.success_save_file);
            }
        } catch (IOException e) {
            showSnackbar(R.string.failed_save_file);
        }
    }

    private void decompressFile() {
        long startTime = System.nanoTime();
        resultBytes = viewModel.decompressFile(initBytes);
        if (resultBytes != null) {
            long totalTime = System.nanoTime() - startTime;
            double totalInSecond = (double) totalTime / 1_000_000_000;
            binding.tvStatus.setText(String.format(getString(R.string.decompress_process_finished), totalInSecond));
        } else {
            showSnackbar(R.string.process_failed_warning);
        }
    }

    @Override
    public int nextNavigationId() {
        return R.id.action_decompress_to_extract;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
