package com.ryanrvldo.audiosteganography.ui;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ryanrvldo.audiosteganography.R;
import com.ryanrvldo.audiosteganography.databinding.FragmentExtractBinding;
import com.ryanrvldo.audiosteganography.model.FileData;
import com.ryanrvldo.audiosteganography.model.PseudoRandomNumber;
import com.ryanrvldo.audiosteganography.utils.FileHelper;
import com.ryanrvldo.audiosteganography.viewmodel.ExtractViewModel;

import java.io.FileNotFoundException;

public class ExtractFragment extends BaseFragment {

    private FragmentExtractBinding binding;
    private ExtractViewModel viewModel;
    private FileHelper fileHelperAudio;

    private FileData fileData;
    private byte[] bytesAudio;
    private Integer[] xn;
    private PseudoRandomNumber key;

    public ExtractFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory())
                .get(ExtractViewModel.class);
        fileHelperAudio = new FileHelper(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentExtractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnSelectAudio.setOnClickListener(this);
        binding.btnSelectKey.setOnClickListener(this);
        binding.btnExtract.setOnClickListener(this);

        viewModel.getFileData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                this.fileData = data;
                this.bytesAudio = data.getFileBytes();
                binding.tvAudioPath.setText(String.format("%s.%s", fileData.getFileName(), fileData.getFileExt()));
            }
        });

        viewModel.getXnValue().observe(getViewLifecycleOwner(), xnValues -> {
            if (xnValues != null) this.xn = xnValues;
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_audio:
                selectContent("audio/*");
                break;
            case R.id.btn_select_key:
                if (bytesAudio != null) {
                    selectContent("*/*");
                } else {
                    showSnackbar(R.string.input_audio_warning);
                }
                break;
            case R.id.btn_extract:
                if (key != null) {
                    extract();
                } else {
                    showSnackbar(R.string.input_key_warning);
                }
                break;
        }
    }

    @Override
    public void selectAudioFileCallback(Uri result) throws FileNotFoundException {
        fileHelperAudio.setPick(result, Build.VERSION.SDK_INT);
        viewModel.setFileData(requireContext().getContentResolver().openInputStream(result), fileHelperAudio.getFilePath());
        showSnackbar(R.string.read_audio_success);
        binding.tvStatus.setText(getString(R.string.audio_file_selected));
    }

    @Override
    public void selectTextFileCallback(Uri result) throws FileNotFoundException {
        viewModel.setKey(requireContext().getContentResolver().openInputStream(result));
        showKey();
    }

    private void showKey() {
        key = viewModel.getKey();
        if (key == null) {
            showSnackbar(R.string.process_failed_warning);
            binding.tvStatus.setText(R.string.failed_read_key);
            return;
        }
        binding.editTxtAKey.setText(String.valueOf(key.getA()));
        binding.editTxtBKey.setText(String.valueOf(key.getB()));
        binding.editTxtC0Key.setText(String.valueOf(key.getC0()));
        binding.editTxtX0Key.setText(String.valueOf(key.getX0()));
        showSnackbar(R.string.read_key_success);
        binding.tvStatus.setText(R.string.key_file_selected);
    }

    private void extract() {
        long startTime = System.nanoTime();
        viewModel.setXnValue();
        StringBuilder builder = new StringBuilder();
        int length = key.getLength();
        for (int i = 0; i < length; i++) {
            if ((Math.abs(bytesAudio[xn[i]])) % 2 == 0) {
                builder.append('0');
            } else if ((Math.abs(bytesAudio[xn[i]])) % 2 == 1) {
                builder.append('1');
            }
        }

        String message = viewModel.generateBinaryToString(builder.toString());

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double totalInSecond = (double) totalTime / 1_000_000_000;
        binding.editTxtMessage.setText(message);
        binding.tvStatus.setText(String.format(getString(R.string.extraction_process_finished), totalInSecond));
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.next_menu).setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}