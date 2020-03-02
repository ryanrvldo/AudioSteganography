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
import com.skripsi.audiosteganography.databinding.FragmentDecompressBinding;
import com.skripsi.audiosteganography.utils.FileHelper;
import com.skripsi.audiosteganography.viewmodel.DecompressViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class DecompressFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "DECOMPRESS";
    private static final int PERMISSION_REQUEST_CODE = 103;
    private static final int AUDIO_REQUEST_CODE = 203;

    private FragmentDecompressBinding binding;
    private DecompressViewModel viewModel;

    private FileHelper fileAudio;

    private byte[] bytesAudio;
    private byte[] bytesAudioCompressed;

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
        fileAudio = new FileHelper(getContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.getBytesAudioCompressed().observe(getViewLifecycleOwner(), bytes -> {
            if (bytes != null) bytesAudioCompressed = bytes;
        });

        viewModel.getBytesAudio().observe(getViewLifecycleOwner(), bytes -> {
            if (bytes != null) bytesAudio = bytes;
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
            case R.id.btn_decompress:
                long startTime = System.nanoTime();
                viewModel.setBytesAudio(decompressFile());
                saveFile();
                long endTime = System.nanoTime();
                long totalTime = endTime - startTime;
                double totalInSecond = (double) totalTime / 1_000_000_000;
                binding.tvStatus.setText(R.string.decompress_completed);
                Log.d(TAG, String.format("Process decompress file in %f seconds\n", totalInSecond));
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
        viewModel.setFileInfo("");
        fileAudio.setPick(uri, Build.VERSION.SDK_INT);
        viewModel.setBytesAudioCompressed(requireContext().getContentResolver(), uri);
        viewModel.setFileInfo(fileAudio.getFilePath());
        String fileName = viewModel.getFileName() + "." + viewModel.getFileExt();
        binding.tvFilePath.setText(fileName);
        Toast.makeText(getContext(), "Success read file audio", Toast.LENGTH_LONG).show();
    }

    private byte[] decompressFile() {
        byte sign = '~';
        byte length;
        int len;
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        for (int i = 0; i < bytesAudioCompressed.length; i++) {
            byte current = bytesAudioCompressed[i];
            Log.d(TAG, "current: " + current);
            if ((current == sign) && (bytesAudioCompressed[i + 1] > '2') && (bytesAudioCompressed[i + 1] < '9')) {
                length = bytesAudioCompressed[i + 1];
                Log.d(TAG, "length: " + length);
                len = Integer.parseInt(String.valueOf((char) length));
                Log.d(TAG, "len: " + len);
                current = bytesAudioCompressed[i + 2];
                Log.d(TAG, "current: " + current);
                for (int j = 0; j < len; j++) {
                    byteArray.write(current);
                }
                i += 2;
            } else {
                byteArray.write(current);
                Log.d(TAG, "current: " + current);
            }
        }
        try {
            byteArray.flush();
            byteArray.close();
            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Decompression getting error", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "byteDecompressed: " + Arrays.toString(byteArray.toByteArray()));
        return byteArray.toByteArray();
    }

    private void saveFile() {
        if (getContext() != null) {
            File path = getContext().getExternalFilesDir(null);
            File file = new File(path, viewModel.getFileName() + "[3]." + viewModel.getFileExt());
            try {
                FileOutputStream output = new FileOutputStream(file);
                output.write(bytesAudio);
                output.close();
                Toast.makeText(getContext(), "Success.\nFile path: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed.", Toast.LENGTH_LONG).show();
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
