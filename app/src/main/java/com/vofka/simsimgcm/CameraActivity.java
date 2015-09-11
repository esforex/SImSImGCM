package com.vofka.simsimgcm;

/**
 * Created by ${vofka} on ${12/09/2014}.
 */

/**
 * Created by vofka on 8/25/2014.
 */

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.vofka.simsimgcm.R.id.camera_preview;




@SuppressWarnings("ALL")
public class CameraActivity extends Activity {
    public static final int MEDIA_TYPE_IMAGE = 100;
    public static final int MEDIA_TYPE_VIDEO = 200;
    private Uri fileUri ;
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private Handler mHandler = new Handler();
    private static final int WAIT_BETWEEN_PICTURE_INTERVAL =2000;  //10 sec
    private static final int WAIT_CAMERA_ACTIVITY_INTERVAL =8000;  //10 sec
    Bitmap bitmapOrg;
    public String primary_sd = "";
    public String secondary_sd = "";
    public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";
    private final String OUTPUT_FILE = primary_sd+"/SimSim Eye/Spyhole/spyhole.jpg";
    private final String OUTPUT_FILE1 =secondary_sd+"/SimSim Eye/Spyhole/spyhole.jpg";
    int width;
    int height;
    ImageView imageView;
    public Boolean cameraActivityFinished=false;
    private PowerManager.WakeLock mWakeLock;
    // get Camera parameters
    // drive parameters
    private GoogleApiClient mGoogleApiClient;
    public byte[] dataIMG;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "CameraActivity");
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        //      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        setContentView(R.layout.camera_preview);
        Button button_capture = (Button) findViewById(R.id.button_capture);
        button_capture.performClick();

        final Timer t = new Timer(); t.schedule(new TimerTask() { @Override  public void run() { mCamera.takePicture(null, null, mPicture); t.cancel(); } },WAIT_BETWEEN_PICTURE_INTERVAL);
    }

    //
  /*
  //retrieve a reference to the UI button
Button captureBtn = (Button)findViewById(R.id.capture_btn);
//handle button clicks
captureBtn.setOnClickListener(this);
 */
    @Override
    public void onStart(){
        super.onStart();
        new PostCameraActivityTask().execute();
    }


    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static boolean isSdPresent() {

        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        public static final String TAG = "";

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile;
            //    getCacheDir();
            //     if(isSdPresent()) {
            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            //      }else
            //          pictureFile=null;

            if (pictureFile == null){

                Toast.makeText(getApplicationContext(), "The file is NOT written ", Toast.LENGTH_SHORT).show();
                //        Log.d(TAG, "Error creating media file, check storage permissions: " );
                //         Toast.makeText(getApplicationContext(), "It seems SDcard is not present on the device." +
                //              " Sorry,the application will be closed until you mount it.", Toast.LENGTH_SHORT).show();
                //       amKillProcess("com.vofka.simsimeye");
                // + e.getMessage()
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                //         String externalStorage=System.getenv("EXTERNAL_STORAGE");
                //          String secondaryStorage=System.getenv("SECONDARY_STORAGE");
                Toast.makeText(getApplicationContext(), "The picture is taken", Toast.LENGTH_SHORT).show();
                //    Toast.makeText(getApplicationContext(),externalStorage , Toast.LENGTH_SHORT).show();
                //    Toast.makeText(getApplicationContext(),secondaryStorage, Toast.LENGTH_SHORT).show();




            } catch (FileNotFoundException e) {
                //          Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                //           Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            intentImageToDrive();
        }
    };

    public void intentImageToDrive() {
        Intent intentImageToDrive;
        intentImageToDrive = new Intent(this,CropedImageToDrive.class);
        startActivity(intentImageToDrive);
     //   finish();
    }

    /** Create a file Uri for saving an image or video */
    //   private static Uri getOutputMediaFileUri(int type){
    private  Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public void getDir(){
        File f  =new File(getFilesDir(),Environment.DIRECTORY_PICTURES);
    }

    /** Create a File for saving an image or video */
    //  private static File getOutputMediaFile(int type){

    private  File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SimSim");

        primary_sd = System.getenv("EXTERNAL_STORAGE");
        secondary_sd = System.getenv("SECONDARY_STORAGE");
        File dirMicroSD = new File(secondary_sd, "SimSim Eye");
        File dirInnerSD = new File(primary_sd, "SimSim Eye");

        if (!dirMicroSD.exists() && !dirMicroSD.mkdirs()) {
            File file = new File(OUTPUT_FILE);
            // Clear old file if it exists before recording.
            if (file.exists()) {
//            Log.d(TAG, "Before recording, output file exists, delete it.");
                file.delete();
            }
            File dirInnerSDSpyhole = new File(dirInnerSD, "Spyhole");
            dirInnerSD=dirInnerSDSpyhole;
            mediaStorageDir=dirInnerSD;
        //    Log.d("SimSim_microsd", dirInnerSD.getAbsolutePath());
        }else{
            File file = new File(OUTPUT_FILE1);
            // Clear old file if it exists before recording.
            if (file.exists()) {
//            Log.d(TAG, "Before recording, output file exists, delete it.");
                file.delete();
            }
            File dirMicroSDSpyhole = new File(dirMicroSD, "Spyhole");
            dirMicroSD=dirMicroSDSpyhole;
            mediaStorageDir=dirMicroSD;
         //   Log.d("SimSim_microsd", dirMicroSD.getAbsolutePath());

        }

//   sdcard0
        //    String secondaryStorage=System.getenv("SECONDARY_STORAGE");
        //     String secondaryStorage1=(secondaryStorage+"/Pictures"+"/SimSim");
        //     File mediaStorageDir1 =
        //              new File(secondaryStorage1);
        //     mediaStorageDir=mediaStorageDir1;

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                //            Log.d("SimSim", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                      "spyhole.jpg");
               //     "SimSim_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;


    }

    @Override
    protected void onPause() {
        super.onPause();

        //    new PostCameraActivityTask().execute();

        //   releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
        //      cameraActivityFinished=true;
        onStop();
    }

    @Override
    public void onStop() {
        super.onStop();
        onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //     Toast.makeText(getApplicationContext(), "SimSim Camera. onDestroy()", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void releaseMediaRecorder(){

        MediaRecorder mMediaRecorder=new MediaRecorder();

        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** Called when the user touches the button */
    public void startCam(View view) {
        setContentView(R.layout.camera_preview);
    }

    public void capture(View v) {
        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(camera_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        Button button_capture = (Button) findViewById(R.id.button_capture);
        button_capture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null,null,null,mPicture);
                    }
                }
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //    if (id == R.id.action_settings) {
        //         return true;
        //     }

        if (id == R.id.exit) {

            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }

            //       getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //    amKillProcess("com.vofka.simsimeye");
            amKillProcess("com.vofka.simsimeye");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void amKillProcess(String process)
    {
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo runningProcess : runningProcesses)
        {
            if(runningProcess.processName.equals(process))
            {
                android.os.Process.sendSignal(runningProcess.pid, android.os.Process.SIGNAL_KILL);
            }
        }
    }



    private class PostCameraActivityTask extends AsyncTask<Boolean, Integer, Boolean> {
        protected boolean doInBackground(Boolean cameraActivityFinished) {
            return cameraActivityFinished;
        }

        protected void onProgressUpdate(Integer... progress) {
            setProgressPercent(progress[0]);
        }

        private void setProgressPercent(Integer progres) {
        }

        @Override
        protected Boolean doInBackground(Boolean... booleans) {

            Thread thread=new Thread();
            try {

                //       if(cameraActivityFinished)
                //          PostCameraActivityTask.this.cancel(true);

                thread.sleep(WAIT_CAMERA_ACTIVITY_INTERVAL);
                PostCameraActivityTask.this.cancel(true);
                finish();


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return cameraActivityFinished;
        }

        protected void onPostExecute(Boolean cameraActivityFinished)
        {
            if(!cameraActivityFinished) {
                finish();
            }
        }
    }

    public int getScreenOrientation()
    {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }
}
