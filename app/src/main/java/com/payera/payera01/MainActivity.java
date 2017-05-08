package com.payera.payera01;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.payera.payera01.applib.AppController;
import com.payera.payera01.applib.MyGPSService;
import com.payera.payera01.applib.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends Activity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;


    private SessionManager session;
    private EditText Amountcredited;
    private UUID MY_UUID;
    private String suuid;

    private RequestQueue mRequestQueue;
    private Button btncheckmoney;

    private BluetoothAdapter mBluetoothAdapter = null;
    // private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int idBlueToothDiscoveryRequest = 1;

    private String emailaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailaddress = ((AppController) this.getApplication()).getvariable();

        if (savedInstanceState != null)
        {
            emailaddress = savedInstanceState.getString("emailaddress");}

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }







        // Displaying the user details on the screen
        txtEmail.setText(emailaddress);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });


        btncheckmoney = (Button) findViewById(R.id.checkmoney);
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());


        //update gps coordinates
        Intent intent1 = new Intent(this, MyGPSService.class);
        startService(intent1);



//        //Intent intent1 = getIntent();
//        // String emailaddress = intent1.getStringExtra("emailaddress"); //get email address from registration
//        emailaddress = ((AppController) this.getApplication()).getvariable();
//
//        Log.d("emailaddress", emailaddress);



        btncheckmoney.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                String url = "http://192.168.173.1:8081//Payera//T1Swipe//checkaccount.php";

                StringRequest strReq = new StringRequest(Request.Method.POST, url
                        , new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Login Response: " ,response.toString());
                        //hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            // Double amount = jObj.getDouble("amount");
                            String amount = jObj.getString("amount");
                            //boolean error = jObj.getBoolean("error");

                            CharSequence text = "You have $" + amount;
                            int duration = Toast.LENGTH_LONG;

                            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                            toast.show();

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Login Error: ", error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to login url
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("tag", "checkamount");
                        params.put("emailaddress", emailaddress);


                        return params;
                    }

                };

                // Adding request to request queue
                //AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

                mRequestQueue.add(strReq);
            }


        } );
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("emailaddress", emailaddress);
    }



    public void Initiate(View view) {
        Amountcredited = (EditText) findViewById(R.id.amountcredited);

        String amountcredited = Amountcredited.getText().toString();

        Intent intent = new Intent(this, Initiator.class);
        intent.putExtra("amountcredited", amountcredited);
        startActivity(intent);
   }


    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

       // db.deleteUsers();

        // Launching the login activity
        ((AppController) this.getApplication()).setvariable(null);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    public void insertmoney(View view){
        Intent intent = new Intent(this, Insertmoney.class);
        startActivity(intent);

    }



    @Override
    protected void onDestroy(){
        super.onDestroy();




    }


}

