package com.ryanrvldo.audiosteganography.ui;

import android.annotation.SuppressLint;
import android.net.Uri;
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
import com.ryanrvldo.audiosteganography.model.Seed;
import com.ryanrvldo.audiosteganography.viewmodel.EmbedViewModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Random;

public class EmbedFragment extends BaseFragment {

    private FragmentEmbedBinding binding;
    private EmbedViewModel viewModel;

    private FileData fileData;
    private Seed seedValue;
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        String message = binding.editTxtMessage.getText().toString();

        int viewId = v.getId();
        switch (viewId) {
            case R.id.btn_select_message:
                selectContent("text/plain");
                break;
            case R.id.btn_select_audio:
                if (message.trim().isEmpty()) {
                    showSnackbar(R.string.input_message_warning);
                    break;
                }
                selectContent("audio/*");
                break;
            case R.id.btn_random:
                if (fileData == null) {
                    showSnackbar(R.string.input_audio_warning);
                    break;
                }
                randomSeed();
                break;
            case R.id.btn_process:
                if (fileData == null || message.trim().isEmpty()) {
                    showSnackbar(R.string.input_message_audio_warning);
                    break;
                }
                if (!isSeedInputted()) {
                    showSnackbar(R.string.input_seed_warning);
                    break;
                }
                embedMessage();
                showResult();
                break;
        }
    }

    @Override
    public void selectTextFileCallback(Uri result) throws FileNotFoundException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(result);
        viewModel.setMessage(inputStream);

        String message = viewModel.getMessage();
        if (message == null || message.isEmpty()) {
            showSnackbar(R.string.failed_read_file);
            return;
        }
        binding.editTxtMessage.setText(message);
        showSnackbar(R.string.read_message_success);
        binding.tvStatus.setText(getString(R.string.message_file_selected));
    }

    @Override
    public void selectAudioFileCallback(Uri result) throws FileNotFoundException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(result);
        viewModel.setFileData(inputStream, result.getPath());
        if (fileData != null) {
            showSnackbar(R.string.read_audio_success);
            binding.tvStatus.setText(getResources().getString(R.string.audio_file_selected));
        }
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
        boolean isSaved = false;
        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(result)) {
            isSaved = viewModel.saveKey(outputStream, getSeedValue());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!isSaved) {
                showSnackbar(R.string.failed_save_file);
            }
        }
    }

    private void randomSeed() {
        Random random = new Random();
        setCharsMessage();
        seedValue = new Seed(
                random.nextInt(50),
                random.nextInt(initBytes.length),
                random.nextInt(50),
                random.nextInt(50),
                charsMessage.length
        );
        binding.editTxtAKey.setText(String.valueOf(seedValue.getA()));
        binding.editTxtBKey.setText(String.valueOf(seedValue.getB()));
        binding.editTxtC0Key.setText(String.valueOf(seedValue.getC0()));
        binding.editTxtX0Key.setText(String.valueOf(seedValue.getX0()));
    }

    private boolean isSeedInputted() {
        return !binding.editTxtAKey.getText().toString().isEmpty() &&
                !binding.editTxtBKey.getText().toString().isEmpty() &&
                !binding.editTxtC0Key.getText().toString().isEmpty() &&
                !binding.editTxtX0Key.getText().toString().isEmpty();
    }

    private void generateRandomNumber() {
        viewModel.setXnValue(getSeedValue());
        binding.tvStatus.setText(getString(R.string.random_number_generated));
    }

    private Seed getSeedValue() {
        if (seedValue != null) {
            return seedValue;
        }
        setCharsMessage();

        return new Seed(
                Integer.parseInt(String.valueOf(binding.editTxtAKey.getText())),
                Integer.parseInt(String.valueOf(binding.editTxtBKey.getText())),
                Integer.parseInt(String.valueOf(binding.editTxtC0Key.getText())),
                Integer.parseInt(String.valueOf(binding.editTxtX0Key.getText())),
                charsMessage.length
        );
    }

    private void setCharsMessage() {
        if (charsMessage == null) {
            String message = binding.editTxtMessage.getText().toString();
            charsMessage = viewModel.getBinaryMessage(message);
        }
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
