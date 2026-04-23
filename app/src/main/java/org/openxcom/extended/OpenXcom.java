package org.openxcom.extended;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Locale;

import org.libsdl.app.SDLActivity;
import org.openxcom.extended.UiVisibilityChanger;
import org.openxcom.extended.config.Config;
//import org.openxcom.extended.DirsConfigActivity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;

/*
 * OpenXcom-specific additions to SDLActivity.
 *
 */


public class OpenXcom extends SDLActivity { 

	private final static String TAG = "OpenXcom";

	protected UiVisibilityChanger uiVisibilityChanger = null;

	private Config config;

	@Override
	protected String[] getArguments() {
		final String locale = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry().toLowerCase();
		return new String[] {
			"-locale", locale,
			"-data",   config.getUseDataCache() ? config.getExternalFilesDir().getAbsolutePath() : config.getDataFolderPath(),
			"-user",   config.getUseSaveCache() ? config.getExternalFilesDir().getAbsolutePath() : config.getSaveFolderPath(),
			"-cfg",    config.getUseConfCache() ? config.getExternalFilesDir().getAbsolutePath() : config.getConfFolderPath()};
	}

	@Override
	protected void onCreate(Bundle savedInstance) {
		config = Config.getInstance();
		if (Config.getInstance() == null) {
			config = Config.createInstance(this);
		}
		uiVisibilityChanger = new UiVisibilityChanger(this, config.getSystemUiStyle());
		setSystemUI();
		super.onCreate(savedInstance);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Set UI a bit later, so that SDL would get the resize event
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
			setSystemUI();
			}
		}, 1000);
	}

	public void setSystemUI() {
		uiVisibilityChanger.setUiVisibilityFlags(config.getSystemUiStyle());
		runOnUiThread(uiVisibilityChanger);			
	}

    /**
     * This method is called from JNI.
     * It launches a new configuration activity in case there's a problem
     * while loading game resources.
     */
	public void showDirDialog() {
		Log.i(TAG, "Starting directory configuration dialog...");
		Intent intent = new Intent(this, DirsConfigActivity.class);
		this.startActivityForResult(intent, 0);
	}

    /**
     * This method is called from JNI.
     * It sets the requested UI visibility style and saves the selected preference.
     * @param newSystemUIStyle Requested UI visibility style.
     */
	public void changeSystemUI(int newSystemUIStyle) {
		Log.i(TAG, "Changing system UI");
		Log.i(TAG, "New style is: " + newSystemUIStyle);
        config.setSystemUiStyle(newSystemUIStyle);
        config.save();
		setSystemUI();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		nativeSetPaths(config.getDataFolderPath(),
                       config.getSaveFolderPath(),
                       config.getConfFolderPath());
	}
	
	public static native void nativeSetPaths(String dataPath, String savePath, String confPath);

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) setSystemUI();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			newConfig.orientation = Configuration.ORIENTATION_LANDSCAPE;
		}

		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
	}

	@Override
	protected String[] getLibraries() {
		return new String[] {
				"openxcom"
		};
	}
}



