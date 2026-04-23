package org.openxcom.extended;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

/**
 * A Runnable to perform actual UI changes; most code moved here from OpenXcom.java clusterfuck.
 *
 */
public class UiVisibilityChanger implements Runnable {

	// UI styles as passed from native code
	private final static int SYSTEM_UI_ALWAYS_SHOWN = 0;
	private final static int SYSTEM_UI_LOW_PROFILE = 1;
	private final static int SYSTEM_UI_IMMERSIVE = 2;

	private int mUiVisibilityFlags = 0;
	private int mUiStyle = SYSTEM_UI_ALWAYS_SHOWN;
	private View mRootView = null;
	private Activity mActivity;
	
	public UiVisibilityChanger(Activity activity , int style) {
		setActivity(activity);
		setRootView(activity.getWindow().getDecorView());
		setUiVisibilityFlags(style);
	}
	
	private void setActivity(Activity activity) {
		mActivity = activity;
	}
	
	/**
	 * Sets rootView to the specified target.
	 * @param rootView New root view.
	 */
	private void setRootView(View rootView) {
		mRootView = rootView;
	}
	
	/**
	 * Sets corresponding UI flags for the requested style;
	 * will fail with an exception if SDK version is too low
	 * or the view is not defined.
	 * @param style Requested style - one of the SYSTEM_UI_ class constants.
	 */
    @TargetApi(16)
    @SuppressWarnings("deprecated")
	public void setUiVisibilityFlags(int style) {
		int version = Build.VERSION.SDK_INT;
		try {
			if (mRootView == null) {
				throw new Exception("rootView is undefined!");
			}
			if (version < 11) {
				throw new Exception("System version is too low!");
			}
			mUiStyle = style;
			switch(style) {
				case SYSTEM_UI_ALWAYS_SHOWN:
					if (version < 14) {
						mUiVisibilityFlags = View.STATUS_BAR_VISIBLE;
					} else {
						mUiVisibilityFlags = View.SYSTEM_UI_FLAG_VISIBLE;
					}
					break;
				case SYSTEM_UI_LOW_PROFILE:
					if (version < 14) {
						mUiVisibilityFlags = View.STATUS_BAR_HIDDEN;
					} else {
						mUiVisibilityFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE;
					}
					break;
				case SYSTEM_UI_IMMERSIVE:
					mUiVisibilityFlags = View.SYSTEM_UI_FLAG_FULLSCREEN
										| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
										| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
			Log.i("OpenXcom", "UiVisibilityChanger::setUiVisibilityFlags: flags set to " + mUiVisibilityFlags);
		}
		catch(Exception e) {
			Log.e("OpenXcom", "UiVisibilityChanger::setUiVisibilityFlags: error: " + e.getMessage());
		}
		
	}
	
	/**
	 * Gets the actual UI visibility flags.
	 * @return actual UI visibility flags.
	 */
	public int getUiVisibilityFlags() {
		return mUiVisibilityFlags;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	/**
	 * Sets UI visibility flags and attaches listener if needed.
	 */
    @SuppressWarnings("deprecated")
	@Override
	public void run() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			applyModernWindowInsets();
		} else if (Build.VERSION.SDK_INT > 10) {
			mRootView.setSystemUiVisibility(mUiVisibilityFlags);
			if ((mUiVisibilityFlags & (View.STATUS_BAR_HIDDEN | View.SYSTEM_UI_FLAG_LOW_PROFILE)) != 0) {
				uiVisibilityChangeListener l = new uiVisibilityChangeListener(mActivity, mUiVisibilityFlags);
				mRootView.setOnSystemUiVisibilityChangeListener(l);
			} else {
				mRootView.setOnSystemUiVisibilityChangeListener(null);
			}
		}
	}

	@TargetApi(30)
	private void applyModernWindowInsets() {
		// Android 15 (targetSdk >= 35) enforces edge-to-edge and ignores
		// Theme.NoTitleBar.Fullscreen. OpenXcom is always a fullscreen game,
		// so always hide system bars. The mUiStyle value only governs whether
		// bars can be revealed transiently by a swipe.
		mActivity.getWindow().setDecorFitsSystemWindows(false);
		WindowInsetsController controller = mActivity.getWindow().getInsetsController();
		if (controller == null) return;
		controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
		controller.setSystemBarsBehavior(
				WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
	}

}

/**
 * A listener that restores UI visibility to its previous state if needed.
 */
@TargetApi(11)
final class uiVisibilityChangeListener implements View.OnSystemUiVisibilityChangeListener {
	
	private Activity mActivity = null;
	private int mUiFlags;
	
	public uiVisibilityChangeListener(Activity activity, int flags) {
		mActivity = activity;
		mUiFlags = flags;
	}
	
	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mActivity.getWindow().getDecorView().setSystemUiVisibility(mUiFlags);
					}});
			}}, 1000);
	}
	
}
