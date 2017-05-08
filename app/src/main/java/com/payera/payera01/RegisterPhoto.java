package com.payera.payera01;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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


public class RegisterPhoto extends Activity
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
    private Face[] mFaces;

    // The surface view for the camera data
    private SurfaceView mView;

    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;

    // Log all errors:
    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

    //Taking photos
    static ImageView myImage1;
    static ImageView myImage2;
    static ImageView myImage3;
    static ImageView myImage4;
    static File imgFile = null;
    int duration = Toast.LENGTH_LONG;
    int imagecounter = 0; //initializer
    Bitmap myBitmap1;
    Bitmap myBitmap2;
    Bitmap myBitmap3;
    Bitmap myBitmap4;
    static final int MEDIA_TYPE_IMAGE = 1;
    static Context context;
    private ProgressDialog pDialog;
    InputStream inputStream;
    String emailaddress;

    //send photos
    private ImageButton btnsendphotos;
    private int faces_number;

    /**
     * Sets the faces for the overlay view, so it can be updated
     * and the face overlays will be drawn again.
     */
    private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
            Log.d("onFaceDetection", "Number of Faces:" + faces.length);
            faces_number = faces.length;
            // Update the view now!
            mFaceView.setFaces(faces);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerphoto);

        mView = new SurfaceView(this);


        btnsendphotos = (ImageButton) findViewById(R.id.btnsendphotos);

        Intent intent = getIntent();
        emailaddress = intent.getStringExtra("emailaddress");
        Log.v("emailladdress",emailaddress);


//        // Progress dialog
//        pDialog = new ProgressDialog(this);
//        pDialog.setCancelable(false);


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

        myImage2 = (ImageView) findViewById(R.id.lastPic2);
        myImage2.bringToFront();

        myImage3 = (ImageView) findViewById(R.id.lastPic3);
        myImage3.bringToFront();

        myImage4 = (ImageView) findViewById(R.id.lastPic4);
        myImage4.bringToFront();

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rellay);
        relativeLayout.bringToFront();

        Float density = getResources().getDisplayMetrics().density;
        Log.v("density", density.toString());


        btnsendphotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (imagecounter == 4) {
                    ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                    ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    ByteArrayOutputStream stream4 = new ByteArrayOutputStream();
                    myBitmap1.compress(Bitmap.CompressFormat.JPEG, 90, stream1); //compress to which format you want.
                    myBitmap2.compress(Bitmap.CompressFormat.JPEG, 90, stream2);
                    myBitmap3.compress(Bitmap.CompressFormat.JPEG, 90, stream3); //compress to which format you want.
                    myBitmap4.compress(Bitmap.CompressFormat.JPEG, 90, stream4);
                    byte[] byte_arr1 = stream1.toByteArray();
                    byte[] byte_arr2 = stream2.toByteArray();
                    byte[] byte_arr3 = stream3.toByteArray();
                    byte[] byte_arr4 = stream4.toByteArray();
                    String image_str1 = Base64.encodeBytes(byte_arr1);
                    String image_str2 = Base64.encodeBytes(byte_arr2);
                    String image_str3 = Base64.encodeBytes(byte_arr3);
                    String image_str4 = Base64.encodeBytes(byte_arr4);


                    new sendregisphoto(RegisterPhoto.this).execute(emailaddress, image_str1, image_str2, image_str3, image_str4);
                }

            }
        });

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
        mDisplayRotation = Util.getDisplayRotation(RegisterPhoto.this);
        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, 0);

        mCamera.setDisplayOrientation(mDisplayOrientation);

        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(mDisplayOrientation);
        }
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
                    + Util.getDisplayRotation(RegisterPhoto.this);
            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                mFaceView.setOrientation(mOrientationCompensation);
            }
        }
    }

    public void takePhoto(View v){
        if (faces_number == 1){
        mCamera.takePicture(null, null, mPicture);}
        else if (faces_number == 0){
            Toast.makeText(RegisterPhoto.this, "No face detected", Toast.LENGTH_LONG).show();
        }
        else if (faces_number>1){
            Toast.makeText(RegisterPhoto.this, "Too many faces detected", Toast.LENGTH_LONG).show();
        }

    }



    //send registration photos


    private class sendregisphoto extends AsyncTask<String, Integer, Double>  {

        private ProgressDialog pdialog;
        /** application context. */
        private Activity activity;
        private Context context;

        public sendregisphoto(Activity activity) {
            this.activity = activity;
            context = activity;
            pdialog = new ProgressDialog(context);
        }


        @Override
        protected void onPreExecute() {
            this.pdialog.setMessage("Registering photos...");
            this.pdialog.show();
            Log.v("pdialog", "showing");
        }

        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
            postData(params[0], params[1],params[2], params[3], params[4]);

            return null;
        }


        protected void onPostExecute(Double result) {



            Intent intent = new Intent(
                    RegisterPhoto.this,
                    Registerswipe.class);
            intent.putExtra("emailaddress", emailaddress);

            startActivity(intent);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        public void postData(String emailaddress,String image1,String image2,String image3,String image4) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://192.168.173.1:8081//Payera//T1Swipe//FaceDatabase//inputimage.php");
            try {


                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("image1", image1));
                nameValuePairs.add(new BasicNameValuePair("image2", image2));
                nameValuePairs.add(new BasicNameValuePair("image3", image3));
                nameValuePairs.add(new BasicNameValuePair("image4", image4));
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
            setImage();
            Log.v("image","set");

            mCamera.startPreview();
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

    public void setImage(){
        if(imgFile !=null){
            if(imgFile.exists()){


                if (imagecounter == 0 | imagecounter== 4) {
                    myBitmap1 = decodeSampleImage(imgFile, 100, 100); // prevents memory out of memory exception

                    myImage1.setImageBitmap(myBitmap1);
                    myImage1.setRotation(270);
                    myBitmap1 = RotateBitmap(myBitmap1, 270);
                    // myImage1.buildDrawingCache();
                    imagecounter = 1;

                } else if(imagecounter == 1){
                    myBitmap2 = decodeSampleImage(imgFile, 100, 100); // prevents memory out of memory exception2
                    myImage2.setImageBitmap(myBitmap2);
                    myImage2.setRotation(270);
                    myBitmap2 = RotateBitmap(myBitmap2, 270);
                    // myImage1.buildDrawingCache();
                    imagecounter = 2;
                }

                else if (imagecounter == 2) {
                    myBitmap3 = decodeSampleImage(imgFile, 100, 100); // prevents memory out of memory exception

                    myImage3.setImageBitmap(myBitmap3);
                    myImage3.setRotation(270);
                    myBitmap3 = RotateBitmap(myBitmap3, 270);
                    // myImage1.buildDrawingCache();
                    imagecounter = 3;

                } else if(imagecounter == 3){
                    myBitmap4 = decodeSampleImage(imgFile, 100, 100); // prevents memory out of memory exception2
                    myImage4.setImageBitmap(myBitmap4);
                    myImage4.setRotation(270);
                    myBitmap4 = RotateBitmap(myBitmap4, 270);
                    // myImage1.buildDrawingCache();
                    imagecounter = 4;
                }
            }

        }
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


}
