package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroqSpeechService {
    private static final String API_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
    private static final String MODEL = "whisper-large-v3";
    
    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;

    public interface SpeechCallback {
        void onTranscription(String text);
        void onError(String error);
    }

    public GroqSpeechService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void transcribe(File audioFile, SpeechCallback callback) {
        if (audioFile == null || !audioFile.exists()) {
            callback.onError("Audio file not found.");
            return;
        }

        RequestBody fileBody = RequestBody.create(audioFile, MediaType.parse("audio/m4a"));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .addFormDataPart("model", MODEL)
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + ApiConfig.GROQ_API_KEY)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postError("Network error during transcription.", callback);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        postError("Transcription failed: " + response.code(), callback);
                        return;
                    }

                    if (response.body() == null) {
                        postError("Empty response from transcription service.", callback);
                        return;
                    }

                    String json = response.body().string();
                    try {
                        JsonObject root = gson.fromJson(json, JsonObject.class);
                        if (root.has("text")) {
                            postSuccess(root.get("text").getAsString(), callback);
                        } else {
                            postError("Invalid transcription response.", callback);
                        }
                    } catch (JsonSyntaxException e) {
                        postError("JSON error during transcription.", callback);
                    }
                } finally {
                    // Clean up temp file
                    if (audioFile.exists()) {
                        audioFile.delete();
                    }
                }
            }
        });
    }

    private void postSuccess(String text, SpeechCallback callback) {
        mainHandler.post(() -> callback.onTranscription(text));
    }

    private void postError(String error, SpeechCallback callback) {
        mainHandler.post(() -> callback.onError(error));
    }
}
