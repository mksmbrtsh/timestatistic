package maximsblog.blogspot.com.timestatistic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it
 * in Google Drive. The user is prompted with a pre-made dialog which allows
 * them to choose the file location.
 */
public class GdriveUpload extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = "android-drive-timestatistic";
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private GoogleApiClient mGoogleApiClient;

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
       // final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(new ResultCallback<ContentsResult>() {

            @Override
            public void onResult(ContentsResult result) {
                // If the operation was not successful, we cannot do anything
                // and must
                // fail.
                if (!result.getStatus().isSuccess()) {
                    Log.i(TAG, "Failed to create new contents.");
                    return;
                }
                // Otherwise, we can write our data to the new contents.
                Log.i(TAG, "New contents created.");
                // Get an output stream for the contents.
                OutputStream outputStream = result.getContents().getOutputStream();
                
                OpenHelper o = new OpenHelper(getApplicationContext());
                byte[] buf=null;
				try {
					buf = o.importDatabase(getFilesDir());
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				o.close();
				if(buf == null)
					return;
                try {
                    outputStream.write(buf);
                } catch (IOException e1) {
                    Log.i(TAG, "Unable to write file contents.");
                    return;
                }
                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                String filename = "timestat" + sdf.format(new Date()) + ".db";
                
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setMimeType("*/*").setTitle(filename).build();
                // Create an intent for the file chooser, and start it.
                IntentSender intentSender = Drive.DriveApi
                        .newCreateFileActivityBuilder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialContents(result.getContents())
                        .build(mGoogleApiClient);
                try {
                    startIntentSenderForResult(
                            intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                } catch (SendIntentException e) {
                    Log.i(TAG, "Failed to launch file chooser.");
                }
            }
        });
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	if (mGoogleApiClient == null) {
    		// Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
    	}
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Database timestatistic successfully saved.");
                
    			Toast.makeText(getApplicationContext(), getString(R.string.uploadtogdriveok), Toast.LENGTH_LONG)
    					.show();
                finish();
                } else {
                	Toast.makeText(getApplicationContext(), getString(R.string.erroruploadtogdriveok), Toast.LENGTH_LONG)
					.show();
                }
                finish();
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        saveFileToDrive();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }
}