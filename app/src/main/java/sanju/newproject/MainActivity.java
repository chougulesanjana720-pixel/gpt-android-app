package sanju.newproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sanju.newproject.adapter.ChatAdapter;
import sanju.newproject.ml.MLEngine;
import sanju.newproject.ml.OnlineAIEngine;
import sanju.newproject.models.ChatMessage;
import sanju.newproject.ui.SettingsActivity;
import sanju.newproject.utils.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MLExpert";

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerViewChat;
    private TextView tvTypingIndicator;
    private EditText etMessage;
    private ImageButton btnPlus;
    private ImageButton btnMic;
    private ImageButton btnSend;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> chatList = new ArrayList<>();

    private OnlineAIEngine onlineAIEngine;
    private MLEngine mlEngine;
    private PreferenceManager preferenceManager;

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isListening = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startListening();
                } else {
                    Toast.makeText(this, "Microphone permission is required for voice typing", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        tvTypingIndicator = findViewById(R.id.tvTypingIndicator);
        etMessage = findViewById(R.id.etMessage);
        btnPlus = findViewById(R.id.btnPlus);
        btnMic = findViewById(R.id.btnMic);
        btnSend = findViewById(R.id.btnSend);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Setup Toolbar & Drawer
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ML Expert");
        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Edge-To-Edge Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinatorLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return WindowInsetsCompat.CONSUMED;
        });

        // Initialize Helpers
        preferenceManager = new PreferenceManager(this);
        onlineAIEngine = new OnlineAIEngine(this);
        mlEngine = new MLEngine();

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(this, chatList);
        chatAdapter.setOnActionClickListener(userPrompt -> generateOnlineAnswer(userPrompt));
        recyclerViewChat.setAdapter(chatAdapter);

        // Auto-refresh: Start fresh session on every app open
        startFreshSession();

        // Setup Voice Typing (Speech to Text ONLY)
        setupVoiceTyping();

        // Plus Icon (Image/File Upload) Listener
        btnPlus.setOnClickListener(v -> {
            String[] options = {"Upload Image (Graph/Dataset)", "Upload File (CSV/PDF)"};
            new AlertDialog.Builder(this)
                    .setTitle("Upload for ML Analysis")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) openImagePicker();
                        else openFilePicker();
                    }).show();
        });

        // Send Button
        btnSend.setOnClickListener(v -> {
            String question = etMessage.getText().toString().trim();
            Log.d(TAG, "Send clicked: " + question);
            if (!question.isEmpty()) {
                addMessageToChat(question, "user");
                etMessage.setText("");
                generateOnlineAnswer(question);
            }
        });

        // Enter key press support
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                btnSend.performClick();
                return true;
            }
            return false;
        });

        // Drawer Navigation Listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chat) {
                startFreshSession();
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_clear) {
                startFreshSession();
                Toast.makeText(MainActivity.this, "Session cleared", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1001);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf", "text/csv"});
        startActivityForResult(intent, 1002);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri newUri = data.getData(); // FRESH URI EVERY TIME - NO CACHE
            String userQ = etMessage.getText().toString().trim();
            if (userQ.isEmpty()) userQ = "Explain this ML related image/dataset";
            etMessage.setText("");

            addUserImageMessage(newUri, userQ);
            addBotMessage("🔍 Analyzing THIS ML diagram online...");

            mlEngine.getImageAnswer(userQ, newUri, this, ans -> {
                runOnUiThread(() -> {
                    removeLastMessage(); // Remove analyzing
                    addBotMessage(ans);
                });
            });
        }
    }

    private void addBotMessage(String messageText) {
        addMessageToChat(messageText, "ai");
    }

    private void removeLastMessage() {
        if (!chatList.isEmpty()) {
            int removeIndex = chatList.size() - 1;
            chatList.remove(removeIndex);
            chatAdapter.notifyItemRemoved(removeIndex);
        }
    }

    private void addUserImageMessage(Uri uri) {
        String question = etMessage.getText().toString().trim();
        if (question.isEmpty()) question = "Explain this ML related image/dataset";
        addUserImageMessage(uri, question);
    }

    private void addUserImageMessage(Uri uri, String text) {
        long timestamp = System.currentTimeMillis();
        String lang = preferenceManager.getSelectedLanguage();
        ChatMessage chatMessage = new ChatMessage("user", text, timestamp, lang, uri.toString());
        chatList.add(chatMessage);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        recyclerViewChat.scrollToPosition(chatList.size() - 1);
    }

    private void setupVoiceTyping() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            btnMic.setVisibility(View.GONE);
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        btnMic.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (!isListening) {
                    startListening();
                } else {
                    stopListening();
                }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                resetMicUI();
            }

            @Override
            public void onError(int error) {
                resetMicUI();
            }

            @Override
            public void onResults(Bundle results) {
                if (results != null) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        etMessage.setText(recognizedText);
                        etMessage.setSelection(recognizedText.length());
                    }
                }
                resetMicUI();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        if (speechRecognizer != null) {
            speechRecognizer.startListening(speechIntent);
            etMessage.setHint("Listening... Speak now");
            btnMic.setImageResource(R.drawable.ic_mic_recording);
            isListening = true;
        }
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        resetMicUI();
    }

    private void resetMicUI() {
        etMessage.setHint("Ask a Machine Learning question...");
        btnMic.setImageResource(R.drawable.ic_mic);
        isListening = false;
    }

    private void startFreshSession() {
        chatList.clear();
        String welcomeMessage = "Hello! I am your ML Expert. Ask me any Machine Learning question!";
        chatList.add(new ChatMessage("ai", welcomeMessage, System.currentTimeMillis(), "en"));
        chatAdapter.notifyDataSetChanged();
        recyclerViewChat.scrollToPosition(0);
    }

    private void addMessageToChat(String messageText, String sender) {
        long timestamp = System.currentTimeMillis();
        String lang = preferenceManager.getSelectedLanguage();
        ChatMessage chatMessage = new ChatMessage(sender, messageText, timestamp, lang);

        chatList.add(chatMessage);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        recyclerViewChat.scrollToPosition(chatList.size() - 1);
    }

    private void generateOnlineAnswer(String userPrompt) {
        tvTypingIndicator.setVisibility(View.VISIBLE);
        mlEngine.getTextAnswer(userPrompt, ans -> {
            runOnUiThread(() -> {
                tvTypingIndicator.setVisibility(View.GONE);
                addBotMessage(ans);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}