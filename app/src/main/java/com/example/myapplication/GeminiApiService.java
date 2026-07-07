package com.example.myapplication;

import com.google.genai.Client;
import com.google.genai.Chat;
import com.google.genai.types.GenerateContentResponse;

public class GeminiApiService {

    private final Chat chat;

    private static final String MODEL_ID = "gemini-2.5-flash";

    public GeminiApiService() {

        Client client = Client.builder()
                .apiKey(BuildConfig.GEMINI_API_KEY)
                .build();

        chat = client.chats.create(MODEL_ID);
    }

    public String getResponse(String prompt) throws Exception {

        GenerateContentResponse response =
                chat.sendMessage(prompt);

        if (response == null || response.text() == null) {
            return "No response received.";
        }

        return response.text();
    }
}