package com.payera.payera01;
//1.23 initiator comprises of camera initator

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.payera.payera01.applib.AppController;
import com.payera.payera01.applib.Base64;
import com.payera.payera01.applib.CameraErrorCallback;
import com.payera.payera01.applib.FaceOverlayView;
import com.payera.payera01.applib.Util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//code is from sendstring...sends uuid to server
//AND makes sender become discoverable
public class Initiator extends Activity
        implements SurfaceHolder.Callback {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Camera mCamera;

    // We need the phone orientation to correctly draw the overlay:
    private int mOrientation;
    private int mOrientationCompensation;
    private OrientationEventListener mOrientationEventListener;

    // Let's keep track of the display rotation and orientation also:
    private int mDisplayRotation;
    private int mDisplayOrientation;

    // Holds the Face Detection result:
    private Camera.Face[] mFaces;

    // The surface view for the camera data
    private SurfaceView mView;

    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;

    // Log all errors:
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

    //Taking photos
    static ImageView myImage1;
    static File imgFile = null;
    int imagecounter = 0; //initializer
    Bitmap myBitmap1;

    static final int MEDIA_TYPE_IMAGE = 1;
    static Context context;
    private ProgressDialog pDialog;
    private InputStream inputStream;
    private String emailaddress;
    private String amountcredited;

    //send photos
    private Button btnsendphotos;

    /**
     * Sets the faces for the overlay view, so it can be updated
     * and the face overlays will be drawn again.
     */
    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            Log.d("onFaceDetection", "Number of Faces:" + faces.length);
            // Update the view now!
            mFaceView.setFaces(faces);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiator);

        mView = new SurfaceView(this);


        //get emailaddress and amountcredited
        Intent intent = getIntent();
        amountcredited = intent.getStringExtra("amountcredited");
        emailaddress = ((AppController) this.getApplication()).getvariable();



        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        mFaceView = new FaceOverlayView(this);
        // addContentView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mView);
        preview.addView(mFaceView);
        // Create and Start the OrientationListener:
        mOrientationEventListener = new SimpleOrientationEventListener(this);
        mOrientationEventListener.enable();

        myImage1 = (ImageView) findViewById(R.id.lastPic1);
        myImage1.bringToFront();



        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rellay);
        relativeLayout.bringToFront();

        Float density = getResources().getDisplayMetrics().density;
        Log.v("density", density.toString());



    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SurfaceHolder holder = mView.getHolder();
        holder.addCallback(this);
    }

    @Override
    protected void onPause() {
        mOrientationEventListener.disable();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mOrientationEventListener.enable();
        super.onResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            // c = Camera.open(); // attempt to get a Camera instance
            mCamera = openFrontFacingCameraGingerbread();
            mCamera.setDisplayOrientation(90);
        }
        catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        mCamera.setFaceDetectionListener(faceDetectionListener);
        mCamera.startFaceDetection();
        try {
            mCamera.setPreviewDisplay(surfaceHolder);

        } catch (Exception e) {
            Log.e(TAG, "Could not preview the image.", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // We have no surface, return immediately:
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        // Try to stop the current preview:
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore...
        }

        configureCamera(width, height);
        setDisplayOrientation();
        setErrorCallback();

        // Everything is configured! Finally start the camera preview again:
        mCamera.startPreview();
    }

    private void setErrorCallback() {
        mCamera.setErrorCallback(mErrorCallback);
    }

    private void setDisplayOrientation() {
        // Now set the display orientation:
        mDisplayRotation = Util.getDisplayRotation(Initiator.this);
        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, 0);

        mCamera.setDisplayOrientation(mDisplayOrientation);

        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(mDisplayOrientation);
        }
    }


    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
        // Set the PreviewSize and AutoFocus:
        setOptimalPreviewSize(parameters, width, height);
        setAutoFocus(parameters);
        // And set the parameters:


        //mCamera.setParameters(parameters);
        // mCamera.setParameters(parameters);
    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = Util.getOptimalPreviewSize(this, previewSizes, targetRatio);
        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
    }

    private void setAutoFocus(Camera.Parameters cameraParameters) {
        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.setPreviewCallback(null);
        mCamera.setFaceDetectionListener(null);
        mCamera.setErrorCallback(null);
        mCamera.release();
        mCamera = null;
    }

    /**
     * We need to react on OrientationEvents to rotate the screen and
     * update the views.
     */
    private class SimpleOrientationEventListener extends OrientationEventListener {

        public SimpleOrientationEventListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            mOrientation = Util.roundOrientation(orientation, mOrientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + Util.getDisplayRotation(Initiator.this);
            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                mFaceView.setOrientation(mOrientationCompensation);
            }
        }
    }



    private class sendregisphoto extends AsyncTask<String, Integer, Double> {


        /** application context. */
        private Activity activity;
        private Context context;

        public sendregisphoto(Activity activity) {
            this.activity = activity;
            context = activity;

        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
            int imgfilecheck = 0; //check if imgfile exists
            while (imgfilecheck == 0){
                if (imgFile != null) {
                    if (imgFile.exists()) {
                        myBitmap1 = decodeSampleImage(imgFile, 100, 100); // prevents memory out of memory exception
                        myBitmap1 = RotateBitmap(myBitmap1, 270);


                        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();

                        myBitmap1.compress(Bitmap.CompressFormat.PNG, 90, stream1); //compress to which format you want.

                        byte[] byte_arr1 = stream1.toByteArray();
                        String image_str1 = Base64.encodeBytes(byte_arr1);
                        imgfilecheck = 1; //get out of while loop
                        postData(emailaddress, image_str1);
                    }
                }
        }

            return null;
        }


        protected void onPostExecute(Double result) {




        }

        protected void onProgressUpdate(Integer... progress) {

        }

        public void postData(String emailaddress,String image1) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://192.168.173.1:8081//Payera//T1Swipe//InputInitiatorDatabase//inputinitiatorimage.php");
            try {


                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("image1", image1));

                nameValuePairs.add(new BasicNameValuePair("emailaddress", emailaddress));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                Log.d("http","sent");


            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }
    }


    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException {

        String res = "";
        StringBuffer buffer = new StringBuffer();
        inputStream = response.getEntity().getContent();
        final int contentLength = (int) response.getEntity().getContentLength(); //getting content length�..
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //Toast.makeText(InputImage.this, "contentLength : " + contentLength, Toast.LENGTH_LONG).show();
            }
        });

        if (contentLength < 0) {
        } else {
            byte[] data = new byte[512];
            int len = 0;
            try {
                while (-1 != (len = inputStream.read(data))) {
                    buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer�..
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close(); // closing the stream�..
            } catch (IOException e) {
                e.printStackTrace();
            }
            res = buffer.toString();     // converting stringbuffer to string�..

//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(UploadImage.this, "Result : " + res, Toast.LENGTH_LONG).show();
//                }
//            });
            //System.out.println("Response => " +  EntityUtils.toString(response.getEntity()));
        }
        return res;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            if (pictureFile == null){
                Log.d("ERROR", "Error creating media file, check storage permissions:");
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

            } catch (FileNotFoundException e) {
                Log.d("ERROR", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("ERROR", "Error accessing file: " + e.getMessage());
            }

            Log.v("image", "set");


        }
    };

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){

        Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(!isSDPresent)
        {
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, "card not mounted", duration);
            toast.show();

            Log.d("ERROR", "Card not mounted");
        }
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/cameraSpeed/");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){

                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
            imgFile = mediaFile;
        } else {
            return null;
        }

        return mediaFile;
    }





    public static Bitmap decodeSampleImage(File f, int width, int height) {
        try {
            System.gc(); // First of all free some memory 	        // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o); 	        // The new size we want to scale to 	                            final int requiredWidth = width;
            final int requiredHeight = height; 	        // Find the scale value (as a power of 2)
            int sampleScaleSize = 1;
            while (o.outWidth / sampleScaleSize / 2 >= requiredHeight && o.outHeight / sampleScaleSize / 2 >= requiredHeight)
                sampleScaleSize *= 2;
//note requiredWidth changed to requiredheight
            // Decode with inSampleSize

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sampleScaleSize;

            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (Exception e) {
            //  Log.d(TAG, e.getMessage()); // We don't want the application to just throw an exception
        }

        return null;
    }


    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx<cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("Your_TAG", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        return cam;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mCamera.takePicture(null, null, mPicture);


        //




                new sendregisphoto(Initiator.this).execute();


                Intent intent = new Intent(
                        Initiator.this,
                        ConfirmPatternActivity.class);

                 intent.putExtra("amountcredited", amountcredited);
                startActivity(intent);

          //  }


      //  }
        return true;
    }
}




