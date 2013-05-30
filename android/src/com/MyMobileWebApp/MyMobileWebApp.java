package com.MyMobileWebApp;

import android.app.Activity;
// import android.app.*;
import android.os.Bundle;

// Webview imports
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JavascriptInterface;

// camera
import android.provider.MediaStore;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.database.Cursor;
import java.io.ByteArrayOutputStream;
import java.io.File;

// uri methods
import android.net.Uri;

// intent
import android.content.Intent;

// key events
import android.view.KeyEvent;

//context
import android.content.Context;

// log imports
import android.util.Log;
import android.webkit.ConsoleMessage;

public class MyMobileWebApp extends Activity {
  // Logging stuffs
  // logcat ActivityManager:I MyMobileWebApp:D *:S
  private static final String TAG = "MyMobileWebApp";
  private static final boolean VERBOSE = true;
  private static final boolean DEVELOPMENT = true;

  private String pic = "";
  private static final int PICK_IMAGE_REQUEST_CODE = 1;
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

  private static String url;

  private WebView web;

  private WebViewClient webClient = new WebViewClient() {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (Uri.parse(url).getHost().equals(url)) {
        return false;
      }

      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      startActivity(intent);
      return true;
    }

    // WHen the page is finished loading
    public void onPageFinished(WebView view, String url) {
      Log.d(TAG,"Finished Loading Page");
      web.loadUrl("javascript:backflip.initConnect();");
    }
  };

  // Create the activity
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("MyMobileWebApp","On Create");
    // switch the url from dev to production
    if (DEVELOPMENT) {
      // url = "http://172.20.43.113:3000";
      url = "http://192.168.1.2:3000";
    } else {
      url = "https://someproductionurl";
    }

    setContentView(R.layout.main);

    web = (WebView)findViewById(R.id.web);

    // If you want javascript enabled!!
    web.getSettings().setJavaScriptEnabled(true);
    // For local storage/cookies
    web.getSettings().setDomStorageEnabled(true);

    // For exposing native functions to javacsript
    web.addJavascriptInterface(new WebAppInterface(this), "Android");

    web.setWebViewClient(webClient);

    // Load the site
    web.loadUrl(url);

    // for debug purposes to cathc console.log in the adb shell
    web.setWebChromeClient(new WebChromeClient() {
      public boolean onConsoleMessage(ConsoleMessage cm) {
        if (VERBOSE) Log.d(TAG, cm.message() + " -- From line "
                             + cm.lineNumber() + " of "
                             + cm.sourceId() );
        return true;
      }
    });

  }

  @Override
  public void onStop() {
    super.onStop();
    Log.d(TAG,"Stoped");
    // Yay sockets!!!
    // web.loadUrl("javascript:backflip.disconnectSocket();");
  }

  @Override
  public void onRestart() {
    super.onRestart();
    Log.d(TAG,"Restart");
    // Yay sockets!!!
    // web.loadUrl("javascript:backflip.reconnectSocket();");
  }

  // Where you can place native functions to expose them to javascript!
  public class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
      mContext = c;
    }

    @JavascriptInterface
    public void takePicture () {
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
    @JavascriptInterface
    public String getSelectedPicture () {
      return pic;
    }

  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
      web.goBack();
      return true;
    }
    // If it wasn't the Back key or there's no web page history, bubble up to the default
    // system behavior (probably exit the activity)
    return super.onKeyDown(keyCode, event);
  }

    @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
        if (resultCode == Activity.RESULT_OK) {

          final Bitmap bmp = (Bitmap) data.getExtras().get("data");
          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
          byte[] byteArray = stream.toByteArray();

          pic = Base64.encodeToString(byteArray,Base64.DEFAULT);

          Thread t = new Thread() {
            public void run () {
              web.loadUrl("javascript:app.pictureTaken();");
            }
          };
          t.start();
        }
      break;
    }
  }
}
