package com.skripsi.audiosteganography.ui;

import android.Manifest;
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
import com.skripsi.audiosteganography.databinding.FragmentExtractBinding;
import com.skripsi.audiosteganography.model.FileData;
import com.skripsi.audiosteganography.model.PseudoRandomNumber;
import com.skripsi.audiosteganography.utils.FileHelper;
import com.skripsi.audiosteganography.viewmodel.ExtractViewModel;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExtractFragment extends Fragment implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 104;
    private static final int AUDIO_REQUEST_CODE = 204;
    private static final int KEY_REQUEST_CODE = 304;

    private FragmentExtractBinding binding;
    private ExtractViewModel viewModel;
    private FileHelper fileHelperAudio;

    private FileData fileData;
    private byte[] bytesAudio;
    private Integer[] xn;
    private PseudoRandomNumber key;

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
        fileHelperAudio = new FileHelper(requireActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFileAudio();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
                break;
            case R.id.btn_select_key:
                if (bytesAudio != null) {
                    selectFileKey();
                } else {
                    Toast.makeText(requireContext(), R.string.input_audio_warning, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_extract:
                if (key != null) {
                    extract();
                } else {
                    Toast.makeText(getContext(), R.string.input_key_warning, Toast.LENGTH_SHORT).show();
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
        if (requestCode == AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                readAudioFile(data.getData());
                binding.tvStatus.setText(getResources().getString(R.string.audio_file_selected));
            }
        }
        if (requestCode == KEY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                readKeyFile(data.getData());
                binding.tvStatus.setText(R.string.key_file_selected);
            }
        }
    }

    private void readAudioFile(Uri uri) {
        fileHelperAudio.setPick(uri, Build.VERSION.SDK_INT);
        viewModel.setFileData(requireContext().getContentResolver(), uri, fileHelperAudio.getFilePath());
        Toast.makeText(requireContext(), R.string.read_audio_success, Toast.LENGTH_SHORT).show();
    }

    private void readKeyFile(Uri uri) {
        viewModel.setKey(requireContext().getContentResolver(), uri);
        showKey();
        Toast.makeText(requireContext(), R.string.read_key_success, Toast.LENGTH_SHORT).show();
    }

    private void showKey() {
        key = viewModel.getKey();
        binding.editTxtAKey.setText(String.valueOf(key.getA()));
        binding.editTxtBKey.setText(String.valueOf(key.getB()));
        binding.editTxtC0Key.setText(String.valueOf(key.getC0()));
        binding.editTxtX0Key.setText(String.valueOf(key.getX0()));
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

        String[] messageArray = viewModel.generateBinaryToString(builder.toString());
        StringBuilder messageBuilder = new StringBuilder();
        for (String message : messageArray) {
            messageBuilder.append((char) Integer.parseInt(message, 2));
        }
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double totalInSecond = (double) totalTime / 1_000_000_000;
        binding.editTxtMessage.setText(messageBuilder.toString());
        binding.tvStatus.setText(String.format(getString(R.string.extraction_process_finished), totalInSecond));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFileAudio();
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
        if (item.getItemId() == R.id.home_menu) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).popBackStack(R.id.homeFragment, false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.next_menu).setVisible(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
