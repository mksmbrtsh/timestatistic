package google.drive.helper;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by zx on 03.03.2018.
 */

public class SyncFileMover {

    File mFile;

    public SyncFileMover(File file){
        mFile = file;
    }

    public long getLocalModifiedDate(){
        return mFile.lastModified();
    }

    /**
     * Helper method to copy a file from an InputStream to an OutputStream
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     */
    private void fileCopyHelper(InputStream in, OutputStream out) {
        byte[] buffer = new byte[4096];
        int n;

        // IT SURE WOULD BE NICE IF TRY-WITH-RESOURCES WAS SUPPORTED IN OLDER SDK VERSIONS :(
        try {
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.e("IOException", "fileCopyHelper | a stream is null");
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // Squash
            }
            try {
                out.close();
            } catch (IOException e) {
                // Squash
            }
        }
    }

    /**
     * Helper method to write the local SQLite Database to the DriveFile in the AppFolder
     * @param outputStream the OutputStream of the DriveFile to write to
     */
    public boolean writeLocalDbToCloudStream(OutputStream outputStream) {
        InputStream localDbInputStream = null;

        // NOPE, STILL NO TRY-WITH-RESOURCES :((
        try {

            localDbInputStream = new FileInputStream(mFile);
            fileCopyHelper(localDbInputStream, outputStream);

        } catch (FileNotFoundException e) {

            Log.e("Controller", "Local Db file not found");
            return false;
        } finally {

            if (localDbInputStream != null) {
                try {
                    localDbInputStream.close();
                } catch (IOException e) {
                    // Squash
                }
            }
            return true;
        }
    }

    /**
     * Helper method to write the DriveFile Database to the local SQLite Database file
     * @param inputStream the InputStream of the DriveFile to read data from
     */
    public boolean writeCloudStreamToLocalDb(InputStream inputStream) {

        OutputStream localDbOutputStream = null;

        // PLEASE IT WOULD BE SO MUCH NICER :(((  [yes, I know this isn't possible. I'm just complaining]
        try {

            localDbOutputStream = new FileOutputStream(mFile);
            fileCopyHelper(inputStream, localDbOutputStream);

        } catch (FileNotFoundException e) {

            Log.e("Controller", "Local Db file not found");
            return false;
        } finally {

            if (localDbOutputStream != null) {
                try {
                    localDbOutputStream.close();
                } catch (IOException e) {
                    // Squash
                }
            }
            return true;
        }
    }
}
