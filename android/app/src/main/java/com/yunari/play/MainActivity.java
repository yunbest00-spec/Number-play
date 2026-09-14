package com.yunari.play;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.webkit.*;
import android.widget.FrameLayout;
import java.io.ByteArrayInputStream;
import java.util.Collections;

public class MainActivity extends Activity {
    private WebView web;
    private SpeechBridge speech;
    private static final String HOST = "appassets.androidplatform.net";
    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout root = new FrameLayout(this);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        root.requestApplyInsets();
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(false);
        web.setBackgroundColor(0xfffffaf1);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        speech = new SpeechBridge(this, web);
        web.addJavascriptInterface(speech, "AriNative");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !("https".equals(request.getUrl().getScheme()) && HOST.equals(request.getUrl().getHost()));
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String path = request.getUrl().getPath();
                if ("https".equals(request.getUrl().getScheme()) && HOST.equals(request.getUrl().getHost())
                        && path != null && path.startsWith("/assets/") && !path.contains("..")) {
                    try {
                        String name = path.substring(8);
                        String mime = name.endsWith(".html") ? "text/html" : name.endsWith(".js") ? "text/javascript" :
                            name.endsWith(".mp3") ? "audio/mpeg" : name.endsWith(".png") ? "image/png" : "application/octet-stream";
                        return new WebResourceResponse(mime, "UTF-8", getAssets().open(name));
                    } catch (Exception ignored) {}
                }
                return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
            }
        });
        web.loadUrl("https://" + HOST + "/assets/index.html");
    }
    @Override public void onBackPressed() {
        web.evaluateJavascript("typeof ariBack==='function' && ariBack()", result -> {
            if (!"true".equals(result)) new AlertDialog.Builder(this).setMessage("놀이를 마칠까요?")
                .setPositiveButton("마치기", (d, w) -> finish()).setNegativeButton("계속 놀기", null).show();
        });
    }
    @Override protected void onPause() {
        if (web != null) {
            web.evaluateJavascript("if(typeof stopVoice==='function')stopVoice();if(typeof audioCtx!=='undefined'&&audioCtx)audioCtx.suspend();", null);
            speech.stop();
            web.onPause();
            web.pauseTimers();
        }
        super.onPause();
    }
    @Override protected void onResume() {
        super.onResume();
        if (web != null) {
            web.resumeTimers();
            web.onResume();
            web.evaluateJavascript("if(typeof audioCtx!=='undefined'&&audioCtx)audioCtx.resume();", null);
        }
    }
    @Override protected void onDestroy() {
        if (speech != null) speech.close();
        if (web != null) { web.removeJavascriptInterface("AriNative"); web.destroy(); web = null; }
        super.onDestroy();
    }
}
