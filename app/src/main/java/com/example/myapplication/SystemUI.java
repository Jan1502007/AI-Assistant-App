package com.example.myapplication;



import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

    public class SystemUI extends AppCompatActivity {

        private TextView btnInstant, btnExpert, btnVision, tvDescription;
        private EditText etInput;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Initialize Views
            btnInstant = findViewById(R.id.btnInstant);
            btnExpert = findViewById(R.id.btnExpert);
            btnVision = findViewById(R.id.btnVision);
            tvDescription = findViewById(R.id.tvDescription);
            etInput = findViewById(R.id.etMessage);

            // Setup Mode Toggles
            btnInstant.setOnClickListener(v -> selectMode(btnInstant, "Instant responses for daily conversations"));
            btnExpert.setOnClickListener(v -> selectMode(btnExpert, "Deep reasoning for complex queries"));
            btnVision.setOnClickListener(v -> selectMode(btnVision, "Analyze and discuss images"));
        }

        private void selectMode(TextView selectedView, String description) {
            // Reset all buttons to default state
            resetButton(btnInstant);
            resetButton(btnExpert);
            resetButton(btnVision);

            // Highlight selected button
            selectedView.setBackgroundResource(R.drawable.bg_mode_active);
            selectedView.setTextColor(Color.parseColor("#3B82F6"));

            // Update description text
            tvDescription.setText(description);
        }

        private void resetButton(TextView textView) {
            textView.setBackground(null);
            textView.setTextColor(Color.parseColor("#4B5563"));
        }
    }


