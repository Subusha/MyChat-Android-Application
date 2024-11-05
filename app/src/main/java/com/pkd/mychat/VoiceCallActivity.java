package com.pkd.mychat;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.webrtc.SurfaceViewRenderer;

public class VoiceCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        // Initialize UI elements
        SurfaceViewRenderer remoteVideoView = findViewById(R.id.remoteVideoView);
        SurfaceViewRenderer localVideoView = findViewById(R.id.localVideoView);
        TextView callStatusTextView = findViewById(R.id.callStatusTextView);
        ImageButton muteButton = findViewById(R.id.muteButton);
        ImageButton hangupButton = findViewById(R.id.hangupButton);

        // Set click listeners for buttons (e.g., mute and hangup)
        muteButton.setOnClickListener(v -> {
            // Implement mute functionality here
        });

        hangupButton.setOnClickListener(v -> {
            // Implement hang-up functionality here
        });
    }
}