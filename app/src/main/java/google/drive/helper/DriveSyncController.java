package google.drive.helper;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.InputStream;
import java.io.OutputStream;

import maximsblog.blogspot.com.timestatistic.RecordsDbHelper;

/**
 * Created by zx on 03.03.2018.
 */

public class DriveSyncController implements OnSuccessListener, OnFailureListener {
    SyncFileMover mSyncFileMover;
    /**
     * Handles high-level drive functions like sync
     */
    private DriveClient mDriveClient;
    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;

    protected DriveClient getDriveClient() {
        return mDriveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }


    // Instance variables used for DriveFile and DriveContents to help initiate file conflicts.
    protected DriveFile mGroceryListFile;
    protected DriveContents mDriveContents;
    private Context mContext;

    public DriveSyncController(Context context, String fileName, GoogleSignInAccount signInAccount) {
        mContext = context;
        mDriveClient = Drive.getDriveClient(context.getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(context.getApplicationContext(), signInAccount);
        mSyncFileMover = new SyncFileMover(context.getDatabasePath(fileName));
    }

    public void onExportDB() {
        getDriveClient()
                .requestSync()
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        return expDB();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Log.e(TAG, "Unexpected error", e);
                        //showMessage(e.getMessage().toString());
                    }
                });
    }

    public void onImportDB() {
        getDriveClient()
                .requestSync()
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        return impDB();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ContentResolver resolver = mContext.getContentResolver();
                ContentProviderClient client = resolver.acquireContentProviderClient(RecordsDbHelper.AUTHORITY);
                RecordsDbHelper provider = (RecordsDbHelper) client.getLocalContentProvider();
                provider.resetDatabase();
                client.close();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Log.e(TAG, "Unexpected error", e);
                        //showMessage(e.getMessage().toString());
                    }
                });
    }

    public void onSyncDB() {
        getDriveClient()
                .requestSync()
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        return syncDB();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Log.e(TAG, "Unexpected error", e);
                        //showMessage(e.getMessage().toString());
                    }
                });
    }

    private Task<Void> impDB() {
        //Log.d(TAG, "Locating grocery list file");
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE,
                        "TimeStatisticDataBaseBackup.db"))
                .build();
        return getDriveResourceClient()
                .query(query)
                .continueWithTask(new Continuation<MetadataBuffer, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(Task<MetadataBuffer> task)
                            throws Exception {
                        MetadataBuffer metadataBuffer = task.getResult();
                        try {
                            if (metadataBuffer.getCount() == 0) {
                                return null;
                            } else {
                                DriveId id = metadataBuffer.get(0).getDriveId();
                                return Tasks.forResult(id.asDriveFile());
                            }
                        } finally {
                            metadataBuffer.release();
                        }
                    }


                })
                .continueWithTask(new Continuation<DriveFile, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<DriveFile> task) throws Exception {
                        return readDBContentsFromCloud(task.getResult());
                    }
                });
    }

    private boolean compareDriveLocalNewer(Metadata m) {
        long lastLocalUpdate = mSyncFileMover.getLocalModifiedDate();
        long lastDriveUpdate = m.getModifiedDate().getTime();

        if (lastLocalUpdate <= 0) {
            return true;
        }

        if (lastDriveUpdate <= 0) {
            return false;
        }

        return lastDriveUpdate > lastLocalUpdate;
    }

    private Task<Void> expDB() {
        //Log.d(TAG, "Locating grocery list file");
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE,
                        "TimeStatisticDataBaseBackup.db"))
                .build();
        return getDriveResourceClient()
                .query(query)
                .continueWithTask(new Continuation<MetadataBuffer, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(Task<MetadataBuffer> task)
                            throws Exception {
                        MetadataBuffer metadataBuffer = task.getResult();
                        try {
                            if (metadataBuffer.getCount() == 0) {
                                Task<DriveFolder> folder = createNewDBonCloud();
                                return createNewDBfileOnCloud(folder);
                            } else {
                                DriveId id = metadataBuffer.get(0).getDriveId();
                                return Tasks.forResult(id.asDriveFile());
                            }
                        } finally {
                            metadataBuffer.release();
                        }
                    }


                })
                .continueWithTask(new Continuation<DriveFile, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<DriveFile> task) throws Exception {
                        return writeDBContentsToCloud(task.getResult());
                    }
                });
    }
    private Task<DriveFolder> createNewDBonCloud() {
        return getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("TimeStatistic")
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        return getDriveResourceClient().createFolder(parentFolder, changeSet);
                    }
                })
                .addOnSuccessListener(this)
                .addOnFailureListener(this);
    }

    private Task<DriveFile> createNewDBfileOnCloud(Task<DriveFolder> folder) {
        return folder.continueWithTask(
                new Continuation<DriveFolder, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(Task<DriveFolder> task) throws Exception {
                        DriveFolder folder2 = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("TimeStatisticDataBaseBackup.db")
                                .build();

                        return getDriveResourceClient().createFile(folder2, changeSet, null);
                    }
                }).addOnSuccessListener(this)
                .addOnFailureListener(this);
    }

    private Task<Void> writeDBContentsToCloud(DriveFile file) {
        mGroceryListFile = file;
        Task<DriveContents> loadTask =
                getDriveResourceClient().openFile(file, DriveFile.MODE_WRITE_ONLY);
        return loadTask.continueWith(new Continuation<DriveContents, Void>() {
            @Override
            public Void then(Task<DriveContents> task) throws Exception {
                //Log.d(TAG, "Reading file contents");
                mDriveContents = task.getResult();
                OutputStream outputStream = mDriveContents.getOutputStream();
                mSyncFileMover.writeLocalDbToCloudStream(outputStream);
                Task<Void> commitTask =
                        getDriveResourceClient().commitContents(mDriveContents, null);
                return null;
            }
        }).addOnSuccessListener(this).addOnFailureListener(this);
    }

    private Task<Void> readDBContentsFromCloud(DriveFile file) {
        mGroceryListFile = file;
        Task<DriveContents> loadTask =
                getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
        return loadTask.continueWith(new Continuation<DriveContents, Void>() {
            @Override
            public Void then(Task<DriveContents> task) throws Exception {
                //Log.d(TAG, "Reading file contents");
                mDriveContents = task.getResult();
                InputStream inputStream = mDriveContents.getInputStream();
                mSyncFileMover.writeCloudStreamToLocalDb(inputStream);
                //String groceryListStr = getStringFromInputStream(inputStream);

                //showMessage(groceryListStr);
                return null;
            }
        }).addOnSuccessListener(this).addOnFailureListener(this);
    }

    @Override
    public void onFailure(Exception e) {
        Log.e("TimeStatistic", e.toString());
    }

    @Override
    public void onSuccess(Object o) {

    }
}
