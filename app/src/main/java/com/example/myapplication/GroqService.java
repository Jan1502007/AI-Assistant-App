package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import android.util.Log;
public class GroqService {
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private final List<ChatMessage> history;
    private final Handler mainHandler;

    public interface GroqCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    public GroqService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.history = new ArrayList<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void sendMessage(String prompt, GroqCallback callback) {
        history.add(new ChatMessage("user", prompt));

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", MODEL);
        
        JsonArray messagesArray = new JsonArray();
        for (ChatMessage msg : history) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.role);
            msgObj.addProperty("content", msg.content);
            messagesArray.add(msgObj);
        }
        jsonBody.add("messages", messagesArray);



        RequestBody body =
                RequestBody.create(
                        jsonBody.toString(),
                        JSON
                );
        // Debug logs
        Log.d("GROQ_DEBUG", "API Key Length = " + ApiConfig.GROQ_API_KEY.length());

        if (ApiConfig.GROQ_API_KEY.isEmpty()) {
            Log.e("GROQ_DEBUG", "GROQ_API_KEY is EMPTY!");
        } else {
            Log.d("GROQ_DEBUG", "GROQ_API_KEY loaded successfully.");
        }

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + ApiConfig.GROQ_API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postError("Network failure. Please verify your connection.", callback);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        handleErrorResponse(response.code(), callback);
                        return;
                    }

                    if (responseBody == null) {
                        postError("The AI returned an empty response.", callback);
                        return;
                    }

                    String jsonResponse = responseBody.string();
                    try {
                        JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);
                        JsonArray choices = root.getAsJsonArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JsonObject firstChoice = choices.get(0).getAsJsonObject();
                            JsonObject message = firstChoice.getAsJsonObject("message");
                            String content = message.get("content").getAsString();
                            
                            history.add(new ChatMessage("assistant", content));
                            postSuccess(content, callback);
                        } else {
                            postError("Invalid response format from server.", callback);
                        }
                    } catch (JsonSyntaxException | NullPointerException e) {
                        postError("JSON parsing error.", callback);
                    }
                }
            }
        });
    }

    private void handleErrorResponse(int code, GroqCallback callback) {
        String message;
        switch (code) {
            case 400:
                message = "Bad Request (400). Please check your input.";
                break;
            case 401:
                message = "Invalid Groq API Key (401).";
                break;
            case 404:
                message = "Resource not found (404).";
                break;
            case 429:
                message = "Rate limit exceeded (429). Try again later.";
                break;
            default:
                message = "Server error (" + code + ").";
                break;
        }
        postError(message, callback);
    }

    private void postSuccess(String result, GroqCallback callback) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postError(String error, GroqCallback callback) {
        mainHandler.post(() -> callback.onError(error));
    }

    public void clearHistory() {
        history.clear();
    }

    private static class ChatMessage {
        String role;
        String content;

        ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

