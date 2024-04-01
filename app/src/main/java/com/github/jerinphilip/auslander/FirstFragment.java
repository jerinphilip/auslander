package com.github.jerinphilip.auslander;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.jerinphilip.auslander.databinding.FragmentFirstBinding;
import com.github.jerinphilip.slimt.Model;
import com.github.jerinphilip.slimt.ModelConfig;
import com.github.jerinphilip.slimt.Package;
import com.github.jerinphilip.slimt.Service;
import com.github.jerinphilip.whisper.asr.IRecorderListener;
import com.github.jerinphilip.whisper.asr.IWhisperListener;
import com.github.jerinphilip.whisper.asr.Recorder;
import com.github.jerinphilip.whisper.asr.Whisper;
import com.github.jerinphilip.whisper.utils.WaveUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

  private FragmentFirstBinding binding;
  private final String TAG = "FirstFragment";

  private Whisper mWhisper = null;
  private Recorder mRecorder = null;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    binding = FragmentFirstBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Translator related stuff.
    Context context = getContext();
    String appData = context.getFilesDir().getAbsolutePath();
    String repository = "Bergamot";
    Path rootPath = Paths.get(appData, repository, "English-German tiny", "ende.student.tiny11");
    if (rootPath.toFile().exists()) {
      int encoderLayers = 6;
      int decoderLayers = 2;
      int feedForwardDepth = 2;
      int numHeads = 8;
      ModelConfig config =
          new ModelConfig(encoderLayers, decoderLayers, feedForwardDepth, numHeads, "paragraph");
      // Package archive = new Package();

      String model_name = "model.intgemm.alphas.bin";
      String vocabulary_name = "vocab.deen.spm";
      String shortlist_name = "lex.s2t.bin";

      int cacheSize = 1024;
      Service service = new Service(cacheSize);

      Package archive =
          new Package(
              Paths.get(rootPath.toString(), model_name).toString(),
              Paths.get(rootPath.toString(), vocabulary_name).toString(),
              Paths.get(rootPath.toString(), shortlist_name).toString(),
              "");

      Model model = new Model(config, archive);

      TextWatcher watcher =
          new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
              boolean html = false;
              List<String> sources = new ArrayList<>();
              sources.add(s.toString());
              String[] targets = service.translate(model, sources, html);
              binding.textviewFirst.setText(targets[0]);
              // Scroll to the bottom
              binding.translationScrollView.post(
                  new Runnable() {
                    @Override
                    public void run() {
                      binding.translationScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                  });
            }

            @Override
            public void afterTextChanged(Editable s) {}
          };

      binding.editTextFirst.addTextChangedListener(watcher);
    }

    // Whisper stuff.
    final String[] waveFileName = {WaveUtil.RECORDING_FILE};
    final Handler handler = new Handler(Looper.getMainLooper());

    // Implementation of record button functionality
    binding.iconMic.setOnClickListener(
        v -> {
          if (mRecorder != null && mRecorder.isInProgress()) {
            Log.d(TAG, "Recording is in progress... stopping...");
            stopRecording();
          } else {
            Log.d(TAG, "Start recording...");
            startRecording();
          }
        });

    // Implementation of transcribe button functionality
    binding.iconKbd.setOnClickListener(
        v -> {
          if (mRecorder != null && mRecorder.isInProgress()) {
            Log.d(TAG, "Recording is in progress... stopping...");
            stopRecording();
          }

          if (mWhisper != null && mWhisper.isInProgress()) {
            Log.d(TAG, "Whisper is already in progress...!");
            stopTranscription();
          } else {
            Log.d(TAG, "Start transcription...");
            String waveFilePath = getFilePath(waveFileName[0]);
            startTranscription(waveFilePath);
          }
        });

    // Call the method to copy specific file types from assets to data folder
    String[] extensionsToCopy = {"pcm", "bin", "wav", "tflite"};
    copyAssetsWithExtensionsToDataFolder(context, extensionsToCopy);

    String modelPath;
    String vocabPath;
    boolean useMultilingual = false; // TODO: change multilingual flag as per model used
    if (useMultilingual) {
      // Multilingual model and vocab
      modelPath = getFilePath("whisper-tiny.tflite");
      vocabPath = getFilePath("filters_vocab_multilingual.bin");
    } else {
      // English-only model and vocab
      modelPath = getFilePath("whisper-tiny-en.tflite");
      vocabPath = getFilePath("filters_vocab_en.bin");
    }

    TextView tv = binding.textviewTranscribeSource;
    mWhisper = new Whisper(context);
    mWhisper.loadModel(modelPath, vocabPath, useMultilingual);
    mWhisper.setListener(
        new IWhisperListener() {
          @Override
          public void onUpdateReceived(String message) {
            Log.d(TAG, "Update is received, Message: " + message);
            // handler.post(() -> tvStatus.setText(message));

            if (message.equals(Whisper.MSG_PROCESSING)) {
              handler.post(() -> tv.setText(""));
            } else if (message.equals(Whisper.MSG_FILE_NOT_FOUND)) {
              // write code as per need to handled this error
              Log.d(TAG, "File not found error...!");
            }
          }

          @Override
          public void onResultReceived(String result) {
            Log.d(TAG, "Result: " + result);
            handler.post(() -> tv.append(result));
          }
        });

    mRecorder = new Recorder(context);
    mRecorder.setListener(
        new IRecorderListener() {
          @Override
          public void onUpdateReceived(String message) {
            Log.d(TAG, "Update is received, Message: " + message);
            // handler.post(() -> tvStatus.setText(message));

            if (message.equals(Recorder.MSG_RECORDING)) {
              handler.post(() -> tv.setText(""));
              // handler.post(() -> btnMicRec.setText(Recorder.ACTION_STOP));
            } else if (message.equals(Recorder.MSG_RECORDING_DONE)) {
              // handler.post(() -> btnMicRec.setText(Recorder.ACTION_RECORD));
            }
          }

          @Override
          public void onDataReceived(float[] samples) {
            // mWhisper.writeBuffer(samples);
          }
        });

    // Assume this Activity is the current activity, check record permission
    checkRecordPermission();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void checkRecordPermission() {
    Context context = getContext();
    int permission =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO);
    if (permission == PackageManager.PERMISSION_GRANTED) {
      Log.d(TAG, "Record permission is granted");
    } else {
      Log.d(TAG, "Requesting record permission");
      requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, 0);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
      Log.d(TAG, "Record permission is granted");
    else Log.d(TAG, "Record permission is not granted");
  }

  private void startRecording() {
    checkRecordPermission();

    String waveFilePath = getFilePath(WaveUtil.RECORDING_FILE);
    mRecorder.setFilePath(waveFilePath);
    mRecorder.start();
  }

  private void stopRecording() {
    mRecorder.stop();
  }

  // Transcription calls
  private void startTranscription(String waveFilePath) {
    mWhisper.setFilePath(waveFilePath);
    mWhisper.setAction(Whisper.ACTION_TRANSCRIBE);
    mWhisper.start();
  }

  private void stopTranscription() {
    mWhisper.stop();
  }

  // Copy assets to data folder
  private static void copyAssetsWithExtensionsToDataFolder(Context context, String[] extensions) {
    AssetManager assetManager = context.getAssets();
    try {
      // Specify the destination directory in the app's data folder
      String destFolder = context.getFilesDir().getAbsolutePath();

      for (String extension : extensions) {
        // List all files in the assets folder with the specified extension
        String[] assetFiles = assetManager.list("");
        for (String assetFileName : assetFiles) {
          if (assetFileName.endsWith("." + extension)) {
            File outFile = new File(destFolder, assetFileName);
            if (outFile.exists()) continue;

            InputStream inputStream = assetManager.open(assetFileName);
            OutputStream outputStream = new FileOutputStream(outFile);

            // Copy the file from assets to the data folder
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
              outputStream.write(buffer, 0, read);
            }

            inputStream.close();
            outputStream.flush();
            outputStream.close();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Returns file path from data folder
  private String getFilePath(String assetName) {
    Context context = getContext();
    File outfile = new File(context.getFilesDir(), assetName);
    if (!outfile.exists()) {
      Log.d(TAG, "File not found - " + outfile.getAbsolutePath());
    }

    Log.d(TAG, "Returned asset path: " + outfile.getAbsolutePath());
    return outfile.getAbsolutePath();
  }

  // Test code for parallel processing
  private void testParallelProcessing() {

    // Define the file names in an array
    String[] fileNames = {"english_test1.wav", "english_test2.wav", "english_test_3_bili.wav"};

    // Multilingual model and vocab
    String modelMultilingual = getFilePath("whisper-tiny.tflite");
    String vocabMultilingual = getFilePath("filters_vocab_multilingual.bin");

    // Perform task for multiple audio files using multilingual model
    for (String fileName : fileNames) {
      Context context = getContext();
      Whisper whisper = new Whisper(context);
      whisper.setAction(Whisper.ACTION_TRANSCRIBE);
      whisper.loadModel(modelMultilingual, vocabMultilingual, true);
      // whisper.setListener((msgID, message) -> Log.d(TAG, message));
      String waveFilePath = getFilePath(fileName);
      whisper.setFilePath(waveFilePath);
      whisper.start();
    }

    // English-only model and vocab
    String modelEnglish = getFilePath("whisper-tiny-en.tflite");
    String vocabEnglish = getFilePath("filters_vocab_en.bin");

    // Perform task for multiple audio files using english only model
    for (String fileName : fileNames) {
      Context context = getContext();
      Whisper whisper = new Whisper(context);
      whisper.setAction(Whisper.ACTION_TRANSCRIBE);
      whisper.loadModel(modelEnglish, vocabEnglish, false);
      // whisper.setListener((msgID, message) -> Log.d(TAG, message));
      String waveFilePath = getFilePath(fileName);
      whisper.setFilePath(waveFilePath);
      whisper.start();
    }
  }
}
