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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

// log imports
import android.util.Log;
import android.webkit.ConsoleMessage;

public class MyMobileWebApp extends Activity implements SensorEventListener {
  // Logging stuffs
  // logcat ActivityManager:I MyMobileWebApp:D *:S
  private static final String TAG = "MyMobileWebApp";
  private static final boolean VERBOSE = true;
  private static final boolean DEVELOPMENT = true;

  private String pic = "";
  private static final int PICK_IMAGE_REQUEST_CODE = 1;
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

  private static String url;

  // private static int dX;
  // private static int dY;
  // private static int dZ;

  private static float dX;
  private static float dY;
  private static float dZ;

  private static int pdX;
  private static int pdY;
  private static int pdZ;

  private static int dXX;

  private static final float NS2S = 1.0f / 1000000000.0f;
  private static float[] last_values = null;
  private static float[] velocity = null;
  private static float[] position = null;
  private static long last_timestamp = 0;

  protected LocationManager locationManager;
  private String provider;

  private WebView web;

  private String latitudeS;
  // private SensorManager mSensorManager;
  // private Sensor mAccelerometer;
  private float mLastX, mLastY, mLastZ;
  private boolean mInitialized;
  private SensorManager mSensorManager;
  private Sensor mAccelerometer;
  private final float NOISE = (float) 2.0;

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
      // web.loadUrl("javascript:backflip.initConnect();");
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

    // LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    //   LocationListener ll = new mylocationlistener();
    // lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);

        // locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // LocationListener ll = new mylocationlistener();

        // locationManager.requestLocationUpdates(
        //         LocationManager.GPS_PROVIDER//,
        //         // MINIMUM_TIME_BETWEEN_UPDATES,
        //         // MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
        //         // new mylocationlistener()
        // );
        //
   // Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
   // Log.d(TAG,location.getLatitude()+ "");
    // locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    // Criteria criteria = new Criteria();
    // provider = locationManager.getBestProvider(criteria, false);
    // Location location = locationManager.getLastKnownLocation(provider);
    // if (location != null) {
    //     System.out.println("Provider " + provider + " has been selected.");
    //     onLocationChanged(location);
    //   } else {
    //     System.out.println("Location not avilable");
    //   }
    // LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

    // boolean network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    // Location location;

    // if(network_enabled){
    //    location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

    //   if(location!=null) {
    //     longitude = location.getLongitude();
    //     latitude = location.getLatitude();
    //   }
    // }

    // mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    // mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    mInitialized = false;
    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
  }
  // private class MyLocationListener implements LocationListener {

  //       public void onLocationChanged(Location location) {
  //           String message = String.format(
  //                   "New Location \n Longitude: %1$s \n Latitude: %2$s",
  //                   location.getLongitude(), location.getLatitude()
  //           );
  //           // Toast.makeText(LbsGeocodingActivity.this, message, Toast.LENGTH_LONG).show();
  //       }

  //       public void onStatusChanged(String s, int i, Bundle b) {
  //       //     Toast.makeText(LbsGeocodingActivity.this, "Provider status changed",
  //       //             Toast.LENGTH_LONG).show();
  //       }

  //       public void onProviderDisabled(String s) {
  //           // Toast.makeText(LbsGeocodingActivity.this,
  //           //         "Provider disabled by the user. GPS turned off",
  //           //         Toast.LENGTH_LONG).show();
  //       }

  //       public void onProviderEnabled(String s) {
  //           // Toast.makeText(LbsGeocodingActivity.this,
  //           //         "Provider enabled by the user. GPS turned on",
  //           //         Toast.LENGTH_LONG).show();
  //       }

  // }

  // private class mylocationlistener implements LocationListener {
  //   @Override
  //   public void onLocationChanged(Location location) {
  //       if (location != null) {
  //       Log.d(TAG, location.getLatitude() + "");
  //       Log.d(TAG, location.getLongitude() + "");
  //       // Toast.makeText(MainActivity.this,
  //       //     location.getLatitude() + "" + location.getLongitude(),
  //       //     Toast.LENGTH_LONG).show();
  //       // return location.getLatitude() + "";
  //       }
  //   }
  //   @Override
  //   public void onProviderDisabled(String provider) {
  //   }
  //   @Override
  //   public void onProviderEnabled(String provider) {
  //   }
  //   @Override
  //   public void onStatusChanged(String provider, int status, Bundle extras) {
  //   }
  // }
  //   protected void showCurrentLocation() {
  //     Log.d(TAG,"hello");

  //     Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

  //       // if (location != null) {
  //       //     String message = String.format(
  //       //             "Current Location \n Longitude: %1$s \n Latitude: %2$s",
  //       //             location.getLongitude(), location.getLatitude()
  //       //     );
  //       //     latitudeS = message;
  //       //     web.loadUrl("javascript:app.lattitude()");
  //       // }

  //   };

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

  protected void onResume() {
    super.onResume();
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
  }
  protected void onPause() {
    super.onPause();
    mSensorManager.unregisterListener(this);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // can be safely ignored for this demo
  }

  // static final float NS2S = 1.0f / 1000000000.0f;
  // float[] last_values = null;
  // float[] velocity = null;
  // float[] position = null;
  // long last_timestamp = 0;

  @Override
  public void onSensorChanged(SensorEvent event) {

    // TextView tvX= (TextView)findViewById(R.id.x_axis);
    // TextView tvY= (TextView)findViewById(R.id.y_axis);
    // TextView tvZ= (TextView)findViewById(R.id.z_axis);
    // ImageView iv = (ImageView)findViewById(R.id.image);
    float x = event.values[0];
    float y = event.values[1];
    float z = event.values[2];
    if (!mInitialized) {
      mLastX = x;
      mLastY = y;
      mLastZ = z;
      // mInitialized = true;
    } else {
      // float deltaX = Math.abs(mLastX - x);
      // float deltaY = Math.abs(mLastY - y);
      // float deltaZ = Math.abs(mLastZ - z);

      float deltaX = mLastX - x;
      float deltaY = mLastY - y;
      float deltaZ = mLastZ - z;

      // if (deltaX < NOISE) deltaX = (float)0.0;
      // if (deltaY < NOISE) deltaY = (float)0.0;
      // if (deltaZ < NOISE) deltaZ = (float)0.0;
      mLastX = x;
      mLastY = y;
      mLastZ = z;

      // dX = (int) deltaX;
      // dY = (int) deltaY;
      // dZ = (int) deltaZ;

      dX = x;//deltaX;
      dY = y;//deltaY;
      dZ = z;//deltaZ;

      if ( deltaX != 0 ) {
        // Log.d(TAG, Float.toString(deltaX) );
        web.loadUrl("javascript:app.deltaXX();");
      }
      if ( deltaY != 0 ) {
        // Log.d(TAG, Float.toString(deltaY) );
        web.loadUrl("javascript:app.deltaYY();");
      }
      if ( deltaZ != 0 ) {
        // Log.d(TAG, Float.toString(deltaZ) );
        web.loadUrl("javascript:app.deltaZZ();");
      }

      // if(last_values != null){
      //     float dt = (event.timestamp - last_timestamp) * NS2S;

      //     for(int index = 0; index < 3;++index){
      //         velocity[index] += (event.values[index] + last_values[index])/2 * dt;
      //         position[index] += velocity[index] * dt;
      //     }
      // }
      // else{
      //     last_values = new float[3];
      //     velocity = new float[3];
      //     position = new float[3];
      //     velocity[0] = velocity[1] = velocity[2] = 0f;
      //     position[0] = position[1] = position[2] = 0f;
      // }
      // System.arraycopy(event.values, 0, last_values, 0, 3);
      // last_timestamp = event.timestamp;
      // web.loadUrl("javascript:app.deltaZZ();");

      // Log.d(TAG, dXX.toString() );
      // tvX.setText(Float.toString(deltaX));
      // tvY.setText(Float.toString(deltaY));
      // tvZ.setText(Float.toString(deltaZ));

      if (deltaX > deltaY) {
        // iv.setImageResource(R.drawable.horizontal);
      } else if (deltaY > deltaX) {
        // iv.setImageResource(R.drawable.vertical);
      }
    }
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

    @JavascriptInterface
    public float getDeltaX () {
      return dX;
    }

    @JavascriptInterface
    public float getDeltaY () {
      return dY;
    }

    @JavascriptInterface
    public float getDeltaZ () {
      return dZ;
    }

    @JavascriptInterface
    public void startGet () {
      mInitialized = true;
    }

    @JavascriptInterface
    public void stopGet () {
      mInitialized = false;
    }

    @JavascriptInterface
    public void getLocation () {
      // showCurrentLocation();
    }

    @JavascriptInterface
    public String getLattitude () {
      return latitudeS;
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
