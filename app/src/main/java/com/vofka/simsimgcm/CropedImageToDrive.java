package com.vofka.simsimgcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it
 * in Google Drive. The user is prompted with a pre-made dialog which allows
 * them to choose the file location.
 */
public class CropedImageToDrive  extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = "android-simsimdrive-1030";
    private static final int PICK_IMAGE_REQUEST = 1;
    //   private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int NO_NUM_DIALOG_ID=1;
    public String primary_sd = "";
    public String secondary_sd = "";
    public Bitmap bMap;

    private GoogleApiClient mGoogleApiClient;
    private Bitmap mBitmapToSave;
    private ImageView imageView;
    private ImageView imageView1;
    private ImageView imageView2;
    public String mPhoneNumber="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.croped_vew);
        //   mHelloTextView = (TextView)findViewById(R.id.textView);
        //   mNameEditText=(EditText)findViewById(R.id.editText);
    }
    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
   //     Log.i(TAG, "Creating contents.");
        final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {

                    @Override
                    public void onResult(final DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                 //           Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
            //            Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        // Write the bitmap data from it.
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 90, bitmapStream);


                        try {
                            outputStream.write(bitmapStream.toByteArray());
                        } catch (IOException e1) {
            //                Log.i(TAG, "Unable to write file contents.");
                        }



                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName="SimSim_"+timeStamp;

                        final MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg")
                                .setTitle(imageFileName)
                                .setStarred(true)
                                .build();



                        //   /*
                        // Perform I/O off the UI thread.
                        new Thread() {
                            @Override
                            public void run() {
                                // create a file on root folder
                                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                        .createFile(mGoogleApiClient, metadataChangeSet, result.getDriveContents())
                                        .setResultCallback(fileCallback);
                            }
                        }.start();
                        //   */
                    }
                });
    }

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    showMessage("Created a file with content: " + result.getDriveFile().getDriveId());

                   call();

                }

            };

    @SuppressWarnings("deprecation")
    public void callForHelp() {
        if (mPhoneNumber.length() == 0) {
            showDialog(NO_NUM_DIALOG_ID);
            return;
        }

        final Uri number = Uri.fromParts("tel", mPhoneNumber, "");
        startActivity(new Intent(Intent.ACTION_CALL, number));
    }

    private void call()
    {
        if (mPhoneNumber.length() == 0) {
            //noinspection deprecation
            showDialog(NO_NUM_DIALOG_ID);
            return;
        }

        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener,
                PhoneStateListener.LISTEN_CALL_STATE);

        final Uri number = Uri.fromParts("tel", mPhoneNumber, "");
        startActivity(new Intent(Intent.ACTION_CALL, number));

    //    Intent callIntent = new Intent(Intent.ACTION_CALL);
    //    callIntent.setData(Uri.parse("tel:87471265793"));
    //    startActivity(callIntent);

    }

    private class PhoneCallListener extends PhoneStateListener {

        private boolean isPhoneCalling = false;

        String LOG_TAG = "LOGGING 123";

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Log.i(LOG_TAG, "OFFHOOK");

                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended, need detect flag
                // from CALL_STATE_OFFHOOK
                Log.i(LOG_TAG, "IDLE");

                if (isPhoneCalling) {

                    Log.i(LOG_TAG, "restart app");

                    // restart app

                    intentNoise();
              //     Intent i = getBaseContext().getPackageManager()
             //              .getLaunchIntentForPackage(
            //                        getBaseContext().getPackageName());
             //       i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //        startActivity(i);

                    isPhoneCalling = false;
                }

            }
        }
    }




    private void readApplicationPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPhoneNumber = prefs.getString("alert_phone_number", null);
    }
    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void intentMain() {
        Intent intentMain;
        intentMain = new Intent(this,MainActivity.class);
        startActivity(intentMain);
        finish();
    }

    public void intentNoise() {
        Intent intentNoise;
        intentNoise = new Intent(this,NoiseAlert.class);
        startActivity(intentNoise);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readApplicationPreferences();
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
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
  //      Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
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
    //        Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
   //     Log.i(TAG, "API client connected.");
        if (mBitmapToSave == null) {
            // This activity has no UI of its own. Just start the camera.
            secondary_sd = System.getenv("SECONDARY_STORAGE");


            if(secondary_sd!=null) {
                bMap = BitmapFactory.decodeFile(secondary_sd+"/SimSim Eye/Spyhole/spyhole.jpg");
                cropImageCenter();
            }else{
                bMap = BitmapFactory.decodeFile("/storage/sdcard0/SimSim Eye/Spyhole/spyhole.jpg");
                cropImageCenter();
            }


        //    mBitmapToSave=bMap;

        }
        saveFileToDrive();



    //    finish();


    }

    public void cropImageCenter(){
        int width=bMap.getWidth();
        int height=bMap.getHeight();

        // присваиваем ImageView
    //    imageView.setImageBitmap(bMap);
        ImageView image1 = (ImageView) findViewById(R.id.imageView1);
        ImageView image2 = (ImageView) findViewById(R.id.imageView2);

        // / Половинки
        int halfWidth = width / 2;
        int halfHeight = height / 2;

        // Центр 1/4
        Bitmap bmCenterPartial = Bitmap.createBitmap(halfWidth, halfHeight,
                Bitmap.Config.ARGB_8888);
        int[] pixels = new int[halfWidth * halfHeight];
        bMap.getPixels(pixels, 0, halfWidth, halfWidth / 2,
                halfHeight / 2, halfWidth, halfHeight);
        bmCenterPartial
                .setPixels(pixels, 0, halfWidth, 0, 0, halfWidth, halfHeight);

        image1.setImageBitmap(bMap);
        image2.setImageBitmap(bmCenterPartial);

        mBitmapToSave=bmCenterPartial;
    }

    @Override
    public void onConnectionSuspended(int cause) {
     //   Log.i(TAG, "GoogleApiClient connection suspended");
    }
}