package com.github.jerinphilip.auslander;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Repository {
    private final String url;
    private final String name;

    public Repository(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() { return this.name; }
    public String getUrl() { return this.url; }

    public ArrayList<Model> fetchModels() throws IOException {
        String json = makeHttpRequest(url);
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        Gson gson = new Gson();
        Model[] models = gson.fromJson(jsonObject.get("models"), Model[].class);
        return new ArrayList<>(Arrays.asList(models));
    }

    private static String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    public static void downloadFile(String fileUrl, File directory) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // Check if the connection is successful
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Open input stream from the connection
                InputStream inputStream = httpConn.getInputStream();

                // Create output stream to write the downloaded file
                FileOutputStream outputStream = new FileOutputStream(new File(directory, "model.tar.gz"));

                // Read from input stream and write to output stream
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Close streams
                outputStream.close();
                inputStream.close();

                System.out.println("File downloaded successfully.");
            } else {
                System.out.println("Failed to download file. Server returned HTTP response code: " + responseCode);
            }
            httpConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downloadModels(ArrayList<Model> models, String archiveRoot) {

        // Create a File object representing the directory
        File directory = new File(archiveRoot);

        for (Model model : models) {
            Path archiveRootPath = Paths.get(archiveRoot, model.getModelName());
            File modelRoot = new File(archiveRootPath.toString());
            downloadModel(model, modelRoot);
        }
    }

    public static void downloadModel(Model model, File location) {
        try {
            URL url = new URL(model.getUrl());
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // Check if the connection is successful
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Open input stream from the connection
                InputStream inputStream = httpConn.getInputStream();

                // Create output stream to write the downloaded file
                if(!location.exists()) {
                    Log.d("repository", "Creating directory: " + location.toString());
                    boolean created = location.mkdirs();
                }

                File archiveFile = new File(location, model.getCode() + ".tar.gz");
                if(!archiveFile.exists()) {
                    FileOutputStream outputStream = new FileOutputStream(archiveFile);

                    // Read from input stream and write to output stream
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    // Close streams
                    outputStream.close();
                    inputStream.close();

                    Log.d("repository.download", "Model " + model.getCode() + " downloaded successfully.");
                } else {
                    Log.d("repository.download", "Model " + model.getCode() + " already downloaded.");

                }

                Repository.extractTarGz(archiveFile.toString(), location.toString());
            } else {
                Log.d("repository.download", "Failed to download model " + model.getCode() + ". Server returned HTTP response code: " + responseCode);
            }
            httpConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void extractTarGz(String tarGzFilePath, String destinationFolder) throws IOException {
        File tarGzFile = new File(tarGzFilePath);
        File destinationDir = new File(destinationFolder);

        try (FileInputStream fis = new FileInputStream(tarGzFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(bis);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

            ArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                Path entryPath = Paths.get(destinationDir.getAbsolutePath(), entry.getName());
                File entryFile = entryPath.toFile();
                File parent = entryFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                try (OutputStream out = Files.newOutputStream(entryPath)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = tarIn.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            }
        }
    }

}