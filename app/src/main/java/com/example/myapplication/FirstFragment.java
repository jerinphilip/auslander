package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

import com.github.jerinphilip.slimt.Model;
import com.github.jerinphilip.slimt.Service;
import com.github.jerinphilip.slimt.Package;
import com.github.jerinphilip.slimt.ModelConfig;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        String appData = context.getFilesDir().getAbsolutePath();
        String root = Paths.get(appData, "English-German tiny", "ende.student.tiny11").toString();
        int encoderLayers = 6;
        int decoderLayers = 2;
        int feedForwardDepth = 2;
        int numHeads = 8;
        ModelConfig config =
                new ModelConfig(encoderLayers, decoderLayers, feedForwardDepth, numHeads, "sentence");
        // Package archive = new Package();

        String model_name = "model.intgemm.alphas.bin";
        String vocabulary_name = "vocab.deen.spm";
        String shortlist_name = "lex.s2t.bin";

        int cacheSize = 1024;
        Service service = new Service(cacheSize);

        Package archive =
                new Package(
                        Paths.get(root, model_name).toString(),
                        Paths.get(root, vocabulary_name).toString(),
                        Paths.get(root, shortlist_name).toString(),
                        "");

        Model model = new Model(config, archive);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean html = false;
                List<String> sources = new ArrayList<>();
                sources.add(s.toString());
                String[] targets = service.translate(model, sources, html);
                binding.textviewFirst.setText(targets[0]);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        binding.editTextFirst.addTextChangedListener(watcher);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}