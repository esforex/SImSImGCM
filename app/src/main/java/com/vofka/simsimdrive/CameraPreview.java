package com.vofka.simsimdrive;

/**
 * Created by ${vofka} on ${12/09/2014}.
 */

/**
 * Created by vofka on 9/3/2014.
 */


import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/** A basic Camera preview class */
@SuppressWarnings("ALL")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final Object TAG ="" ;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private SurfaceHolder holder;

    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        ///////////////
        //    setCameraParameters();
        ///////////////////////

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }



    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
//  /        Log.d((String) TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }



    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

//////////////////
        setCameraParameters();

/////////////////////


        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            //          Log.d((String) TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    public void setCameraParameters(){
        // set preview size and make any resize, rotate or
        // reformatting changes here

        Camera.Parameters parameters = mCamera.getParameters();
        //      parameters.set("jpeg-quality", 70);


        //////////////////////////////////////////
        //set color efects to none
        //      parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
        //set antibanding to none
//        if (parameters.getAntibanding() != null) {
        //           parameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
        //       }
        // set white ballance
        //       if (parameters.getWhiteBalance() != null) {
        //           parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
        //       }
        //set flash
        if (parameters.getFlashMode() != null) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        //set zoom
        //      if (parameters.isZoomSupported()) {
        //          parameters.setZoom(0);
        //       }
        //set focus mode
        //    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        ///////////////////////////////

        parameters.setPictureFormat(ImageFormat.JPEG);
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();

        int listSise=sizes.size();

        Camera.Size maxSiseWidth= sizes.get(0);
        Camera.Size minSiseWidth=sizes.get(listSise-1);

        int maxWidth=maxSiseWidth.width;
        int minWidth=minSiseWidth.width;


        Camera.Size size = sizes.get(Integer.valueOf((sizes.size()-1)/2)); //choose a medium resolution


        if((listSise-1)>=8) {


            if (maxWidth < minWidth) {
                //  min order
                maxWidth = minWidth;
                if(maxWidth<3000)
                    parameters.set("jpeg-quality", 90);
                else
                    parameters.set("jpeg-quality", 70);


                size = sizes.get(Integer.valueOf(((sizes.size() - 1) / 2) + ((sizes.size() - 1) / 4))); //choose a higher  resolution in min order
            } else {

                //  max order
                if(maxWidth<3000)
                    parameters.set("jpeg-quality", 90);
                else
                    parameters.set("jpeg-quality", 70);

                size = sizes.get(Integer.valueOf(((sizes.size() - 1) / 2) - ((sizes.size() - 1) / 4))); //choose a higher  resolution in max order
            }
        }

        if((listSise-1)<8) {
            if (maxWidth < minWidth) {
                //  min order
                maxWidth = minWidth;
                if(maxWidth<3000)
                    parameters.set("jpeg-quality", 90);
                else
                    parameters.set("jpeg-quality", 70);

                size = sizes.get(Integer.valueOf(((sizes.size() - 1) / 2) + 1)); //choose a higher  resolution in min order
            } else {
                //  max order
                if(maxWidth<3000)
                    parameters.set("jpeg-quality", 90);
                else
                    parameters.set("jpeg-quality", 70);

                size = sizes.get(Integer.valueOf(((sizes.size() - 1) / 2) - 1)); //choose a higher  resolution in max order
            }
        }

        parameters.setPictureSize(size.width, size.height);
        mCamera.setParameters(parameters);
        //   mCamera.setDisplayOrientation(90);

        //       List<Camera.Size> sizes2 = parameters.getSupportedPreviewSizes();
//        Camera.Size size2 = sizes.get(0);

//        parameters.setPreviewSize(size2.width, size2.height);
    }
}