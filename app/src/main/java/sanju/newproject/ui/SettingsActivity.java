package sanju.newproject.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import sanju.newproject.R;
import sanju.newproject.database.AppDatabase;
import sanju.newproject.utils.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private Switch switchDarkTheme;
    private Spinner spinnerLanguage;
    private Button btnClearHistory;

    private final String[] languageNames = {
            "Auto (Detect Language)", "English", "Hindi", "Marathi", "Spanish",
            "French", "German", "Chinese (Mandarin)", "Japanese", "Korean",
            "Russian", "Portuguese", "Italian", "Arabic", "Bengali",
            "Urdu", "Tamil", "Telugu", "Gujarati", "Punjabi", "Vietnamese"
    };

    private final String[] languageCodes = {
            "auto", "en", "hi", "mr", "es",
            "fr", "de", "zh", "ja", "ko",
            "ru", "pt", "it", "ar", "bn",
            "ur", "ta", "te", "gu", "pa", "vi"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(this);

        if (preferenceManager.isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        switchDarkTheme = findViewById(R.id.switchDarkTheme);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        btnClearHistory = findViewById(R.id.btnClearHistory);

        switchDarkTheme.setChecked(preferenceManager.isDarkTheme());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languageNames);
        spinnerLanguage.setAdapter(adapter);

        // Select current language code
        String currentLang = preferenceManager.getSelectedLanguage();
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                spinnerLanguage.setSelection(i);
                break;
            }
        }

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setDarkTheme(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferenceManager.setSelectedLanguage(languageCodes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnClearHistory.setOnClickListener(v -> {
            new Thread(() -> {
                AppDatabase.getInstance(getApplicationContext()).chatDao().deleteAllMessages();
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "Chat history cleared", Toast.LENGTH_SHORT).show());
            }).start();
        });
    }
}
