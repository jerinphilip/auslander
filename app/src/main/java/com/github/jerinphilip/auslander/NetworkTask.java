package com.github.jerinphilip.auslander;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class NetworkTask extends AsyncTask<String, Void, String> {
  private final Context context;
  private final Repository repository;
  private final NetworkTaskListener listener;

  public NetworkTask(Context context, Repository repository, NetworkTaskListener listener) {
    this.context = context;
    this.repository = repository;
    this.listener = listener;
  }

  @Override
  protected String doInBackground(String... params) {
    try {
      ArrayList<Model> models = this.repository.fetchModels();
      // Get the application's directory
      String archiveRoot =
          Paths.get(this.context.getFilesDir().toString(), repository.getName()).toString();
      Repository.downloadModels(models, archiveRoot);
    } catch (IOException e) {
      Log.e("NetworkTask", "Error", e);
      return null;
    }
    return "";
  }

  public interface NetworkTaskListener {
    void onProgressUpdate(int progress);

    void onDownloadComplete(String result);
  }
}
