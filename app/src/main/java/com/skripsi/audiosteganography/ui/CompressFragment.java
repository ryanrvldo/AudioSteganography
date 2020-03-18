package com.skripsi.audiosteganography.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.skripsi.audiosteganography.R;
import com.skripsi.audiosteganography.databinding.FragmentCompressBinding;
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

    private static final String TAG = "COMPRESS";
    private static final int PERMISSION_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE = 202;

    private FragmentCompressBinding binding;
    private CompressViewModel viewModel;

    private FileHelper fileAudio;

    private byte[] initBytes;
    private byte[] resultBytes;

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
        fileAudio = new FileHelper(getContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.getInitBytes().observe(getViewLifecycleOwner(), bytes -> {
            if (bytes != null) this.initBytes = bytes;
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_selectFile:
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_compress:
                long startTime = System.nanoTime();
                compressFile();
                saveFile();
                long endTime = System.nanoTime();
                long totalTime = endTime - startTime;
                double totalInSecond = (double) totalTime / 1_000_000_000;
                Log.d(TAG, String.format("Process compress in %f seconds\n", totalInSecond));
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
        fileAudio.setPick(uri, Build.VERSION.SDK_INT);
        viewModel.setInitBytes(requireContext().getContentResolver(), uri);
        viewModel.setFileInfo(fileAudio.getFilePath());
        String fileName = viewModel.getFileName() + "." + viewModel.getFileExt();
        binding.tvFilePath.setText(fileName);
        Toast.makeText(getContext(), "Success read file audio", Toast.LENGTH_SHORT).show();
    }

    private void compressFile() {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        StringBuilder strCount = new StringBuilder();
        byte sign = '#';
        byte current = initBytes[0];
        int count = 1;
        try {
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
            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Compression getting error", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "initFile: " + initBytes.length);
        Log.d(TAG, "compressFile: " + resultBytes.length);
    }

    private void saveFile() {
        try {
            File path = requireContext().getExternalFilesDir(null);
            File file = new File(path, viewModel.getFileName() + "[2]." + viewModel.getFileExt());
            FileOutputStream output = new FileOutputStream(file);
            output.write(resultBytes);
            output.close();
            binding.tvStatus.setText(getString(R.string.compress_completed));
            Toast.makeText(getContext(), "Success.\nFile path: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed.", Toast.LENGTH_SHORT).show();
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
