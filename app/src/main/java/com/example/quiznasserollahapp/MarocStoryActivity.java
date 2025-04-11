package com.example.quiznasserollahapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;



public class MarocStoryActivity extends AppCompatActivity {

    private TextView tvStory;
    private Button btnNext;
    private RadioGroup radioGroup;
    private RadioButton radioName, radioAnonymous;
    private RadioButton radioYes;
    private int step = 1;
    private String userName = "Anonymous";

    private SpeechRecognizer speechRecognizer;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maroc_story);

        // Initialize views
        tvStory = findViewById(R.id.tvStory);
        btnNext = findViewById(R.id.btnNext);
        radioGroup = findViewById(R.id.radioGroup);
        radioName = findViewById(R.id.radioName);
        radioAnonymous = findViewById(R.id.radioAnonymous);

        // Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                userName = user.getDisplayName();
            } else if (user.getEmail() != null) {
                userName = user.getEmail();
            } else if (user.getPhoneNumber() != null) {
                userName = user.getPhoneNumber();
            }
        }

        // Step 1 UI
        tvStory.setText("She looks at you and says: \"What’s your name?\"");
        radioGroup.setVisibility(View.VISIBLE);
        radioName.setText(userName);
        radioAnonymous.setText("Prefer not to say");

        btnNext.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                tvStory.setText("Please choose one to continue.");
                return;
            }

            if (step == 1) {
                RadioButton selectedRadio = findViewById(selectedId);
                String chosenName = selectedRadio.getText().toString();

                tvStory.setText("She says: \"My village has sick people who need help. Will you come with me?\"");

                // Update UI for Step 2
                radioGroup.removeAllViews();
                radioYes = new RadioButton(this);
                radioYes.setId(View.generateViewId());
                radioYes.setText("Yes");
                radioYes.setTextColor(getResources().getColor(R.color.old_paper));
                radioYes.setButtonTintList(getResources().getColorStateList(R.color.old_paper));
                radioGroup.addView(radioYes);

                step = 2;

                // Start listening
                checkAudioPermissionAndStartListening();
            } else if (step == 2) {
                Intent intent = new Intent(MarocStoryActivity.this, VillageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void checkAudioPermissionAndStartListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startVoiceRecognition();
        }
    }

    private void startVoiceRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    tvStory.setText("Didn’t catch that. Please say 'Yes'.");
                    startVoiceRecognition(); // Retry
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    String userSpeech = matches.get(0).toLowerCase();

                    if (userSpeech.contains("yes")) {
                        radioYes.setChecked(true);
                        btnNext.performClick();
                    } else {
                        tvStory.setText("You said: \"" + userSpeech + "\". Please say 'Yes'.");
                        startVoiceRecognition(); // Try again

                        // 🔥 OPENAI USE CASE:
                        // Here you can call OpenAI's API with the user's speech.
                        // Example (pseudo-code):
                        //
                        // String reply = openAiApi.send(userSpeech);
                        // tvStory.setText("AI says: " + reply);
                    }
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        // Give mic a little time to get ready
        new Handler().postDelayed(() -> {
            speechRecognizer.startListening(intent);
        }, 800);
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
