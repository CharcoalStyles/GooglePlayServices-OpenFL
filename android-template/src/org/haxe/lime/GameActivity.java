package org.haxe.lime;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.Math;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import org.haxe.extension.Extension;
import org.haxe.HXCPP;
import java.util.Calendar;
import android.widget.Toast;

////////////////////////////////////////////////////////////////////////
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.graphics.Color;
import com.google.android.gms.ads.*;
import com.google.android.gms.common.api.*;
import com.google.example.games.basegameutils.*;
import com.google.android.gms.games.*;
import com.google.android.gms.games.event.*;
import org.haxe.lime.HaxeObject;
::if (ANDROID_PERMISSIONS != null)::::foreach ANDROID_PERMISSIONS::::if (__current__ == "com.android.vending.BILLING")::
import com.example.android.trivialdrivesample.util.*;
::end::::end::::end::
////////////////////////////////////////////////////////////////////////

public class GameActivity extends BaseGameActivity implements SensorEventListener {
	
	
	private static final int DEVICE_ORIENTATION_UNKNOWN = 0;
	private static final int DEVICE_ORIENTATION_PORTRAIT = 1;
	private static final int DEVICE_ORIENTATION_PORTRAIT_UPSIDE_DOWN = 2;
	private static final int DEVICE_ORIENTATION_LANDSCAPE_RIGHT = 3;
	private static final int DEVICE_ORIENTATION_LANDSCAPE_LEFT = 4;
	private static final int DEVICE_ORIENTATION_FACE_UP = 5;
	private static final int DEVICE_ORIENTATION_FACE_DOWN = 6;
	private static final int DEVICE_ROTATION_0 = 0;
	private static final int DEVICE_ROTATION_90 = 1;
	private static final int DEVICE_ROTATION_180 = 2;
	private static final int DEVICE_ROTATION_270 = 3;
	private static final String GLOBAL_PREF_FILE = "nmeAppPrefs";
	
	private static float[] accelData = new float[3];
	private static GameActivity activity;
	private static AssetManager mAssets;
	private static int bufferedDisplayOrientation = -1;
	private static int bufferedNormalOrientation = -1;
	private static Context mContext;
	private static List<Extension> extensions;
	private static float[] inclinationMatrix = new float[16];
	private static HashMap<String, Class> mLoadedClasses = new HashMap<String, Class>();
	private static float[] magnetData = new float[3];
	private static DisplayMetrics metrics;
	private static float[] orientData = new float[3];
	private static float[] rotationMatrix = new float[16];
	private static SensorManager sensorManager;
	
	public Handler mHandler;
	////////////////////////////////////////////////////////////////////////
	static RelativeLayout adLayout;
	static RelativeLayout.LayoutParams adMobLayoutParams;
	static AdView adView;
	static Boolean adVisible = false, adInitialized = false, adTestMode = false;
	static InterstitialAd interstitial;
	static String testDeviceID;	
	static GameActivity singleton;
	static boolean isUsingIAP = false;
	::if (ANDROID_PERMISSIONS != null)::::foreach ANDROID_PERMISSIONS::::if (__current__ == "com.android.vending.BILLING")::
	public IabHelper mHelper;
	public boolean iapHelperSetup = false;
	private static HaxeObject iapCallback;
	::end::::end::::end::
	////////////////////////////////////////////////////////////////////////
	
	private static MainView mMainView;
	private MainView mView;
	private Sound _sound;
	
	@Override
	protected void onCreate (Bundle state) {
		singleton = this;
		
		::if (ANDROID_PERMISSIONS != null)::::foreach ANDROID_PERMISSIONS::::if (__current__ == "com.android.vending.BILLING")::
		isUsingIAP = true;
		::end::::end::::end::
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Set requested clients (games and cloud save)
		setRequestedClients(BaseGameActivity.CLIENT_ALL);
		
		super.onCreate (state);
		
		activity = this;
		mContext = this;
		mHandler = new Handler ();
		mAssets = getAssets ();
		
		Extension.assetManager = mAssets;
		Extension.callbackHandler = mHandler;
		Extension.mainActivity = this;
		Extension.mainContext = this;
		
		_sound = new Sound (getApplication ());
		//getResources().getAssets();
		
		::if WIN_FULLSCREEN::
			::if (ANDROID_TARGET_SDK_VERSION < 19)::
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
					| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			::end::
		::end::
		
		metrics = new DisplayMetrics ();
		getWindowManager ().getDefaultDisplay ().getMetrics (metrics);
		
		::foreach ndlls::
		System.loadLibrary ("::name::");
		::end::
		HXCPP.run ("ApplicationMain");

		////////////////////////////////////////////////////////////////////////
		FrameLayout rootLayout = new FrameLayout(this); 
		mView = new MainView(getApplication(), this);
		adLayout = new RelativeLayout(this);
        
        RelativeLayout.LayoutParams adMobLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
        rootLayout.addView(mView);
		rootLayout.addView(adLayout, adMobLayoutParams);
		
        setContentView(rootLayout); 
		testDeviceID = "";
		
		////////////////////////////////////////////////////////////////////////
		
		Extension.mainView = mView;
		
		sensorManager = (SensorManager)activity.getSystemService (Context.SENSOR_SERVICE);
		
		if (sensorManager != null) {
			
			sensorManager.registerListener (this, sensorManager.getDefaultSensor (Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
			sensorManager.registerListener (this, sensorManager.getDefaultSensor (Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
			
		}
		
		Extension.packageName = getApplicationContext ().getPackageName ();
		
		if (extensions == null) {
			
			extensions = new ArrayList<Extension>();
			::if (ANDROID_EXTENSIONS != null)::::foreach ANDROID_EXTENSIONS::
			extensions.add (new ::__current__:: ());::end::::end::
			
		}
		
		for (Extension extension : extensions) {
			
			extension.onCreate (state);
			
		}
		
	}
	
	/*
	User Sign-In
	*/
	
	static public void userSignIn()
	{
		singleton.getGameHelper().beginUserInitiatedSignIn();
	}
	static public void userSignOut()
	{
		singleton.getGameHelper().signOut();
	}
	
	static public boolean userLoggedIn() {
		return singleton.getGameHelper().isSignedIn();
	}
	
	/* Achievements */
	static public void unlockAchievemnt(final String achievementId)
	{
		if (singleton.getGameHelper().isSignedIn())
		{
			Games.Achievements.unlock(singleton.getApiClient(), achievementId);
		}
	}
	
	static public void incrementAchievemnt(final String achievementId, final int amout)
	{
		if (singleton.getGameHelper().isSignedIn())
		{
			Games.Achievements.increment(singleton.getApiClient(), achievementId, amout);
		}
	}
	
	
	static public void showAchievements()
	{
		if (singleton.getGameHelper().isSignedIn())
		{
			singleton.startActivityForResult(Games.Achievements.getAchievementsIntent(singleton.getApiClient()), 42);
		}
	}
	
	/* Events */
	static public void incrementEvent(final String event, final int amount)
	{
		if (singleton.getGameHelper().isSignedIn())
		{
			Games.Events.increment(singleton.getApiClient(), event, amount);
		}
	}
	
	/* Leaderboards */
	static public void submitScore(final String board, final int score)
	{
		if (singleton.getGameHelper().isSignedIn())
		{
			Games.Leaderboards.submitScore(singleton.getApiClient(), board, score);
		}
	}
	
	static public void showLeaderboard(final String boardId)
	{
		if (singleton.getGameHelper().isSignedIn())
		{
			singleton.startActivityForResult(Games.Leaderboards.getLeaderboardIntent(singleton.getApiClient(), boardId), 42);
		}
	}
	
	static public void showAllLeaderboards()
	{
		if (singleton.getGameHelper().isSignedIn())
		{
			singleton.startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(singleton.getApiClient()), 42);
		}
	}
	
	/* Logging Helper */
	
	static public void log(int type, String tag, String msg)
	{
		switch(type)
		{
			case 2:
				if (Log.isLoggable(tag, Log.VERBOSE)) {
					Log.v(tag, msg);
					}
				break;
			case 3:
				if (Log.isLoggable(tag, Log.DEBUG)) {
					Log.d(tag, msg);
					}
				break;
			case 4:
				if (Log.isLoggable(tag, Log.INFO)) {
					Log.i(tag, msg);
					}
				break;
			case 5:
				if (Log.isLoggable(tag, Log.WARN)) {
					Log.w(tag, msg);
					}
				break;
			case 6:
				if (Log.isLoggable(tag, Log.ERROR)) {
					Log.e(tag, msg);
					}
				break;
			case 7:
				if (Log.isLoggable(tag, Log.ASSERT)) {
					Log.wtf(tag, msg);
					}
				break;
		}
	}
	
	//IAP
	static public boolean setupIap(String key)
	{
		Log.w("IAP", "Starting In-app Billing setup");
		if (isUsingIAP)
		{			
		   // compute your public key and store it in base64EncodedPublicKey
		   singleton.mHelper = new IabHelper(singleton, key);
		   
		   singleton.mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			   public void onIabSetupFinished(IabResult result) {
				  if (!result.isSuccess()) {
					 // Oh noes, there was a problem.
					 Log.e("IAP", "Problem setting up In-app Billing: " + result);
				  }            
					 // Hooray, IAB is fully set up! 
					 singleton.iapHelperSetup = true;
					 Log.w("IAP", "In-app Billing setup!");
			   }
			});
		}
		
		return singleton.iapHelperSetup;
	}
	
	static public void testReturnIapObject()
	{
		Log.w("IAP", "testReturnIapObject");
		returnIapObject("testProduct", 42, "qwertyuiop");
	}
	
	static public void setupIapCallback(final HaxeObject hxObj)
	{
		iapCallback = hxObj;
	}
	
	static public void returnIapObject(final String productID, final int requestCode, final String payload)
	{
		GameActivity.getInstance().runOnUiThread
		(
			new Runnable()
			{ 
				public void run() 
				{
					iapCallback.call("onPurchase", new Object[] {productID, requestCode, payload});
				}
			}
		);
	}
	
	static public void lauchPurchaseFlow(String productID, int requestCode, String payload)
	{
		try
		{
			singleton.mHelper.launchPurchaseFlow(singleton, productID, requestCode, singleton.mPurchaseFinishedListener, payload);
        }       
        catch(IllegalStateException ex){
            singleton.mHelper.flagEndAsync();
        }
	}
	
	static public void retrievePurchases()
	{
		GameActivity.getInstance().runOnUiThread
		(
			new Runnable()
			{ 
				public void run() 
				{
					try
					{
					singleton.mHelper.queryInventoryAsync(singleton.mGotInventoryListener); 
					}
					catch(IllegalStateException ex){
						singleton.mHelper.flagEndAsync();
					}
				}
			}
		);
	}
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener 
	   = new IabHelper.OnIabPurchaseFinishedListener() {
	   public void onIabPurchaseFinished(IabResult result, Purchase purchase) 
	   {
			if (result.isFailure()) {
				Log.d("IAP", "Error purchasing: " + result);
				return;
			}      
			else {
				returnIapObject(purchase.getSku(), purchase.getPurchaseState(), purchase.getDeveloperPayload());
			}
	   }
	};
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener 
		= new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
			Inventory inventory) {

			if (result.isFailure()) {
				Log.d("IAP", "Error: " + result);
				return;
			}
			else {
				for (Purchase purchase : inventory.getAllPurchases())
				{
					returnIapObject(purchase.getSku(), purchase.getPurchaseState(), purchase.getDeveloperPayload());
				}
			}
			returnIapObject("EOL", 0, "EOL");
		}
	};
	
	////////////////////////////////////////////////////////////////////////
	/*Ad mob functions*/
	static public void loadAd() {
		AdRequest adRequest;
		if (testDeviceID.length() > 0)		
			adRequest= new AdRequest.Builder().addTestDevice(testDeviceID).build();
		else
			adRequest= new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}
	
	static public void initAd(final String id, final int x, final int y, final String testDevice) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				String adID = id;
				
				if(testDevice.length() > 0)
				{
					adTestMode = true;
					testDeviceID = testDevice;
				}
				else
					adTestMode = false;
				
				if (activity == null) {
					return;
				}
				
				adView = new AdView(activity);
				adView.setAdUnitId(adID);
				adView.setAdSize(AdSize.SMART_BANNER);

				loadAd();
				adMobLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
       
                if(x == 0) {
					adMobLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                }
				else if(x == 1) {
					adMobLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
				else if(x == 2) {
					adMobLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                }
				
				if(y == 0) {
					adMobLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				}
				else if(y == 1) {
					adMobLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				}
				else if(y == 2) {
					adMobLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                }
				
				adInitialized = true;
			}
		});
	}
	
	static public void showAd() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (adInitialized && !adVisible) {
					adLayout.removeAllViews();
					adView.setBackgroundColor(Color.BLACK);
					adLayout.addView(adView, adMobLayoutParams);
					adView.setBackgroundColor(0);
					adVisible = true;
				}
			}
		});
	}
        
	static public void hideAd() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (adInitialized && adVisible) {
					adLayout.removeAllViews();
					loadAd();
					adVisible = false;
				}
			}
		});
	}
	
	static public void loadInterstitial() {
		AdRequest adRequest;
		if (testDeviceID.length() > 0)		
			adRequest= new AdRequest.Builder().addTestDevice(testDeviceID).build();
		else
			adRequest= new AdRequest.Builder().build();
		interstitial.loadAd(adRequest);
	}
	
	static public void initInterstitial(final String id, final String testDevice) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
				if (activity == null) {
					return;
				}
				
				if(testDevice.length() > 0)
				{
					adTestMode = true;
					testDeviceID = testDevice;
				}
				else
					adTestMode = false;
				
                interstitial = new InterstitialAd(activity);
                interstitial.setAdUnitId(id);

                loadInterstitial();
            }
        });
    }

    static public void showInterstitial() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (interstitial.isLoaded()) {
                    interstitial.show();
                }
            }
        });
    }
	///////////////////////////////////////////////////////////////////////////////////////////
	
	public static double CapabilitiesGetPixelAspectRatio () {
		
		return metrics.xdpi / metrics.ydpi;
		
	}
	
	
	public static double CapabilitiesGetScreenDPI () {
		
		return metrics.xdpi;
		
	}
	
	
	public static double CapabilitiesGetScreenResolutionX () {
		
		return metrics.widthPixels;
		
	}
	
	
	public static double CapabilitiesGetScreenResolutionY () {
		
		return metrics.heightPixels;
		
	}
	
	
	public static String CapabilitiesGetLanguage () {
		
		return Locale.getDefault ().getLanguage ();
		
	}
	
	
	public static void clearUserPreference (String inId) {
		
		SharedPreferences prefs = activity.getSharedPreferences (GLOBAL_PREF_FILE, MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit ();
		prefEditor.putString (inId, "");
		prefEditor.commit ();
		
	}
	
	
	public void doPause () {
		
		_sound.doPause ();
		
		mView.sendActivity (Lime.DEACTIVATE);
		mView.onPause ();
		
		if (sensorManager != null) {
			
			sensorManager.unregisterListener (this);
			
		}
		
		////////////////////////////////////////////////
		if (adView != null) {
			adView.pause();
		}
		////////////////////////////////////////////////
		
	}
	
	
	public void doResume () {
			
		mView.onResume ();
		
		_sound.doResume ();
		
		mView.sendActivity (Lime.ACTIVATE);
		
		if (sensorManager != null) {
			
			sensorManager.registerListener (this, sensorManager.getDefaultSensor (Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
			sensorManager.registerListener (this, sensorManager.getDefaultSensor (Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
			
		}
	
		////////////////////////////////////////////////
		if (adView != null) {
			adView.resume();
		}
		////////////////////////////////////////////////
		
	}
	
	
	public static AssetManager getAssetManager () {
		
		return mAssets;
		
	}
	
	
	public static Context getContext () {
		
		return mContext;
		
	}
	
	
	public static GameActivity getInstance () {
		
		return activity;
		
	}
	
	
	public static MainView getMainView () {
		
		return activity.mView;
		
	}
	
	
	public static byte[] getResource (String inResource) {
		
		try {
			
			InputStream inputStream = mAssets.open (inResource, AssetManager.ACCESS_BUFFER);
			long length = inputStream.available ();
			byte[] result = new byte[(int)length];
			inputStream.read (result);
			inputStream.close ();
			return result;
			
		} catch (IOException e) {
			
			Log.e ("GameActivity",  "getResource" + ":" + e.toString ());
			
		}
		
		return null;
		
	}
	
	
	public static int getResourceID (String inFilename) {
	
		//::foreach assets::::if (type == "music")::if (inFilename.equals("::id::")) return ::APP_PACKAGE::.R.raw.::flatName::;
		//::end::::end::
		//::foreach assets::::if (type == "sound")::if (inFilename.equals("::id::")) return ::APP_PACKAGE::.R.raw.::flatName::;
		//::end::::end::
		return -1;
		
	}
	
	
	static public String getSpecialDir (int inWhich) {
		
		Log.v ("GameActivity", "Get special Dir " + inWhich);
		File path = null;
		
		switch (inWhich) {
			
			case 0: // App
				return mContext.getPackageCodePath ();
			
			case 1: // Storage
				path = mContext.getFilesDir ();
				break;
			
			case 2: // Desktop
				path = Environment.getDataDirectory ();
				break;
			
			case 3: // Docs
				path = Environment.getExternalStorageDirectory ();
				break;
			
			case 4: // User
				path = mContext.getExternalFilesDir (Environment.DIRECTORY_DOWNLOADS);
				break;
			
		}
		
		return path == null ? "" : path.getAbsolutePath ();
		
	}
	
	
	public static String getUserPreference (String inId) {
		
		SharedPreferences prefs = activity.getSharedPreferences (GLOBAL_PREF_FILE, MODE_PRIVATE);
		return prefs.getString (inId, "");
		
	}
	
	
	public static void launchBrowser (String inURL) {
		
		Intent browserIntent = new Intent (Intent.ACTION_VIEW).setData (Uri.parse (inURL));
		
		try {
			
			activity.startActivity (browserIntent);
			
		} catch (Exception e) {
			
			Log.e ("GameActivity", e.toString ());
			return;
			
		}
		
	}
	
	
	private void loadNewSensorData (SensorEvent event) {
		
		final int type = event.sensor.getType ();
		
		if (type == Sensor.TYPE_ACCELEROMETER) {
			
			accelData = event.values.clone ();
			Lime.onAccelerate (-accelData[0], -accelData[1], accelData[2]);
			
		}
		
		if (type == Sensor.TYPE_MAGNETIC_FIELD) {
			
			magnetData = event.values.clone ();
			//Log.d("GameActivity","new mag: " + magnetData[0] + ", " + magnetData[1] + ", " + magnetData[2]);
			
		}
		
	}
	
	
	@Override public void onAccuracyChanged (Sensor sensor, int accuracy) {
		
		
		
	}
	
	
	@Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		
		for (Extension extension : extensions) {
			
			if (!extension.onActivityResult (requestCode, resultCode, data)) {
				
				return;
				
			}
			
		}
		
		super.onActivityResult (requestCode, resultCode, data);
		
	}
	
	
	@Override protected void onDestroy () {
		
		for (Extension extension : extensions) {
			
			extension.onDestroy ();
			
		}
		
		if (isUsingIAP)
		{
			if (mHelper != null) mHelper.dispose();
			mHelper = null;
		}
		
		// TODO: Wait for result?
		mView.sendActivity (Lime.DESTROY);
		activity = null;
		super.onDestroy ();
		
	}
	
	@Override public void onLowMemory () {
		
		super.onLowMemory ();
		
		for (Extension extension : extensions) {
			
			extension.onLowMemory ();
			
		}
		
	}
	
	
	@Override protected void onNewIntent (final Intent intent) {
		
		for (Extension extension : extensions) {
			
			extension.onNewIntent (intent);
			
		}
		
		super.onNewIntent (intent);
		
	}
	
	@Override protected void onPause () {
		
		doPause ();
		super.onPause ();
		
		for (Extension extension : extensions) {
			
			extension.onPause ();
			
		}
		
	}
	
	
	@Override protected void onRestart () {
		
		super.onRestart ();
		
		for (Extension extension : extensions) {
			
			extension.onRestart ();
			
		}
		
	}
	
	
	@Override protected void onResume () {
		
		super.onResume();
		doResume();
		
		for (Extension extension : extensions) {
			
			extension.onResume ();
			
		}
		
	}
	
	
	@Override public void onSensorChanged (SensorEvent event) {
		
		loadNewSensorData (event);
		
		if (accelData != null && magnetData != null) {
			
			boolean foundRotationMatrix = SensorManager.getRotationMatrix (rotationMatrix, inclinationMatrix, accelData, magnetData);
			
			if (foundRotationMatrix) {
				
				SensorManager.getOrientation (rotationMatrix, orientData);
				Lime.onOrientationUpdate (orientData[0], orientData[1], orientData[2]);
				
			}
			
		}
		
		Lime.onDeviceOrientationUpdate (prepareDeviceOrientation ());
		Lime.onNormalOrientationFound (bufferedNormalOrientation);
		
	}
	
	
	@Override protected void onStart () {
		
		super.onStart();
		
		::if WIN_FULLSCREEN::::if (ANDROID_TARGET_SDK_VERSION >= 16)::
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			
			getWindow().getDecorView().setSystemUiVisibility (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN);
			
		}
		::end::::end::
		
		for (Extension extension : extensions) {
			extension.onStart ();
			
		}
		
	}
	
	
	@Override protected void onStop () {
		
		super.onStop ();
		
		for (Extension extension : extensions) {
			
			extension.onStop ();
			
		}
		
	}
	
	@Override public void onTrimMemory (int level) {
		
		super.onTrimMemory (level);
		
		for (Extension extension : extensions) {
			
			extension.onTrimMemory (level);
			
		}
		
	}
	
	public static void popView () {
		
		activity.setContentView (activity.mView);
		activity.doResume ();
		
	}
	
	
	public static void postUICallback (final long inHandle) {
		
		activity.mHandler.post (new Runnable () {
			
			@Override public void run () {
				
				Lime.onCallback (inHandle);
				
			}
			
		});
		
	}
	
	
	private int prepareDeviceOrientation () {
		
		int rawOrientation = getWindow ().getWindowManager ().getDefaultDisplay ().getOrientation ();
		
		if (rawOrientation != bufferedDisplayOrientation) {
			
			bufferedDisplayOrientation = rawOrientation;
			
		}
		
		int screenOrientation = getResources ().getConfiguration ().orientation;
		int deviceOrientation = DEVICE_ORIENTATION_UNKNOWN;
		
		if (bufferedNormalOrientation < 0) {
			
			switch (screenOrientation) {
				
				case Configuration.ORIENTATION_LANDSCAPE:
					
					switch (bufferedDisplayOrientation) {
						
						case DEVICE_ROTATION_0:
						case DEVICE_ROTATION_180:
							bufferedNormalOrientation = DEVICE_ORIENTATION_LANDSCAPE_LEFT;
							break;
						
						case DEVICE_ROTATION_90:
						case DEVICE_ROTATION_270:
							bufferedNormalOrientation = DEVICE_ORIENTATION_PORTRAIT;
							break;
						
						default:
							bufferedNormalOrientation = DEVICE_ORIENTATION_UNKNOWN;
						
					}
					
					break;
				
				case Configuration.ORIENTATION_PORTRAIT:
					
					switch (bufferedDisplayOrientation) {
						
						case DEVICE_ROTATION_0:
						case DEVICE_ROTATION_180:
							bufferedNormalOrientation = DEVICE_ORIENTATION_PORTRAIT;
							break;
						
						case DEVICE_ROTATION_90:
						case DEVICE_ROTATION_270:
							bufferedNormalOrientation = DEVICE_ORIENTATION_LANDSCAPE_LEFT;
							break;
						
						default:
							bufferedNormalOrientation = DEVICE_ORIENTATION_UNKNOWN;
						
					}
					
					break;
				
				default: // ORIENTATION_SQUARE OR ORIENTATION_UNDEFINED
					bufferedNormalOrientation = DEVICE_ORIENTATION_UNKNOWN;
				
			}
			
		}
		
		switch (screenOrientation) {
			
			case Configuration.ORIENTATION_LANDSCAPE:
				
				switch (bufferedDisplayOrientation) {
					
					case DEVICE_ROTATION_0:
					case DEVICE_ROTATION_270:
						deviceOrientation = DEVICE_ORIENTATION_LANDSCAPE_LEFT;
						break;
					
					case DEVICE_ROTATION_90:
					case DEVICE_ROTATION_180:
						deviceOrientation = DEVICE_ORIENTATION_LANDSCAPE_RIGHT;
						break;
					
					default: // impossible!
						deviceOrientation = DEVICE_ORIENTATION_UNKNOWN;
					
				}
				
				break;
			
			case Configuration.ORIENTATION_PORTRAIT:
				
				switch (bufferedDisplayOrientation) {
					
					case DEVICE_ROTATION_0:
					case DEVICE_ROTATION_90:
						deviceOrientation = DEVICE_ORIENTATION_PORTRAIT;
						break;
					
					case DEVICE_ROTATION_180:
					case DEVICE_ROTATION_270:
						deviceOrientation = DEVICE_ORIENTATION_PORTRAIT_UPSIDE_DOWN;
						break;
					
					default: // impossible!
						deviceOrientation = DEVICE_ORIENTATION_UNKNOWN;
				}
				
				break;
			
			default: // ORIENTATION_SQUARE OR ORIENTATION_UNDEFINED
				deviceOrientation = DEVICE_ORIENTATION_UNKNOWN;
			
		}
		
		return deviceOrientation;
		
	}
	
	
	public static void pushView (View inView) {
		
		activity.doPause ();
		activity.setContentView (inView);
		
	}
	
	
	public void queueRunnable (Runnable runnable) {
		
		Log.e ("GameActivity", "queueing...");
		
	}
	
	
	public static void registerExtension (Extension extension) {
		
		if (extensions.indexOf (extension) == -1) {
			
			extensions.add (extension);
			
		}
		
	}
	
	
	public static void showKeyboard (boolean show) {
		
		if (activity == null) {
			
			return;
			
		}
		
		InputMethodManager mgr = (InputMethodManager)activity.getSystemService (Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow (activity.mView.getWindowToken (), 0);
		
		if (show) {
			
			mgr.toggleSoftInput (InputMethodManager.SHOW_FORCED, 0);
			// On the Nexus One, SHOW_FORCED makes it impossible
			// to manually dismiss the keyboard.
			// On the Droid SHOW_IMPLICIT doesn't bring up the keyboard.
			
		}
		
	}
	
	
	public static void setUserPreference (String inId, String inPreference) {
		
		SharedPreferences prefs = activity.getSharedPreferences (GLOBAL_PREF_FILE, MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = prefs.edit ();
		prefEditor.putString (inId, inPreference);
		prefEditor.commit ();
		
	}
	
	
	public static void vibrate (int period, int duration) {
		
		Vibrator v = (Vibrator)activity.getSystemService (Context.VIBRATOR_SERVICE);
		
		if (period == 0) {
			
			v.vibrate (duration);
			
		} else {
				
			int periodMS = (int)Math.ceil (period / 2);
			int count = (int)Math.ceil ((duration / period) * 2);
			long[] pattern = new long[count];
			
			for (int i = 0; i < count; i++) {
				
				pattern[i] = periodMS;
				
			}
			
			v.vibrate (pattern, -1);
			
		}
		
	}
	
	////////////////////////////////////////////////////
	@Override public void onSignInSucceeded(){
	}
	@Override public void onSignInFailed(){
	}
	
}