package com.igpsport.testauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class AuthActivity extends AppCompatActivity {


    class JsInvoker {

        @JavascriptInterface
        public void getLoginResult(String result) {

            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("data", result);
            setResult(1000, intent);
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);


        WebView wvAuth = (WebView) findViewById(R.id.wvAuth);
        WebSettings webSettings = wvAuth.getSettings();
        webSettings.setJavaScriptEnabled(true);

        wvAuth.addJavascriptInterface(new JsInvoker(), "app");
        wvAuth.loadUrl(Constants.AUTH_URL);

    }
}
