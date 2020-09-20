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
import com.ryanrvldo.audiosteganography.databinding.FragmentEmbedBinding;
import com.ryanrvldo.audiosteganography.model.FileData;
import com.ryanrvldo.audiosteganography.model.PseudoRandomNumber;
import com.ryanrvldo.audiosteganography.utils.FileHelper;
import com.ryanrvldo.audiosteganography.viewmodel.EmbedViewModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class EmbedFragment extends BaseFragment {

    private FragmentEmbedBinding binding;
    private EmbedViewModel viewModel;
    private FileHelper fileHelperAudio;
    private FileHelper fileHelperMessage;

    private FileData fileData;
    private Integer[] xn;
    private char[] charsMessage;
    private byte[] initBytes;
    private byte[] resultBytes;
    private double runningTime;

    public EmbedFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory())
                .get(EmbedViewModel.class);
        fileHelperAudio = new FileHelper(requireActivity());
        fileHelperMessage = new FileHelper(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmbedBinding.inflate(inflater, container, false);

        binding.btnSelectAudio.setOnClickListener(this);
        binding.btnSelectMessage.setOnClickListener(this);
        binding.btnRandom.setOnClickListener(this);
        binding.btnProcess.setOnClickListener(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel.getFileData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                this.fileData = data;
                this.initBytes = data.getFileBytes();
                binding.tvAudioPath.setText(String.format("%s.%s", this.fileData.getFileName(), this.fileData.getFileExt()));
            }
        });
        viewModel.getXnValue().observe(getViewLifecycleOwner(), xnValues -> {
            if (xnValues != null) this.xn = xnValues;
        });
    }

    @Override
    public void onClick(View v) {
        String message = "";
        if (binding.editTxtMessage.getText() != null) {
            message = binding.editTxtMessage.getText().toString();
        }

        switch (v.getId()) {
            case R.id.btn_select_message:
                selectContent("text/plain");
                break;
            case R.id.btn_select_audio:
                if (!message.equals("")) {
                    selectContent("audio/*");
                } else {
                    showSnackbar(R.string.input_message_warning);
                }
                break;
            case R.id.btn_random:
                if (fileData != null) {
                    randomSeed();
                } else {
                    showSnackbar(R.string.input_audio_warning);
                }
                break;
            case R.id.btn_process:
                if (fileData != null && !message.equals("")) {
                    if (isSeedInputted()) {
                        embedMessage();
                        showResult();
                    } else {
                        showSnackbar(R.string.input_seed_warning);
                    }
                } else {
                    showSnackbar(R.string.input_message_audio_warning);
                }
                break;
        }
    }

    @Override
    public void selectTextFileCallback(Uri result) throws FileNotFoundException {
        super.selectTextFileCallback(result);
        fileHelperMessage.setPick(result, Build.VERSION.SDK_INT);
        viewModel.setMessage(requireContext().getContentResolver().openInputStream(result));
        binding.editTxtMessage.setText(viewModel.getMessage());
        showSnackbar(R.string.read_message_success);
        binding.tvStatus.setText(getResources().getString(R.string.message_file_selected));
    }

    @Override
    public void selectAudioFileCallback(Uri result) throws FileNotFoundException {
        super.selectAudioFileCallback(result);
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
            e.printStackTrace();
            showSnackbar(R.string.failed_save_file);
        }
    }

    @Override
    public void saveKeyFileCallback(Uri result) {
        try {
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(result);
            boolean isSaved = viewModel.saveKey(outputStream, getRandomNumber());
            if (!isSaved) {
                showSnackbar(R.string.failed_save_file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showSnackbar(R.string.failed_save_file);
        }
    }

    private void randomSeed() {
        Random random = new Random();
        binding.editTxtBKey.setText(String.valueOf(random.nextInt(initBytes.length)));
        binding.editTxtAKey.setText(String.valueOf(random.nextInt(50)));
        binding.editTxtC0Key.setText(String.valueOf(random.nextInt(50)));
        binding.editTxtX0Key.setText(String.valueOf(random.nextInt(50)));
    }

    private boolean isSeedInputted() {
        return binding.editTxtAKey.getText() != null &&
                binding.editTxtBKey.getText() != null &&
                binding.editTxtC0Key.getText() != null &&
                binding.editTxtX0Key.getText() != null;
    }

    private void generateRandomNumber() {
        viewModel.setXnValue(getRandomNumber());
        binding.tvStatus.setText(getString(R.string.random_number_generated));
    }

    public PseudoRandomNumber getRandomNumber() {
        if (charsMessage == null) {
            String message = Objects.requireNonNull(binding.editTxtMessage.getText()).toString();
            charsMessage = viewModel.getBinaryMessage(message);
        }
        return new PseudoRandomNumber(
                Integer.parseInt(Objects.requireNonNull(binding.editTxtAKey.getText()).toString()),
                Integer.parseInt(Objects.requireNonNull(binding.editTxtBKey.getText()).toString()),
                Integer.parseInt(Objects.requireNonNull(binding.editTxtC0Key.getText()).toString()),
                Integer.parseInt(Objects.requireNonNull(binding.editTxtX0Key.getText()).toString()),
                charsMessage.length
        );
    }

    private void embedMessage() {
        long startTime = System.nanoTime();
        generateRandomNumber();
        resultBytes = viewModel.embedMessage(initBytes, charsMessage, xn);
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        runningTime = (double) totalTime / 1_000_000_000;

        mCreateAudioFile.launch(fileData.getFileName() + "[1]." + fileData.getFileExt());
        mCreateKeyFile.launch(fileData.getFileName() + ".key");
    }

    private void showResult() {
        Map<String, Double> result = viewModel.getEmbeddingResult(initBytes, resultBytes);
        binding.tvStatus.setText(String.format(
                getString(R.string.embed_process_finished),
                result.get("MSE"), result.get("PSNR"), runningTime));
    }

    @Override
    public int nextNavigationId() {
        return R.id.action_embed_to_compress;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
