package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MessageAdapter.OnRetryListener {

    private ActivityMainBinding binding;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private FirebaseAuthManager authManager;
    private GroqService groqService;
    private GroqSpeechService speechService;
    private AudioRecorder audioRecorder;
    private String lastUserMessage = "";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new FirebaseAuthManager();
        
        // Initialize Groq service
        groqService = new GroqService();
        speechService = new GroqSpeechService();
        audioRecorder = new AudioRecorder(this);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        updateEmptyView();

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.btnMic.setOnClickListener(v -> toggleRecording());
        
        // Chip listeners
        View.OnClickListener chipListener = v -> {
            if (v instanceof com.google.android.material.chip.Chip) {
                binding.etMessage.setText(((com.google.android.material.chip.Chip) v).getText());
                sendMessage();
            }
        };
        binding.chip1.setOnClickListener(chipListener);
        binding.chip2.setOnClickListener(chipListener);
        binding.chip3.setOnClickListener(chipListener);
        binding.chip4.setOnClickListener(chipListener);

        binding.ivLogout.setOnClickListener(v -> {
            authManager.logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void toggleRecording() {
        if (audioRecorder.isRecording()) {
            stopRecording();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startRecording();
            }
        }
    }

    private void startRecording() {
        try {
            audioRecorder.startRecording();
            binding.btnMic.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_red_light)));
            binding.btnMic.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white)));
            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        File audioFile = audioRecorder.stopRecording();
        binding.btnMic.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.background)));
        binding.btnMic.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_secondary)));
        
        if (audioFile != null) {
            Toast.makeText(this, "Transcribing...", Toast.LENGTH_SHORT).show();
            speechService.transcribe(audioFile, new GroqSpeechService.SpeechCallback() {
                @Override
                public void onTranscription(String text) {
                    binding.etMessage.setText(text);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        // Animate send button
        binding.btnSend.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
            binding.btnSend.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
        });

        lastUserMessage = text;
        addMessage(new Message(text, Message.TYPE_USER));
        binding.etMessage.setText("");
        
        callGroq(text);
    }

    private void callGroq(String prompt) {
        // Show loading indicator
        addMessage(new Message("...", Message.TYPE_LOADING));

        groqService.sendMessage(prompt, new GroqService.GroqCallback() {
            @Override
            public void onSuccess(String response) {
                removeLastMessage(); // Remove loading
                addMessage(new Message(response, Message.TYPE_AI));
            }

            @Override
            public void onError(String errorMessage) {
                removeLastMessage(); // Remove loading
                addMessage(new Message(errorMessage, Message.TYPE_ERROR));
            }
        });
    }

    private void addMessage(Message message) {
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        binding.recyclerView.smoothScrollToPosition(messageList.size() - 1);
        updateEmptyView();
    }

    private void removeLastMessage() {
        if (!messageList.isEmpty()) {
            int lastIndex = messageList.size() - 1;
            messageList.remove(lastIndex);
            adapter.notifyItemRemoved(lastIndex);
        }
    }

    private void updateEmptyView() {
        if (messageList.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRetry() {
        if (!lastUserMessage.isEmpty()) {
            if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getType() == Message.TYPE_ERROR) {
                removeLastMessage();
                callGroq(lastUserMessage);
            }
        }
    }
}
