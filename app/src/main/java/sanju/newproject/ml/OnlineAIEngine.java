package sanju.newproject.ml;

import android.content.Context;
import android.net.Uri;

public class OnlineAIEngine {
    private final Context context;
    private final MLEngine mlEngine = new MLEngine();

    public interface AIResponseCallback {
        void onResponseReady(String answer, String detectedLanguage);
    }

    public OnlineAIEngine(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
    }

    public static boolean isNetworkAvailable(Context context) {
        return true;
    }

    public String getAnswer(String rawQuestion) {
        return mlEngine.getAnswer(rawQuestion);
    }

    public void generateAnswer(String query, String targetLanguageCode, AIResponseCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onResponseReady("Please ask a Machine Learning question!", "en");
            return;
        }
        mlEngine.getAnswer(query, answer -> callback.onResponseReady(answer, "en"));
    }

    public void generateAnswerWithImage(String query, Uri imageUri, AIResponseCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            query = "Explain this ML related image/dataset";
        }
        mlEngine.getAnswerWithImage(query, imageUri, context, answer -> callback.onResponseReady(answer, "en"));
    }
}