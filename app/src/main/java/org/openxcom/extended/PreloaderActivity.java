package org.openxcom.extended;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import org.openxcom.extended.config.Config;
import org.openxcom.extended.util.FilesystemHelper;

public class PreloaderActivity extends Activity {

	private static final String TAG = "PreloaderActivity";
	
	protected AssetManager assets = null;
	protected TextView preloaderLog = null;
	protected Context context;
	private ProgressDialog pd;
	
	private Config config;

    private static final int REQUEST_WRITE_STORAGE = 112;
    private boolean hasWritePermission = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        config = Config.createInstance(this);
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preloader);
		context = this;
		preloaderLog = (TextView) findViewById(R.id.preloaderLog);
		assets = getAssets();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasWritePermission = Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasWritePermission =
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED;
        } else {
            hasWritePermission = true;
        }
	}
	
	@Override
	protected void onDestroy() {
		if (pd != null) {
			pd.dismiss();
		}
		super.onDestroy();
	}
	
	/**
	 * We have to determine if the user has the latest version
	 * of the OpenXcom data, languages and patches. If not, we'll have to install them.
	 */
    @Override
    protected void onStart() {
        super.onStart();
        if (!hasWritePermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requestAllFilesAccess();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestWriteStorage();
            }
        } else {
            unpackData();
        }
    }

    @TargetApi(30)
    private void requestAllFilesAccess() {
        new AlertDialog.Builder(this)
                .setTitle("Permission required")
                .setMessage("This game stores its data on external storage. " +
                        "Please grant 'All files access' on the next screen.")
                .setPositiveButton("Open Settings", (d, i) -> {
                    Intent intent = new Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_WRITE_STORAGE);
                })
                .setCancelable(false)
                .show();
    }

    @TargetApi(23)
    private void requestWriteStorage()
    {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WRITE_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasWritePermission = Environment.isExternalStorageManager();
            if (hasWritePermission) {
                unpackData();
            } else {
                requestAllFilesAccess();
            }
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    unpackData();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setMessage("This game operates on external data. It cannot function otherwise.")
                                .setTitle("Permission required")
                                .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        requestWriteStorage();
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                }
        }
    }

    private void unpackData() {
        new AsyncTask<Void, String, Void>()
        {
            Set<String> assetContents;

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(context);
                pd.setTitle(getString(R.string.preloader_title));
                pd.setMessage(getString(R.string.preloader_init));
                pd.setCancelable(false);
                pd.setIndeterminate(true);
                Log.i(TAG, "Updating game data...");
                assetContents = new TreeSet<>();
                pd.show();
            }

            public void onProgressUpdate(String... message) {
                /*if (!pd.isShowing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pd.show();
                        }});
                }*/
                pd.setMessage(message[0]);
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                // Only copy old files if necessary
                if (config.hasOldFiles() && !hasGameFiles()) {
                    publishProgress("Copying old game files to new location...");
                    String[] gameFolders = new String[] {"GEODATA",
                            "GEOGRAPH",
                            "MAPS",
                            "ROUTES",
                            "SOUND",
                            "TERRAIN",
                            "UFOGRAPH",
                            "UFOINTRO",
                            "UNITS"};
                    String origPath = config.getOldFilesPath();
                    try {
                        for(String dir : gameFolders) {
                            Log.i(TAG, "Copying " + dir + "...");
                            FilesystemHelper.copyFolder(new File(origPath + "/" + dir),
                                    new File(config.getDataFolderPath() + "/" + dir),
                                    true);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error copying directory!");
                        Log.e(TAG, e.getMessage());
                    }
                }

                // Get a list of baked-in assets
                publishProgress(getString(R.string.preloader_assets_gen));
                try {
                    for (String assetName : assets.list("")) {
                        assetContents.add(assetName);
                        Log.i(TAG, "Adding asset: " + assetName);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error while listing assets!");
                    Log.e(TAG, e.getMessage());
                }
                for (String archiveName : assetContents) {
                    if (archiveName.endsWith(".zip")) {
                        String checksumName = archiveName + ".MD5";
                        String version;
                        String currentVersion = config.getAssetVersion(archiveName);
                        if (assetContents.contains(checksumName)) {
                            byte buffer[] = new byte[1024];
                            try {
                                int length = assets.open(checksumName).read(buffer);
                                version = new String(buffer, 0, length - 2);
                            } catch (IOException e) {
                                Log.e(TAG, "Error while reading file archive checksum!");
                                Log.e(TAG, e.getMessage());
                                version = "undefined";
                            }
                        } else {
                            version = "undefined";
                        }
                        if (!version.equals(currentVersion)) {
                            publishProgress(getString(R.string.preloader_extracting) + archiveName);
                            Log.i(TAG, "Extracting " + archiveName + " to " + config.getDataFolderPath());
                            try {
                                FilesystemHelper.zipExtract(assets.open(archiveName), new File(config.getDataFolderPath()));
                                config.setAssetVersion(archiveName, version);
                            } catch (IOException e) {
                                Log.e(TAG, "Error while extracting " + archiveName);
                                Log.e(TAG, e.getMessage());
                            }
                        } else {
                            Log.i(TAG, "Skipping " + archiveName + " because the version is the same");
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (pd != null) {
                    pd.dismiss();
                }
                Log.i(TAG, "Preloading finished.");
                passExecution();
            }

        }.execute((Void[]) null);
    }
	
	/**
	 * This method gets called by AsyncThread to execute the main activity.
	 * Since this activity can actually be started by another activity,
	 * we should also check the previous intent.
	 */
	protected void passExecution() {
		Intent calledIntent = this.getIntent();
		Bundle extParams = calledIntent.getExtras();
		String calledFrom = null;
		if (extParams != null) {
			calledFrom = extParams.getString("calledFrom");
		}
		if (calledFrom != null)
		{
			Log.i(TAG, "Got extra parameters!");
			Log.i(TAG, "calledFrom: " + calledFrom);
			if (calledFrom.equals("DirsConfigActivity")) {
				// We were started from the file chooser dialog activity,
				// so we should return to it.
				Log.i(TAG, "Called from DirsConfigActivity, returning");
				setResult(0);
				finish();
			}
		} else {
            Intent intent;
            if (hasGameFiles()) {
                Log.i(TAG, "Launching OpenXcom activity");
                intent = new Intent(this, OpenXcom.class);
            } else {
                Log.i(TAG, "Couldn't find game files, launching DirsConfigActivity");
                intent = new Intent(this, DirsConfigActivity.class);
            }
			startActivity(intent);
		}
	}

	/**
	 * Method that (crudely) checks if we have the game files.
	 * @return True if the game files are properly installed.
	 */
	protected boolean hasGameFiles() {
        try {
            ZipInputStream ufo1zip = new ZipInputStream(assets.open("2_UFO.zip"));
            ZipEntry ze = ufo1zip.getNextEntry();
            while (ze != null) {
                if(ze.getName().equals("UFO/TERRAIN/UFO1.PCK")) {
                    return true;
                }
                ze = ufo1zip.getNextEntry();
            }
        } catch (IOException e) {
            Log.w(TAG, "Could not open 2_UFO.zip; did you package your files correctly?");
        }
        return new File(config.getDataFolderPath() + "/UFO/TERRAIN/UFO1.PCK").exists();
	}
}
