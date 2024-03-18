package com.github.jerinphilip.auslander;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;


import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            /*
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();

             */
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button addRepositoryButton = findViewById(R.id.buttonAddRepository);
        EditText editTextUrl = findViewById(R.id.editTextRepositoryUrl);
        EditText editTextName = findViewById(R.id.editTextRepositoryName);

        ArrayList<Repository> repositories = new ArrayList<>();
        repositories.add(new Repository("Bergamot", "https://translatelocally.com/models.json"));
        repositories.add(new Repository("opus", "https://object.pouta.csc.fi/OPUS-MT-models/app/models.json"));

        RepositoryAdapter repositoriesAdapter = new RepositoryAdapter(this, repositories);
        ListView listView = findViewById(R.id.listViewRepositories);
        listView.setAdapter(repositoriesAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Get the selected item
                Repository repository = (Repository) adapterView.getItemAtPosition(position);
                Log.d("settings", "Downloading " +  repository.getName());
                // Start AsyncTask for download
                NetworkTask.NetworkTaskListener listener = new NetworkTask.NetworkTaskListener() {
                    @Override
                    public void onProgressUpdate(int progress) {
                        // Update UI with download progress
                        // progressBar.setProgress(progress);
                    }

                    @Override
                    public void onDownloadComplete(String result) {
                        // Update UI with download result
                        Toast.makeText(SettingsActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                };

                NetworkTask downloadTask = new NetworkTask(SettingsActivity.this, repository, listener);
                downloadTask.execute();
            }
        });


        addRepositoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve text from EditText
                String url = editTextUrl.getText().toString();
                String name = editTextName.getText().toString();
                Repository repository = new Repository(name, url);
                repositories.add(repository);
                //repositoriesAdapter.update(repositories);
                repositoriesAdapter.notifyDataSetChanged();
                Log.d("settings","Adding " + "Repository(" + repository.getName() + ", " + repository.getUrl() + ")");

            }
        });



    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}