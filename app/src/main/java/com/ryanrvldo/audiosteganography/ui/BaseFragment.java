package com.ryanrvldo.audiosteganography.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

public abstract class BaseFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "BaseFragment";
    protected NavController navController;
    protected ActivityResultLauncher<String> mCreateAudioFile = registerForActivityResult(new CreateDocument(),
            result -> checkOnUriResultIsNull(result, uri -> this.saveAudioFileCallback(result)));
    protected ActivityResultLauncher<String> mCreateKeyFile = registerForActivityResult(new CreateDocument(),
            result -> checkOnUriResultIsNull(result, uri -> saveKeyFileCallback(result)));
    private boolean isAudio = true;
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new GetContent(),
            result -> checkOnUriResultIsNull(result, uri -> {
                try {
                    if (isAudio) {
                        selectAudioFileCallback(result);
                        Log.d(TAG, "Uri: " + result.getPath());
                    } else {
                        selectTextFileCallback(result);
                    }
                } catch (FileNotFoundException exception) {
                    Log.d(TAG, result + " : FileNotFound", exception);
                }
            }));

    @Override
    public abstract void onClick(View view);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItemId = item.getItemId();
        if (menuItemId == R.id.next_menu) {
            navController.navigate(nextNavigationId());
            return true;
        } else if (menuItemId == R.id.home_menu) {
            navController.popBackStack(R.id.homeFragment, false);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected abstract int nextNavigationId();

    protected void selectContent(@NonNull String input) {
        isAudio = input.contains("audio");
        mGetContent.launch(input);
    }

    protected abstract void selectAudioFileCallback(Uri result) throws FileNotFoundException;

    protected void saveAudioFileCallback(Uri result) {
    }

    protected void selectTextFileCallback(Uri result) throws FileNotFoundException {
    }

    protected void saveKeyFileCallback(Uri result) {
    }

    private void checkOnUriResultIsNull(Uri result, OnUriResultIsNotNull onUriResultIsNotNull) {
        if (result == null) {
            showSnackbar(R.string.uri_null);
        } else {
            onUriResultIsNotNull.callback(result);
        }
    }

    protected void showSnackbar(int resId) {
        Snackbar.make(requireView(), resId, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.colorPrimaryLight))
                .show();
    }

    private interface OnUriResultIsNotNull {
        void callback(Uri uri);
    }
}
