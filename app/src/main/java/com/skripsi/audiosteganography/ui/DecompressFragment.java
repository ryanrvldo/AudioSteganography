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
import com.skripsi.audiosteganography.databinding.FragmentDecompressBinding;
import com.skripsi.audiosteganography.model.FileData;
import com.skripsi.audiosteganography.utils.FileHelper;
import com.skripsi.audiosteganography.viewmodel.DecompressViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class DecompressFragment extends Fragment implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 103;
    private static final int AUDIO_REQUEST_CODE = 203;

    private FragmentDecompressBinding binding;
    private DecompressViewModel viewModel;

    private FileHelper fileHelperAudio;

    private FileData fileData;
    private byte[] initBytes;
    private byte[] resultBytes;

    public DecompressFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDecompressBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnSelectFile.setOnClickListener(this);
        binding.btnDecompress.setOnClickListener(this);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(DecompressViewModel.class);
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
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_decompress:
                if (initBytes != null) {
                    decompressFile();
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
        Toast.makeText(getContext(), R.string.read_audio_success, Toast.LENGTH_LONG).show();
    }

    private void decompressFile() {
        long startTime = System.nanoTime();
        byte sign = '#';
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        StringBuilder charCount = new StringBuilder();
        int count = 0;
        boolean flag = false;
        for (byte current : initBytes) {
            if (!flag) {
                if (current == sign) {
                    flag = true;
                } else {
                    byteArray.write(current);
                    charCount = new StringBuilder();
                }
            } else {
                if (!Character.isDigit(current)) {
                    if (count > 0) {
                        for (int i = 0; i < count; i++) {
                            byteArray.write(current);
                        }
                        charCount = new StringBuilder();
                        count = 0;
                    } else {
                        byteArray.write(sign);
                        byteArray.write(current);
                    }
                    flag = false;
                } else {
                    charCount.append((char) current);
                    count = Integer.parseInt(charCount.toString());
                }
            }
        }
        try {
            byteArray.flush();
            resultBytes = byteArray.toByteArray();
            byteArray.close();
        } catch (IOException e) {
            Toast.makeText(getContext(), R.string.process_failed_warning + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double totalInSecond = (double) totalTime / 1_000_000_000;
        binding.tvStatus.setText(String.format(getString(R.string.decompress_process_finished), totalInSecond));
    }

    private void saveFile() {
        File path = requireContext().getExternalFilesDir(null);
        File file = new File(path, fileData.getFileName() + "[3]." + fileData.getFileExt());
        try {
            FileOutputStream output = new FileOutputStream(file);
            output.write(resultBytes);
            output.close();
            Toast.makeText(requireContext(), String.format(getString(R.string.success_save_file), file.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), R.string.failed_save_file + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_decompressFragment_to_extractFragment);
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
