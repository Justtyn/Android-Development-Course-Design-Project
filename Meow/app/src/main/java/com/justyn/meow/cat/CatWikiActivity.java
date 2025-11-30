package com.justyn.meow.cat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.justyn.meow.R;

public class CatWikiActivity extends AppCompatActivity {

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
