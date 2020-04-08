package com.google.firebase.udacity.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Web2Activity extends AppCompatActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web2);

        webView = (WebView) findViewById(R.id.webView);
        WebSettings ws2 = webView.getSettings();
        ws2.setJavaScriptEnabled(true);
        ws2.setAppCacheEnabled(true);

        Bundle b = getIntent().getExtras();
        String url = b.getString("url");
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading (WebView view, WebResourceRequest request)
            {
                return false;
            }

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                super.onPageFinished(view, url);
            }
        });
    }

    @Override
    public void onBackPressed(){

        if (webView.canGoBack()){
            webView.goBack();
        }else {
            finish();
        }
    }
}
