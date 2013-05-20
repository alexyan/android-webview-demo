package com.example.WebView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created with IntelliJ IDEA.
 * User: Alex
 * Date: 13-5-9
 * Time: 下午1:37
 * To change this template use File | Settings | File Templates.
 */

public class HelloWebView extends Activity {
    WebView webview;

    EditText textUrl;

    Button goBack;

    Button go;

    boolean javascriptInterfaceBroken;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * For IDEA :
         *   Tools -> Android -> Monitor(DDMS included)
         *   可以打开LogCat
         */
        //getNetworkInfo
        Log.d("network info",Connectivity.getNetworkInfo(this).toString());

        //This code makes the current Activity Full-Screen. No Status-Bar or anything except the Activity-Window!
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.main);

        //貌似2.3下的方法 4.x已不支持
        //This method is deprecated. This method is now obsolete.
        //Enables platform notifications of data state and proxy changes. Notifications are enabled by default.
        //WebView.enablePlatformNotifications();

        textUrl = (EditText) findViewById(R.id.textUrl);
        webview = (WebView)findViewById(R.id.webview);
        goBack = (Button) findViewById(R.id.goback);
        go = (Button) findViewById(R.id.go);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        /**
         * Can't type inside a Web View
         * http://stackoverflow.com/questions/2653923/cant-type-inside-a-web-view
         */
        webview.requestFocus(View.FOCUS_DOWN);



        webview.setWebViewClient(new HelloWebViewClient());
        //webview.loadUrl("https://mcashier.test.alipay.net/cashier/wapcashier_login.htm");
        webview.loadUrl("http://mobilepp.stable.alipay.net");
        //webview.loadUrl("https://m.alipay.com/appIndex.htm");

        //




        WebSettings settings = webview.getSettings();
        //启用javascript
        settings.setJavaScriptEnabled(true);
        //Sets whether the database storage API is enabled.
        settings.setDatabaseEnabled(true);
        //禁用App缓存
        settings.setAppCacheEnabled(false);
        //Overrides the way the cache is used.
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        //
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabasePath(databasePath);


        // Determine if JavaScript interface is broken.
        // For now, until we have further clarification from the Android team,
        // use version number.
        // 2.3.x下检测到js core为JSC, 并且调用Android Native方法导致应用程序崩溃
        // 4.1.2下js core为V8, 执行正常
        Log.d("android version",Build.VERSION.RELEASE.toString());
        try {
            if ("2.3.3".equals(Build.VERSION.RELEASE)) {
                javascriptInterfaceBroken = true;
            }
        } catch (Exception e) {
            // Ignore, and assume user javascript interface is working correctly.
        }
        // Add javascript interface only if it's not broken
        if (!javascriptInterfaceBroken) {
            webview.addJavascriptInterface(new TheJavascriptInterface(this), "Native");
        }
        //webview.addJavascriptInterface(new TheJavascriptInterface(this),"Native");


        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient(){

            public void onProgressChanged(WebView view, int progress){
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000);
            }


            public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota,
                                                long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
                quotaUpdater.updateQuota(5 * 1024 * 1024);
            }
        });
        /**
         * Thanks to:
         * http://stackoverflow.com/questions/4293965/android-webview-focus-problem
        */
        webview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });


        textUrl.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                // If the event is a key-down event on the "enter" button
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //webview.loadUrl(textUrl.getText().toString());
                    go.performClick();
                    return true;
                }else{
                    return false;
                }


                //return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        goBack.setOnClickListener(new View.OnClickListener(){
             public void onClick(View v){
                if(webview.canGoBack()){
                    webview.goBack();
                }
             }
        });

        go.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                webview.loadUrl(textUrl.getText().toString());
            }
        });

    }

    long startTime;
    long endTime;

    private class HelloWebViewClient extends WebViewClient {

        public boolean shouldOverrideUrlLoading(WebView view, String url){
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //startActivity(intent);
            //return true;
            view.loadUrl(url);
            textUrl.setText(url.toString());
            //Log.d("getUrl",webview.getUrl().toString());
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.i("WEB_VIEW_TEST", "error code:" + errorCode);
            super.onReceivedError(view, errorCode, description, failingUrl);
            //handler.proceed(); // Ignore SSL certificate errors
        }


        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }


        // 开始加载
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //textUrl.setText(webview.getUrl().toString());
            super.onPageStarted(view, url, favicon);
            startTime = System.currentTimeMillis();
        }

        // 结束加载
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            endTime = System.currentTimeMillis();
            Log.e("TAG", "costTime:" + (endTime - startTime));
            //textUrl.setText(webview.getUrl().toString());

            if(javascriptInterfaceBroken){
                /*String handleGingerbreadStupidity=
                        "javascript:function openQuestion(id) { window.location='http://jshandler:openQuestion:'+id; }; "
                                + "javascript: function handler() { this.openQuestion=openQuestion; }; "
                                + "javascript: var jshandler = new handler();";
                view.loadUrl(handleGingerbreadStupidity);*/
            }
        }


        /*
        * (non-Javadoc)
        *
        * @see
        * android.webkit.WebViewClient#onLoadResource(android.webkit.WebView,
        * java.lang.String)
        */
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.i("TAG", "url : " + url);
        }
    }
}