package com.skripsi.audiosteganography.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.skripsi.audiosteganography.R;
import com.skripsi.audiosteganography.databinding.FragmentHomeBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnEmbedMenu.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_embedFragment));
        binding.btnCompressMenu.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_compressFragment));
        binding.btnDecompressMenu.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_decompressFragment));
        binding.btnExtractMenu.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_extractFragment));
        binding.btnAboutMenu.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_aboutFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
