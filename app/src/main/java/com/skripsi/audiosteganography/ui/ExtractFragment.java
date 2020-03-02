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
import com.skripsi.audiosteganography.databinding.FragmentExtractBinding;
import com.skripsi.audiosteganography.utils.FileHelper;
import com.skripsi.audiosteganography.viewmodel.ExtractViewModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExtractFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "EXTRACT";
    private static final int PERMISSION_REQUEST_CODE = 104;
    private static final int AUDIO_REQUEST_CODE = 204;
    private static final int KEY_REQUEST_CODE = 304;

    private FragmentExtractBinding binding;
    private ExtractViewModel viewModel;
    private FileHelper fileAudio;

    private byte[] bytesAudio;
    private Integer[] xnValue;
    private int[] key;

    public ExtractFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentExtractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnSelectAudio.setOnClickListener(this);
        binding.btnSelectKey.setOnClickListener(this);
        binding.btnExtract.setOnClickListener(this);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(ExtractViewModel.class);
        fileAudio = new FileHelper(getContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.getBytesAudio().observe(getViewLifecycleOwner(), bytes -> {
            if (bytes != null) this.bytesAudio = bytes;
        });
        viewModel.getXnValue().observe(getViewLifecycleOwner(), xnValues -> {
            if (xnValues != null) this.xnValue = xnValues;
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_audio:
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFileAudio();
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_select_key:
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFileKey();
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_extract:
                if (key != null) {
                    extract();
                } else {
                    Toast.makeText(getContext(), "Enter the key first!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void selectFileAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, AUDIO_REQUEST_CODE);
    }

    private void selectFileKey() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, KEY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUDIO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                fileAudio.setPick(data.getData(), Build.VERSION.SDK_INT);
                viewModel.setFileInfo(fileAudio.getFilePath());
                readFileAudio(data.getData());
                String fileName = viewModel.getFileName() + "." + viewModel.getFileExt();
                binding.tvAudioPath.setText(fileName);
            }
        }
        if (requestCode == KEY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                readFileKey(data.getData());
                setKey();
            }
        }
    }

    private void readFileAudio(Uri uri) {
        if (getContext() != null) {
            viewModel.setBytesAudio(getContext().getContentResolver(), uri);
            Toast.makeText(getContext(), "Success read file audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFileKey(Uri uri) {
        if (getContext() != null) {
            viewModel.setKey(getContext().getContentResolver(), uri);
            Toast.makeText(getContext(), "Success read file message", Toast.LENGTH_SHORT).show();
        }
    }

    private void setKey() {
        key = viewModel.getKey();
        viewModel.setXnValue(key[4], key[0], key[1], key[2], key[3]);
        binding.aEdit.setText(String.valueOf(key[0]));
        binding.bEdit.setText(String.valueOf(key[1]));
        binding.c0Edit.setText(String.valueOf(key[2]));
        binding.x0Edit.setText(String.valueOf(key[3]));
    }

    private void extract() {
        StringBuilder builder = new StringBuilder();
        long startTime = System.nanoTime();
        for (int i = key[4] - 1; i >= 0; i--) {
            if ((Math.abs(bytesAudio[xnValue[i]])) % 2 == 0) {
                builder.append('0');
            } else if ((Math.abs(bytesAudio[xnValue[i]])) % 2 == 1) {
                builder.append('1');
            }
        }
        long endTime = System.nanoTime();

        String[] messageArray = viewModel.generateBinaryToMessage(builder.reverse().toString());
        StringBuilder messageBuilder = new StringBuilder();
        for (String message : messageArray) {
            messageBuilder.append((char) Integer.parseInt(message, 2));
        }
        long totalTime = endTime - startTime;
        double totalInSecond = (double) totalTime / 1_000_000_000;
        binding.editMessage.setText(messageBuilder.toString());
        Log.d(TAG, String.format("Process finished in %f seconds", totalInSecond));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home_menu) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.next_menu).setVisible(false);
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
