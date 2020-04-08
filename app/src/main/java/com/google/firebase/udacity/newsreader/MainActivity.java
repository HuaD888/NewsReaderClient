package com.google.firebase.udacity.newsreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity {
    final Boolean UseLocal = false;
    WebView webView;
    WebView webView2;
    //SwipeRefreshLayout swipe;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        WebAction();
    }

    public void WebAction() {
        webView2 = (WebView) findViewById(R.id.webView2);
        webView2.setWebContentsDebuggingEnabled(true);
        WebSettings ws2 = webView2.getSettings();
        ws2.setBuiltInZoomControls(true);
        ws2.setDisplayZoomControls(false);
        ws2.setJavaScriptEnabled(true);
        ws2.setAllowFileAccess(true);
        ws2.setAppCacheEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            SetWebSetting(ws2);
            registerForContextMenu(webView2);
        }

        webView2.setWebChromeClient(new WebChromeClient(){
            private Boolean triggered = false;
            private int threshold = 90;

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(!triggered && newProgress >= threshold) {
                    String url = view.getUrl();
                    if (url.contains("wenxuecity")) {
                        triggered = true;
                        view.evaluateJavascript("(function() {var parent = document.getElementsByTagName('head').item(0); var style = document.createElement('style'); style.type = 'text/css'; style.innerHTML = ('.sidebar {visibility:hidden !important}  .maincontainer {font-size:40px !important}');  parent.appendChild(style);})()", null);
                    } else if (url.contains("backchina")) {
                        triggered = true;
                        view.evaluateJavascript("(function() {var t = document.querySelectorAll(\"div[data-google-query-id]\"); if(t !== undefined) t.forEach(e => e.style.visibility=\"hidden\");})()", null);
                    }
                }
            }
        });

        webView2.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url == "about:blank") {
                    webView2.clearCache(true);
                    webView2.clearHistory();
                    webView2.clearFormData();
                }
                else if(url.contains("wenxuecity"))
                {
                    view.evaluateJavascript("(function() {var parent = document.getElementsByTagName('head').item(0); var style = document.createElement('style'); style.type = 'text/css'; style.innerHTML = ('.sidebar {visibility:hidden !important}  .maincontainer {font-size:40px}');  parent.appendChild(style);})()", null);
                }
                else if(url.contains("backchina"))
                {
                    view.evaluateJavascript("(function() {var t = document.querySelectorAll(\"div[data-google-query-id]\"); if(t !== undefined) t.forEach(e => e.style.visibility=\"hidden\");})()", null);
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.d(TAG, "resource url is" + url);
            }
        });
        webView2.setVisibility(View.GONE);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebContentsDebuggingEnabled(true);
        WebSettings ws = webView.getSettings();
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setJavaScriptEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAppCacheEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            SetWebSetting(ws);
            registerForContextMenu(webView);
        }

        //webView.getSettings().setJavaScriptEnabled(true);
        //webView.getSettings().setAppCacheEnabled(true);
        webView.setVisibility(View.VISIBLE);

        if(UseLocal)
            webView.loadUrl("http://10.0.2.2:3500/?app=1"); //http://10.0.2.2:3500/?app=1
        else
            webView.loadUrl("https://huacrawler.azurewebsites.net/?app=1");

        //swipe.setRefreshing(true);

        webView.setWebViewClient(new WebViewClient() {
            //webView.setWebViewClient(new LoadInSameClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String myCookies = CookieManager.getInstance().getCookie(url);
                Log.d(TAG, "cookie is" + myCookies);
                /*
                String myCookies = CookieManager.getInstance().getCookie(url);
                webView.evaluateJavascript("window.localStorage.getItem('rawdata'", new ValueCallback<String>(){
                    @Override
                    public void onReceiveValue(String value) {
                        Log.d(TAG, value);
                    }
                });
                 */
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadUrl("file:///android_assets/error.html");
            }

            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                webView.setVisibility(View.GONE);
                webView2.setVisibility(View.VISIBLE);
                webView2.loadUrl(request.getUrl().toString());
                return true;
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //webView.setVisibility(View.GONE);

                    /*
                    Intent intent = new Intent(MainActivity.this, Web2Activity.class);
                    Bundle b = new Bundle();
                    b.putString("url", url);
                    intent.putExtras(b);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityForResult(intent, 2);
                    */

                webView.setVisibility(View.GONE);
                webView2.setVisibility(View.VISIBLE);
                webView2.loadUrl(url);

                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                request.getRequestHeaders().put("Referrer Policy", "no-referrer");
                return super.shouldInterceptRequest(view, request);
            }
        });
    }

    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // Bitmap is loaded, use image here
            Intent i = new Intent(Intent.ACTION_SEND);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setType("image/*");
            i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
            startActivity(Intent.createChooser(i, "Share Image"));
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            Log.d("DEBUG", "onBitmapFailed");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d("DEBUG", "onPrepareLoad");
        }
    };

    public void shareItem(String url) {
        Picasso.get().load(url).into(target);
    }

    public Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();
            //bmpUri = Uri.fromFile(file);
            bmpUri = FileProvider.getUriForFile(this, "com.google.firebase.udacity.newsreader", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo){
        super.onCreateContextMenu(contextMenu, view, contextMenuInfo);

        WebView curwebView1;
        if(webView2.getVisibility() == View.VISIBLE)
            curwebView1 = webView2;
        else
            curwebView1 = webView;

        final WebView.HitTestResult webViewHitTestResult = curwebView1.getHitTestResult();

        if (webViewHitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                webViewHitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
        {
            contextMenu.add(0, view.getId(), 0, "Share Image")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            String DownloadImageURL = webViewHitTestResult.getExtra();
                            if(URLUtil.isValidUrl(DownloadImageURL)){
                                shareItem(DownloadImageURL);
                            }
                            else {
                                Toast.makeText(MainActivity.this,"Sorry.. Something Went Wrong.",Toast.LENGTH_LONG).show();
                            }

                            return false;
                        }
                    });
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle()=="share image")
        {
            // This is the code which i am using for share intent
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageState()));
            startActivity(Intent.createChooser(share, "Share image using"));
        }else{
            return false;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sports:
                SwitchToMainWeb();
                if(UseLocal)
                    webView.loadUrl("http://10.0.2.2:3500/?channel=%E4%BD%93%E8%82%B2&app=1");
                else
                    webView.loadUrl("http://huacrawler.azurewebsites.net/?channel=%E4%BD%93%E8%82%B2&app=1");
                break;
            case R.id.action_news:
                SwitchToMainWeb();
                if(UseLocal)
                    webView.loadUrl("http://10.0.2.2:3500/?app=1");
                else
                    webView.loadUrl("http://huacrawler.azurewebsites.net/?app=1");
                break;
            case R.id.action_stock:
                SwitchToMainWeb();
                if(UseLocal)
                    webView.loadUrl("http://10.0.2.2:3500/?channel=stock&app=1");
                else
                    webView.loadUrl("http://huacrawler.azurewebsites.net/?channel=stock&app=1");
                break;
            case R.id.action_health:
                SwitchToMainWeb();
                if(UseLocal)
                    webView.loadUrl("http://10.0.2.2:3500/?channel=health&app=1");
                else
                    webView.loadUrl("http://huacrawler.azurewebsites.net/?channel=health&app=1");
                break;
            case R.id.action_teamblind:
                SwitchToMainWeb();
                if(UseLocal)
                    webView.loadUrl("http://10.0.2.2:3500/teamblind?app=1");
                else
                    webView.loadUrl("http://huacrawler.azurewebsites.net/teamblind?app=1");
                break;
            case R.id.action_history:
                SwitchToMainWeb();
                if(UseLocal)
                    webView.loadUrl("http://10.0.2.2:3500/history?app=1");
                else
                    webView.loadUrl("http://huacrawler.azurewebsites.net/history?app=1");
                break;
            case R.id.action_about:
                WebView curwebView;
                if (webView2.getVisibility() == View.VISIBLE)
                    curwebView = webView2;
                else
                    curwebView = webView;

                if(UseLocal)
                    curwebView.loadUrl("http://10.0.2.2:3500/about?app=1");
                else
                    curwebView.loadUrl("http://huacrawler.azurewebsites.net/about?app=1");

                break;
            case R.id.action_refresh:
                if (webView2.getVisibility() == View.VISIBLE) {
                    webView2.reload();
                }
                else {
                    webView.evaluateJavascript("refreshData()", null);
                }

                /*
                if(webView.getVisibility() == View.VISIBLE)
                {
                    webView.evaluateJavascript("refreshData()", null);

                    webView.evaluateJavascript("window.localStorage.getItem('rawdata')", new ValueCallback<String>(){
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d(TAG, value);
                        }
                    });

                }
                 */

                break;
            case R.id.action_share:
                WebView curwebView1;
                if (webView2.getVisibility() == View.VISIBLE)
                    curwebView1 = webView2;
                else
                    curwebView1 = webView;

                String msg = curwebView1.getUrl();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share..."));
            default:


                break;
        }
        return true;
    }


    @Override
    public void onBackPressed(){
        if (webView2.getVisibility() == View.VISIBLE) {
            if (webView2.canGoBack()) {
                Boolean goBack = true;

                WebBackForwardList list = webView2.copyBackForwardList();
                String historyUrl = list.getItemAtIndex(list.getCurrentIndex() - 1).getUrl();
                if (historyUrl.equals("about:blank"))
                    goBack = false;

                if (goBack) {
                    webView2.goBack();
                    return;
                }
            }

            webView2.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView2.loadUrl("about:blank");
        }
        else
        {
            if (webView.canGoBack()){
                webView.goBack();
            }else {
                finish();
            }
        }
    }

    private void SwitchToMainWeb()
    {
        if (webView2.getVisibility() == View.VISIBLE) {
            webView2.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView2.loadUrl("about:blank");
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
