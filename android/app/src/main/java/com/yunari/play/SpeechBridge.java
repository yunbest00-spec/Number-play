package com.yunari.play;

import android.app.Activity;
import android.speech.tts.*;
import android.webkit.*;
import android.widget.Toast;
import java.util.Locale;
import org.json.JSONObject;

public final class SpeechBridge {
    private final Activity activity;
    private final WebView web;
    private TextToSpeech tts;
    private boolean ready, failed, closed, warned;
    private Runnable pending;
    SpeechBridge(Activity activity, WebView web) {
        this.activity = activity;
        this.web = web;
        tts = new TextToSpeech(activity, status -> activity.runOnUiThread(() -> {
            if (closed) return;
            ready = status == TextToSpeech.SUCCESS;
            failed = !ready;
            Runnable next = pending;
            pending = null;
            if (next != null) next.run();
        }));
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}
            @Override public void onDone(String id) { complete(id); }
            @Override public void onError(String id) { complete(id); }
        });
    }
    private void complete(String id) {
        activity.runOnUiThread(() -> {
            if (!closed) web.evaluateJavascript("window.ariNativeDone && window.ariNativeDone(" + JSONObject.quote(id) + ")", null);
        });
    }
    private void missingVoice() {
        if (!warned) {
            warned = true;
            Toast.makeText(activity, "휴대폰 설정에서 한국어와 영어 음성 데이터를 설치하면 안내를 들을 수 있어요.", Toast.LENGTH_LONG).show();
        }
    }
    @JavascriptInterface public void speak(String text, String language, double rate, String id) {
        activity.runOnUiThread(() -> {
            if (closed) return;
            Runnable task = () -> {
                if (!ready) { missingVoice(); complete(id); return; }
                Locale locale = language.startsWith("en") ? Locale.US : Locale.KOREA;
                int availability = tts.setLanguage(locale);
                if (availability < TextToSpeech.LANG_AVAILABLE) { missingVoice(); complete(id); return; }
                Voice best = null;
                if (tts.getVoices() != null) for (Voice voice : tts.getVoices()) {
                    if (!voice.isNetworkConnectionRequired() && voice.getLocale().getLanguage().equals(locale.getLanguage())
                            && (best == null || voice.getQuality() > best.getQuality())) best = voice;
                }
                if (best != null) tts.setVoice(best);
                tts.setSpeechRate((float)Math.max(0.7, Math.min(1.2, rate)));
                tts.setPitch(1f);
                if (tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.ERROR) complete(id);
            };
            if (ready || failed) task.run(); else pending = task;
        });
    }
    @JavascriptInterface public void stop() {
        activity.runOnUiThread(() -> { pending = null; if (tts != null) tts.stop(); });
    }
    void close() {
        closed = true;
        pending = null;
        if (tts != null) { tts.stop(); tts.shutdown(); }
    }
}
