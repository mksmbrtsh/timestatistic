package maximsblog.blogspot.com.timestatistic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import google.drive.helper.DriveSyncController;

/**
 * Created by zx on 02.03.2018.
 */

public class GoogleDriveBackupActivity extends Activity implements View.OnClickListener  {

    private Button mExpBtn;
    private Button mImpBtn;
    private DriveSyncController mDriveSyncController;
    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_SIGN_IN = 0;



    @Override
    protected void onStart() {
        super.onStart();
        signIn();
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                initializeDriveClient(account);
            } catch (ApiException e) {
                showMessage(e.getMessage());
            }
        }
    }

    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    protected void signIn() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_FILE)
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }

    }

    /**
     * Continues the sign-in process, initializing the DriveResourceClient with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        OpenHelper mDbHelper = new OpenHelper(this);
        mDriveSyncController = new DriveSyncController(this, mDbHelper.getDatabaseName(), signInAccount);
        mExpBtn.setEnabled(true);
        mImpBtn.setEnabled(true);
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     *//*
    protected void onDriveClientReady() {
        getDriveClient()
                .requestSync()
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        return initializeGroceryList();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Log.e(TAG, "Unexpected error", e);
                        showMessage(e.getMessage().toString());
                    }
                });
    }
*/
    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive_backup);
        mExpBtn = (Button)findViewById(R.id.imp_btn);
        mExpBtn.setOnClickListener(this);
        mImpBtn = (Button)findViewById(R.id.exp_btn);
        mImpBtn.setOnClickListener(this);
        mExpBtn.setEnabled(false);
        mImpBtn.setEnabled(false);
        // Init the SQLiteOpenHelper

    }

/*
    private Task<Void> saveFile() {
        //Log.d(TAG, "Saving file.");
        // [START reopen_for_write]
        Task<DriveContents> reopenTask =
                getDriveResourceClient().reopenContentsForWrite(mDriveContents);
        // [END reopen_for_write]
        return reopenTask
                .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<DriveContents> task) throws Exception {
                        // [START write_conflict_strategy]
                        DriveContents driveContents = task.getResult();
                        OutputStream outputStream = driveContents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write("test");
                        }
                        // ExecutionOptions define the conflict strategy to be used.
                        // [START execution_options]
                        ExecutionOptions executionOptions =
                                new ExecutionOptions.Builder()
                                        .setNotifyOnCompletion(true)
                                        .setConflictStrategy(
                                                ExecutionOptions.CONFLICT_STRATEGY_KEEP_REMOTE)
                                        .build();
                        return getDriveResourceClient().commitContents(
                                driveContents, null, executionOptions);
                        // [END execution_options]
                        // [END write_conflict_strategy]
                    }
                })
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        showMessage("file_saved");
                       // Log.d(TAG, "Reopening file for read.");
                        return loadContents(mGroceryListFile);
                    }
                });
    }
*/

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.imp_btn){
            mDriveSyncController.onImportDB();
        } else {
            mDriveSyncController.onExportDB();
        }
    }

}
