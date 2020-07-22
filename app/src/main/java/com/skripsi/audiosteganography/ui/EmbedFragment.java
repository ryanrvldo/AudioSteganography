package com.skripsi.audiosteganography.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.skripsi.audiosteganography.R;
import com.skripsi.audiosteganography.databinding.FragmentEmbedBinding;
import com.skripsi.audiosteganography.model.FileData;
import com.skripsi.audiosteganography.model.PseudoRandomNumber;
import com.skripsi.audiosteganography.utils.FileHelper;
import com.skripsi.audiosteganography.viewmodel.EmbedViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmbedFragment extends Fragment implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int AUDIO_REQUEST_CODE = 201;
    private static final int MESSAGE_REQUEST_CODE = 301;

    private FragmentEmbedBinding binding;
    private EmbedViewModel viewModel;
    private FileHelper fileHelperAudio;
    private FileHelper fileHelperMessage;

    private FileData fileData;
    private Integer[] xn;
    private char[] charsMessage;
    private byte[] bytesAudio;
    private byte[] initBytes;
    private double runningTime;

    public EmbedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmbedBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnSelectAudio.setOnClickListener(this);
        binding.btnSelectMessage.setOnClickListener(this);
        binding.btnRandom.setOnClickListener(this);
        binding.btnProcess.setOnClickListener(this);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(EmbedViewModel.class);
        fileHelperAudio = new FileHelper(requireActivity());
        fileHelperMessage = new FileHelper(requireActivity());

        viewModel.getFileData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                this.fileData = data;
                this.bytesAudio = data.getFileBytes();
                binding.tvAudioPath.setText(String.format("%s.%s", this.fileData.getFileName(), this.fileData.getFileExt()));
            }
        });
        viewModel.getXnValue().observe(getViewLifecycleOwner(), xnValues -> {
            if (xnValues != null) this.xn = xnValues;
        });
    }

    @Override
    public void onClick(View v) {
        String message = Optional.ofNullable(binding.editTxtMessage.getText())
                .map(CharSequence::toString)
                .orElse("");

        switch (v.getId()) {
            case R.id.btn_select_message:
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFileText();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_select_audio:
                if (!message.equals("")) {
                    selectFileAudio();
                } else {
                    Toast.makeText(requireContext(), R.string.input_message_warning, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_random:
                if (fileData != null) {
                    randomSeed();
                } else {
                    Toast.makeText(requireContext(), R.string.input_audio_warning, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_process:
                if (fileData != null && !message.equals("")) {
                    if (binding.editTxtAKey.getText() != null &&
                            binding.editTxtBKey.getText() != null &&
                            binding.editTxtC0Key.getText() != null &&
                            binding.editTxtX0Key.getText() != null) {
                        long startTime = System.nanoTime();
                        generateRandomNumber();
                        embedData();
                        long endTime = System.nanoTime();
                        long totalTime = endTime - startTime;
                        runningTime = (double) totalTime / 1_000_000_000;
                        showResult();
                    } else {
                        Toast.makeText(requireContext(), R.string.input_seed_warning, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.input_message_audio_warning, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void selectFileAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, AUDIO_REQUEST_CODE);
    }

    private void selectFileText() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        startActivityForResult(intent, MESSAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUDIO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                readFileAudio(data.getData());
                binding.tvStatus.setText(getResources().getString(R.string.audio_file_selected));
            }
        }
        if (requestCode == MESSAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                readFileMessage(data.getData());
                binding.tvStatus.setText(getResources().getString(R.string.message_file_selected));
            }
        }
    }

    private void readFileAudio(Uri uri) {
        fileHelperAudio.setPick(uri, Build.VERSION.SDK_INT);
        viewModel.setFileData(requireContext().getContentResolver(), uri, fileHelperAudio.getFilePath());
        Toast.makeText(getContext(), R.string.read_audio_success, Toast.LENGTH_LONG).show();
    }

    private void readFileMessage(Uri uri) {
        fileHelperMessage.setPick(uri, Build.VERSION.SDK_INT);
        viewModel.setMessage(requireContext().getContentResolver(), uri);
        binding.editTxtMessage.setText(viewModel.getMessage());
        Toast.makeText(getContext(), R.string.read_message_success, Toast.LENGTH_LONG).show();
    }

    private void randomSeed() {
        Random random = new Random();
        int b = random.nextInt(bytesAudio.length);
        binding.editTxtBKey.setText(String.valueOf(b));
        binding.editTxtAKey.setText(String.valueOf(random.nextInt(1000)));
        binding.editTxtC0Key.setText(String.valueOf(random.nextInt(1000)));
        binding.editTxtX0Key.setText(String.valueOf(random.nextInt(1000)));
    }

    private void generateRandomNumber() {
        String message = Objects.requireNonNull(binding.editTxtMessage.getText()).toString();
        charsMessage = viewModel.getCharMessage(message);
        PseudoRandomNumber randomNumber = new PseudoRandomNumber(
                Integer.parseInt(Objects.requireNonNull(binding.editTxtAKey.getText()).toString()),
                Integer.parseInt(Objects.requireNonNull(binding.editTxtBKey.getText()).toString()),
                Integer.parseInt(Objects.requireNonNull(binding.editTxtC0Key.getText()).toString()),
                Integer.parseInt(Objects.requireNonNull(binding.editTxtX0Key.getText()).toString()),
                charsMessage.length
        );
        viewModel.setXnValue(randomNumber);
        viewModel.saveKey(requireContext(), randomNumber);
        binding.tvStatus.setText(getString(R.string.random_number_generated));
    }

    private void embedData() {
        initBytes = bytesAudio.clone();
        int length = charsMessage.length;
        for (int i = 0; i < length; i++) {
            if (charsMessage[i] == '1' && ((Math.abs(bytesAudio[xn[i]])) % 2 == 0)) {
                bytesAudio[xn[i]] += 1;
            } else if (charsMessage[i] == '0' && ((Math.abs(bytesAudio[xn[i]])) % 2 == 1)) {
                bytesAudio[xn[i]] -= 1;
            }
        }

        File path = requireContext().getExternalFilesDir(null);
        File file = new File(path, fileData.getFileName() + "[1]." + fileData.getFileExt());

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(bytesAudio);
            outputStream.flush();

            FileOutputStream output = new FileOutputStream(file);
            output.write(outputStream.toByteArray());
            output.close();
            outputStream.close();
            Toast.makeText(getContext(), String.format(getString(R.string.success_save_file), file.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), R.string.failed_save_file + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showResult() {
        int length;
        int temp = 0;
        BigInteger mseBig;
        BigInteger psnrBig;
        if (initBytes.length == bytesAudio.length) {
            length = initBytes.length;
        } else {
            length = bytesAudio.length;
        }
        for (int i = 0; i < length; i++) {
            temp += Math.pow(Math.abs(bytesAudio[i] - initBytes[i]), 2);
        }
        mseBig = BigInteger.valueOf(temp);
        double MSE = (mseBig.doubleValue() / length);
        psnrBig = BigInteger.valueOf(255);
        psnrBig = psnrBig.pow(2);
        double PSNR = 10 * Math.log10(psnrBig.doubleValue() / MSE);
        binding.tvStatus.setText(String.format(
                getString(R.string.embed_process_finished),
                MSE,
                PSNR,
                runningTime));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFileText();
            } else {
                Toast.makeText(requireContext(), R.string.permission_denied_warning, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next_menu:
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_embedFragment_to_compressFragment);
                return true;
            case R.id.home_menu:
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
