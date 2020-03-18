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
import com.skripsi.audiosteganography.databinding.FragmentEmbedBinding;
import com.skripsi.audiosteganography.utils.FileHelper;
import com.skripsi.audiosteganography.viewmodel.EmbedViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmbedFragment extends Fragment implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int AUDIO_REQUEST_CODE = 201;
    private static final int MESSAGE_REQUEST_CODE = 301;
    private static final String TAG = "EMBED";

    private FragmentEmbedBinding binding;
    private EmbedViewModel viewModel;
    private FileHelper fileAudio;
    private FileHelper fileMessage;

    private byte[] dataAudio;
    private byte[] headerAudio;
    private Integer[] xn;
    private char[] charsMessage;

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
        binding.btnGenerate.setOnClickListener(this);
        binding.btnProcess.setOnClickListener(this);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(EmbedViewModel.class);
        fileAudio = new FileHelper(getContext());
        fileMessage = new FileHelper(getContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.getHeaderAudio().observe(getViewLifecycleOwner(), bytes -> {
            if (bytes != null) this.headerAudio = bytes;
        });
        viewModel.getDataAudio().observe(getViewLifecycleOwner(), bytes -> {
            if (bytes != null) this.dataAudio = bytes;
        });
        viewModel.getXnValue().observe(getViewLifecycleOwner(), xnValues -> {
            if (xnValues != null) this.xn = xnValues;
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_selectAudio:
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFileAudio();
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_selectMessage:
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFileText();
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_random:
                randomMWC();
                break;
            case R.id.btn_generate:
                int a = Integer.parseInt(binding.aEdit.getText().toString());
                int b = Integer.parseInt(binding.bEdit.getText().toString());
                int c0 = Integer.parseInt(binding.c0Edit.getText().toString());
                int x0 = Integer.parseInt(binding.x0Edit.getText().toString());
                String message = binding.editMessage.getText().toString();
                charsMessage = viewModel.getCharMessage(message);
                viewModel.setXnValue(charsMessage.length, a, b, c0, x0);
                binding.tvStatus.setText(getString(R.string.random_number_generated));
                break;
            case R.id.btn_process:
                embedData();
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
        fileAudio.setPick(uri, Build.VERSION.SDK_INT);
        viewModel.setBytesAudio(requireContext().getContentResolver(), uri);
        viewModel.setFileInfo(fileAudio.getFilePath());
        String fileName = viewModel.getFileName() + "." + viewModel.getFileExt();
        binding.tvAudioPath.setText(fileName);
        Toast.makeText(getContext(), "Success read file audio", Toast.LENGTH_LONG).show();
    }

    private void readFileMessage(Uri uri) {
        fileMessage.setPick(uri, Build.VERSION.SDK_INT);
        viewModel.setMessage(requireContext().getContentResolver(), uri);
        binding.editMessage.setText(viewModel.getMessage());
        Toast.makeText(getContext(), "Success read file message", Toast.LENGTH_LONG).show();
    }

    private void randomMWC() {
        Random random = new Random();
        binding.aEdit.setText(String.valueOf(random.nextInt()));
        binding.bEdit.setText(String.valueOf(random.nextInt()));
        binding.c0Edit.setText(String.valueOf(random.nextInt()));
        binding.x0Edit.setText(String.valueOf(random.nextInt()));
    }

    private void embedData() {
        long startTime = System.nanoTime();
        for (int i = 0; i < charsMessage.length; i++) {
            if (charsMessage[i] == '1' && ((Math.abs(dataAudio[xn[i]])) % 2 == 0)) {
                dataAudio[xn[i]] += 1;
            } else if (charsMessage[i] == '0' && ((Math.abs(dataAudio[xn[i]])) % 2 == 1)) {
                dataAudio[xn[i]] -= 1;
            }
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double totalInSecond = (double) totalTime / 1_000_000_000;
        Log.d(TAG, String.format("Process finished in %f seconds\n", totalInSecond));

        File path = requireContext().getExternalFilesDir(null);
        File file = new File(path, viewModel.getFileName() + "[1]." + viewModel.getFileExt());

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(headerAudio);
            outputStream.write(dataAudio);
            outputStream.flush();

            FileOutputStream output = new FileOutputStream(file);
            output.write(outputStream.toByteArray());
            output.close();
            outputStream.close();
            Toast.makeText(getContext(), "Success.\nFile path: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save file!", Toast.LENGTH_LONG).show();
        }
        viewModel.saveKey(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
}
