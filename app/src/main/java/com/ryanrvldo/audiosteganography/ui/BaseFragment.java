package com.ryanrvldo.audiosteganography.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument;
import androidx.activity.result.contract.ActivityResultContracts.GetContent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.ryanrvldo.audiosteganography.R;

import java.io.FileNotFoundException;

public class BaseFragment extends Fragment implements View.OnClickListener {

    public ActivityResultLauncher<String> mCreateAudioFile = registerForActivityResult(new CreateDocument(),
            result -> {
                if (result == null) {
                    showSnackbar(R.string.uri_null);
                    return;
                }
                saveAudioFileCallback(result);
            });

    public ActivityResultLauncher<String> mCreateKeyFile = registerForActivityResult(new CreateDocument(),
            result -> {
                if (result == null) {
                    showSnackbar(R.string.uri_null);
                    return;
                }
                saveKeyFileCallback(result);
            });
    private NavController navController;
    private boolean isAudio = true;
    private ActivityResultLauncher<String> mGetContent = registerForActivityResult(new GetContent(),
            result -> {
                if (result == null) {
                    showSnackbar(R.string.uri_null);
                    return;
                }
                try {
                    if (isAudio) {
                        selectAudioFileCallback(result);
                    } else {
                        selectTextFileCallback(result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });

    public BaseFragment() {
    }

    public void selectContent(@NonNull String input) {
        isAudio = input.contains("audio");
        mGetContent.launch(input);
    }

    public void selectAudioFileCallback(Uri result) throws FileNotFoundException {
    }

    public void selectTextFileCallback(Uri result) throws FileNotFoundException {
    }

    public void saveKeyFileCallback(Uri result) {
    }

    public void saveAudioFileCallback(Uri result) {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        setHasOptionsMenu(true);
    }

    @Override
    public void onClick(View view) {
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
                navController.navigate(nextNavigationId());
                return true;
            case R.id.home_menu:
                navController.popBackStack(R.id.homeFragment, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public int nextNavigationId() {
        return 0;
    }

    public void showSnackbar(int resId) {
        Snackbar.make(requireView(), resId, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.colorPrimaryLight))
                .show();
    }
}
