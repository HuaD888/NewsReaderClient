package com.google.firebase.udacity.newsreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class BrowserActivity extends AppCompatActivity {
    public static final String PREFERENCES = "PREFERENCES_NAME";
    public static final String WEB_LINKS = "links";
    public static final String WEB_TITLE = "title";
    private static final String TAG = "BrowserActivity";
    ConstraintLayout constraintLayout;
    WebView webView;
    private ProgressBar progressBar;
    String current_page_url = "https://www.wikipedia.com";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        constraintLayout = findViewById(R.id.constraintLayoutParent);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        /*
        getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_menu_camera); //ic_arrow_back
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        */

        if (getIntent().getExtras() != null) {
            current_page_url = getIntent().getStringExtra("url");
        }

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        WebSettings ws2 = webView.getSettings();
        ws2.setBuiltInZoomControls(true);
        ws2.setDisplayZoomControls(false);
        ws2.setJavaScriptEnabled(true);
        ws2.setAllowFileAccess(true);
        ws2.setAppCacheEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            SetWebSetting(ws2);
        }

        webView.loadUrl(current_page_url);
        initWebView();
    }

    private void initWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                current_page_url = url;
                invalidateOptionsMenu();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.loadUrl(request.getUrl().toString());
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                invalidateOptionsMenu();

                Boolean apply = false;
                if(url.contains("wenxuecity"))
                {
                    apply = true;
                    view.evaluateJavascript("(function() {var parent = document.getElementsByTagName('head').item(0); var style = document.createElement('style'); style.type = 'text/css'; style.innerHTML = ('.sidebar {visibility:hidden !important}  .maincontainer {font-size:40px}');  parent.appendChild(style);})()", null);
                }
                else if(url.contains("backchina"))
                {
                    apply = true;
                    view.evaluateJavascript("(function() {var t = document.querySelectorAll(\"div[data-google-query-id]\"); if(t !== undefined) t.forEach(e => e.style.visibility=\"hidden\");})()", null);
                }

                if(apply)
                {
                    Snackbar snackbar1 = Snackbar.make(constraintLayout, "Applied local style successfully", Snackbar.LENGTH_SHORT);
                    snackbar1.show();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                invalidateOptionsMenu();
            }
        });

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.clearCache(true);
        webView.clearHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bowser, menu);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String links = sharedPreferences.getString(WEB_LINKS, null);

        if (links != null) {
            Gson gson = new Gson();
            ArrayList<String> linkList = gson.fromJson(links, new TypeToken<ArrayList<String>>() {
            }.getType());

            if (linkList.contains(current_page_url)) {
                menu.getItem(1).setIcon(R.drawable.bookmarks_sel);
            } else {
                menu.getItem(1).setIcon(R.drawable.bookmarks);
            }
        } else {
            menu.getItem(1).setIcon(R.drawable.bookmarks);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Snackbar snackbar1;
        if (item.getItemId() == R.id.action_bookmark) {

            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
            String jsonLink = sharedPreferences.getString(WEB_LINKS, null);
            String jsonTitle = sharedPreferences.getString(WEB_TITLE, null);


            if (jsonLink != null && jsonTitle != null) {

                Gson gson = new Gson();
                ArrayList<String> linkList = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {
                }.getType());

                ArrayList<String> titleList = gson.fromJson(jsonTitle, new TypeToken<ArrayList<String>>() {
                }.getType());

                if (linkList.contains(current_page_url)) {
                    linkList.remove(current_page_url);
                    titleList.remove(webView.getTitle().trim());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                    editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                    editor.apply();

                    snackbar1 = Snackbar.make(constraintLayout, "Bookmark was removed successfully!", Snackbar.LENGTH_SHORT);
                    snackbar1.show();

                } else {
                    linkList.add(current_page_url);
                    titleList.add(webView.getTitle().trim());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                    editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                    editor.apply();

                    snackbar1 = Snackbar.make(constraintLayout, "Bookmark was added successfully!", Snackbar.LENGTH_SHORT);
                    snackbar1.show();
                }
            } else {

                ArrayList<String> linkList = new ArrayList<>();
                ArrayList<String> titleList = new ArrayList<>();
                linkList.add(current_page_url);
                titleList.add(webView.getTitle());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                editor.apply();

                snackbar1 = Snackbar.make(constraintLayout, "Bookmark was added successfully!", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }

            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void SetWebSetting(WebSettings ws)
    {
        try {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            ws.setLoadsImagesAutomatically(true);
            Log.d(TAG, "Enabling HTML5-Features");
            Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", new Class[]{
                    Boolean.TYPE
            });
            m1.invoke(ws, Boolean.TRUE);

            Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", new Class[]{
                    Boolean.TYPE
            });
            m2.invoke(ws, Boolean.TRUE);

            Method m3 = WebSettings.class.getMethod("setDatabasePath", new Class[]{
                    String.class
            });
            m3.invoke(ws, "/data/data/" + getPackageName() + "/databases/");

            Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[]{
                    Long.TYPE
            });
            m4.invoke(ws, 1024 * 1024 * 8);

            Method m5 = WebSettings.class.getMethod("setAppCachePath", new Class[]{
                    String.class
            });
            m5.invoke(ws, "/data/data/" + getPackageName() + "/cache/");

            Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", new Class[]{
                    Boolean.TYPE
            });
            m6.invoke(ws, Boolean.TRUE);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Reflection fail", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Log.e(TAG, "Reflection fail", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Reflection fail", e);
        }
    }
}
