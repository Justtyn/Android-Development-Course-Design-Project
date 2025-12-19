package com.justyn.meow.cat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.justyn.meow.R;

/**
 * 猫咪百科页面：用 WebView 加载本地 HTML 内容。
 * <p>
 * 页面内容来自 assets/cat_wiki.html，需开启 JavaScript 以保证页面交互正常。
 * </p>
 */
public class CatWikiActivity extends AppCompatActivity {

    /**
     * 初始化 WebView 并加载本地页面。
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cat_wiki);

        WebView webView = findViewById(R.id.webViewMeow);
        // 允许网页使用 JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        // 加载 assets 下HTML
        webView.loadUrl("file:///android_asset/cat_wiki.html");
    }
}
