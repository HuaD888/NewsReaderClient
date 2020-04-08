package com.google.firebase.udacity.newsreader;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class LoadInSameClient extends WebViewClient {
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webview, WebResourceRequest webrequest)
    {
        Log.d("test", "shouldInterceptRequest");
        return this.handleRequest(webrequest.getUrl().toString());
    }

    @NonNull
    private WebResourceResponse handleRequest(@NonNull String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            return new WebResourceResponse("text/json", "utf-8", inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
