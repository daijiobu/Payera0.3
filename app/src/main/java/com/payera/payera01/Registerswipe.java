package com.payera.payera01;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.payera.payera01.applib.AppController;
import com.payera.payera01.swipelib.PatternLockUtils;
import com.payera.payera01.swipelib.PatternUtils;
import com.payera.payera01.swipelib.PatternView;
import com.payera.payera01.swipelib.SetPatternActivitylib;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sumyiren on 15/09/2015.
 */
public class Registerswipe extends SetPatternActivitylib {

    private ProgressDialog pDialog;
    private String emailaddress;
    private String patternstr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        emailaddress = intent.getStringExtra("emailaddress");
        Log.v("emailladdress", emailaddress);


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


    }


    @Override
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        PatternLockUtils.setPattern(pattern, this);
        patternstr = PatternUtils.patternToString(pattern);
        registerswipe(emailaddress, patternstr);

    }

    private void registerswipe(final String emailaddress, final String patternstr ) {
        // Tag used to cancel the request
        String tag_string_req = "req_registerswipe";
        String url = "http://192.168.173.1:8081//Payera//T1Swipe//Swiperegis.php";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Register Response: ", response.toString());


                try {
                    JSONObject jObj = new JSONObject(response);
                    // Double amount = jObj.getDouble("amount");
                    String regissuccess = jObj.getString("Regissuccess");
                    //boolean error = jObj.getBoolean("error");


                    if (regissuccess.compareTo("Success")==0) {
                        Intent intent = new Intent(
                                Registerswipe.this,
                                LoginActivity.class);

                        Log.v("regissuccess",regissuccess);

                        Toast.makeText(getApplicationContext(),
                                "Account created", Toast.LENGTH_LONG).show();

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);


                    } else {

                        // Error occurred in registration. Get the error
                        Intent intent = new Intent(
                                Registerswipe.this,
                                RegisterActivity.class);
                        Toast.makeText(getApplicationContext(),
                                "Error in creating new account, Please try again", Toast.LENGTH_LONG).show();


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Registration Error: ", error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "swiperegis");
                params.put("emailaddress", emailaddress);
                params.put("patternstr", patternstr);
                Log.v("patternstrmap",patternstr);


                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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



