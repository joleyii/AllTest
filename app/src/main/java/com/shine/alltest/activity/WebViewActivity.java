package com.shine.alltest.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.shine.alltest.R;

/**
 * Created by 123 on 2017/5/22.
 */

public class WebViewActivity extends Activity {
    private WebView contentWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        String a = "111";
        String b = "222";
        String c = "";
        c = a + b;
        Log.d("eeeeee", c);
        // 启用javascript
        contentWebView = (WebView) findViewById(R.id.web);
        contentWebView.getSettings().setJavaScriptEnabled(true);
        // 从assets目录下面的加载html
        contentWebView.loadUrl("http://172.168.66.61/ws/client.htm?ip=1.1.1.1");
        contentWebView.addJavascriptInterface(WebViewActivity.this, "android");

    }

    @JavascriptInterface
    public void startFunction() {
        Log.d("webgettttt", "");
    }

    @JavascriptInterface
    public void startFunction(final String text) {
        Log.d("webgettttt", text + "");
    }
}
