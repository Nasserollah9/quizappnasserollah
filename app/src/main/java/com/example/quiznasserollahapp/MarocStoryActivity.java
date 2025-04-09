package com.example.quiznasserollahapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MarocStoryActivity extends AppCompatActivity {

    private TextView tvStory;
    private Button btnNext;
    private RadioGroup radioGroup;
    private RadioButton radioName, radioAnonymous;

    private int step = 1;
    private String userName = "Anonymous";

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

        // Fetch Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                userName = user.getDisplayName();
            } else if (user.getEmail() != null) {
                userName = user.getEmail(); // Fallback to email if display name is null
            } else if (user.getPhoneNumber() != null) {
                userName = user.getPhoneNumber(); // Fallback to phone
            }
        }

        // Step 1: Ask for name
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

                // Prepare new RadioGroup for step 2
                radioGroup.removeAllViews();
                RadioButton radioYes = new RadioButton(this);
                radioYes.setId(View.generateViewId());
                radioYes.setText("Yes");
                radioYes.setTextColor(getResources().getColor(R.color.old_paper));
                radioYes.setButtonTintList(getResources().getColorStateList(R.color.old_paper));
                radioGroup.addView(radioYes);

                step = 2;
            } else if (step == 2) {
                Intent intent = new Intent(MarocStoryActivity.this, VillageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
