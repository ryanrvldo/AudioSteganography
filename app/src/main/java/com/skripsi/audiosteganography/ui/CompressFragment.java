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
import com.skripsi.audiosteganography.databinding.FragmentCompressBinding;
import com.skripsi.audiosteganography.model.FileData;
import com.skripsi.audiosteganography.utils.FileHelper;
import com.skripsi.audiosteganography.viewmodel.CompressViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class CompressFragment extends Fragment implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE = 202;

    private FragmentCompressBinding binding;
    private CompressViewModel viewModel;

    private FileHelper fileHelperAudio;

    private FileData fileData;
    private byte[] initBytes;
    private byte[] resultBytes;
    private double runningTime;

    public CompressFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCompressBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
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
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_compress:
                if (initBytes != null) {
                    compressFile();
                    saveFile();
                } else {
                    Toast.makeText(requireContext(), R.string.input_audio_warning, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, AUDIO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUDIO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                readFileAudio(data.getData());
                binding.tvStatus.setText(getResources().getString(R.string.audio_file_selected));
            }
        }
    }

    private void readFileAudio(Uri uri) {
        fileHelperAudio.setPick(uri, Build.VERSION.SDK_INT);
        viewModel.setFileData(requireContext().getContentResolver(), uri, fileHelperAudio.getFilePath());
        Toast.makeText(getContext(), R.string.read_audio_success, Toast.LENGTH_SHORT).show();
    }

    private void compressFile() {
        long startTime = System.nanoTime();
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
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
            resultBytes = byteArray.toByteArray();
            byteArray.close();
        } catch (IOException e) {
            Toast.makeText(getContext(), getString(R.string.process_failed_warning) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        runningTime = (double) totalTime / 1_000_000_000;
        showResult();
    }

    private void saveFile() {
        try {
            File path = requireContext().getExternalFilesDir(null);
            File file = new File(path, fileData.getFileName() + "[2]." + fileData.getFileExt());
            FileOutputStream output = new FileOutputStream(file);
            output.write(resultBytes);
            output.close();
            Toast.makeText(requireContext(), String.format(getString(R.string.success_save_file), file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), getString(R.string.failed_save_file) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showResult() {
        double CR = ((double) initBytes.length) / resultBytes.length;
        double SS = (1 - ((double) resultBytes.length / initBytes.length)) * 100;
        double BR = (double) resultBytes.length / 16;
        binding.tvStatus.setText(String.format(getString(R.string.compress_process_finished), CR, SS, BR, runningTime));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFile();
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
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_compressFragment_to_decompressFragment);
                return true;
            case R.id.home_menu:
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false);
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
