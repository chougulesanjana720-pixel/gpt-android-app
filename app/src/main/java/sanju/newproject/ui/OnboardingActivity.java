package sanju.newproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import sanju.newproject.MainActivity;
import sanju.newproject.R;
import sanju.newproject.utils.PreferenceManager;

public class OnboardingActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPhone;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(this);

        if (preferenceManager.isOnboardingCompleted()) {
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        Button btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.isEmpty() || phone.length() != 10 || !phone.matches("\\d{10}")) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        preferenceManager.setOnboardingCompleted(true, email, phone);
        Toast.makeText(this, "Welcome to Online AI Chat!", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
        finish();
    }
}