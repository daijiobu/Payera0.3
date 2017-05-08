package com.payera.payera01.applib;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by sumyiren on 27/07/2015.
 */
//create a serversocket
public class MyGPSService extends Service {

    public String emailaddress;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        emailaddress = ((AppController) this.getApplication()).getvariable();




        Log.v("Test", "Create service");


    }

    GPSTracker gps;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Handler handler = new Handler();
        Runnable r = new Runnable() {

            public void run() {

                gps = new GPSTracker(MyGPSService.this);

                //
                    //create socket to server and get input (launch confirmation)

                    if (gps.canGetLocation()) {

                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        Log.d("latitude",Double.toString(latitude));
                       // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                       if (latitude == 0 && longitude == 0){}
                        else{
                        new MyAsyncTask().execute(emailaddress,Double.toString(latitude), Double.toString(longitude));}

                        Log.d("gps","coordinates sent");}
                        else{
                            // can't get location
                            // GPS or Network is not enabled
                            // Ask user to enable GPS/network in settings
                            gps.showSettingsAlert();
                        }

                   // }
                }
            };

        handler.postDelayed(r, 1000*60*1); //echange this??

        Thread t = new Thread(r);
        t.start();
//        try {
//            t.sleep(10000);
//        } catch (InterruptedException e) {
//            System.out.println("Main thread Interrupted");
//        }
        stopSelf();
        return Service.START_NOT_STICKY;
    }





    @Override
    public void onDestroy() {
        // I want to restart this service again in one hour
//        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
//        alarm.set(
//                alarm.RTC_WAKEUP,
//                System.currentTimeMillis() + (1000 * 60 * 1),
//                PendingIntent.getService(this, 0, new Intent(this, MyGPSService.class), 0) //send every 1 min
//        );
    }


    private class MyAsyncTask extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
            postData(params[0], params[1],params[2]);
            return null;
        }

        protected void onPostExecute(Double result) {

            Toast.makeText(getApplicationContext(), "command sent", Toast.LENGTH_LONG).show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        public void postData(String emailaddress ,String latitude, String longitude) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://192.168.173.1:8081//Payera//T1Swipe//GPS.php");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("email_address", emailaddress));
                nameValuePairs.add(new BasicNameValuePair("Latitude", latitude));
                nameValuePairs.add(new BasicNameValuePair("Longitude", longitude));
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



}


