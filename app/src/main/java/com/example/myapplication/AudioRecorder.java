package com.example.myapplication;

import android.content.Context;
import android.media.MediaRecorder;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {
    private MediaRecorder recorder;
    private File audioFile;
    private final Context context;

    public AudioRecorder(Context context) {
        this.context = context;
    }

    public void startRecording() throws IOException {
        audioFile = File.createTempFile("audio_record", ".m4a", context.getCacheDir());
        
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(audioFile.getAbsolutePath());
        
        recorder.prepare();
        recorder.start();
    }

    public File stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                // Handle case where stop is called immediately after start
                if (audioFile != null && audioFile.exists()) {
                    audioFile.delete();
                }
                return null;
            } finally {
                recorder.release();
                recorder = null;
            }
        }
        return audioFile;
    }

    public boolean isRecording() {
        return recorder != null;
    }
}
