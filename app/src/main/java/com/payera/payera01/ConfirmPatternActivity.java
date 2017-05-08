/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.payera.payera01;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.payera.payera01.applib.AppController;
import com.payera.payera01.swipelib.BasePatternActivity;
import com.payera.payera01.swipelib.PatternUtils;
import com.payera.payera01.swipelib.PatternView;
import com.payera.payera01.swipelib.ViewAccessibilityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmPatternActivity extends BasePatternActivity
        implements PatternView.OnPatternListener {


    private static final String KEY_NUM_FAILED_ATTEMPTS = "num_failed_attempts";

    public static final int RESULT_FORGOT_PASSWORD = RESULT_FIRST_USER;

    protected int numFailedAttempts;

    private ProgressDialog pDialog;
    private String emailaddress;
    private String amountcredited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        amountcredited = intent.getStringExtra("amountcredited");
        emailaddress = ((AppController) this.getApplication()).getvariable();
        messageText.setText("Insert Pattern");

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        patternView.setOnPatternListener(this);
        leftButton.setText(R.string.pl_cancel);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
        rightButton.setText("Confirm");
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendswipe(emailaddress, getpattern());

            }
        });
        ViewAccessibilityCompat.announceForAccessibility(messageText, messageText.getText());

        if (savedInstanceState == null) {
            numFailedAttempts = 0;
        } else {
            numFailedAttempts = savedInstanceState.getInt(KEY_NUM_FAILED_ATTEMPTS);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_NUM_FAILED_ATTEMPTS, numFailedAttempts);
    }

    @Override
    public void onPatternStart() {

        removeClearPatternRunnable();

        // Set display mode to correct to ensure that pattern can be in stealth mode.
        patternView.setDisplayMode(PatternView.DisplayMode.Correct);
    }

    @Override
    public void onPatternCellAdded(List<PatternView.Cell> pattern) {}

    @Override
    public void onPatternDetected(List<PatternView.Cell> pattern) {

        setpattern(PatternUtils.patternToString(pattern));


    }

    public void sendpattern(){

        String patternstring = getpattern();



    }

    private String patternstr;

    private void setpattern(String patstr) {this.patternstr = patstr;}
    private String getpattern() {return patternstr;}

//        if (isPatternCorrect(pattern)) {
//            onConfirmed();
//        } else {
//            messageText.setText(R.string.pl_wrong_pattern);
//            patternView.setDisplayMode(PatternView.DisplayMode.Wrong);
//            postClearPatternRunnable();
//            ViewAccessibilityCompat.announceForAccessibility(messageText, messageText.getText());
//            onWrongPattern();


    @Override
    public void onPatternCleared() {
        removeClearPatternRunnable();
    }

    protected boolean isStealthModeEnabled() {
        return false;
    }

    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        return true;
    }

    protected void onConfirmed() {
        setResult(RESULT_OK);
        finish();
    }

    protected void onWrongPattern() {
        ++numFailedAttempts;
    }

    protected void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void onForgotPassword() {
        setResult(RESULT_FORGOT_PASSWORD);
        finish();
    }

    private void sendswipe(final String emailaddress, final String patternstr ) {
        // Tag used to cancel the request
        String tag_string_req = "req_registerswipe";
        String url = "http://192.168.173.1:8081//Payera//T1Swipe//confirmpattern.php";

        pDialog.setMessage("Confirming...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Register Response: ", response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    // Double amount = jObj.getDouble("amount");
                    String confirmation = jObj.getString("Confirmation");
                    //boolean error = jObj.getBoolean("error");


                    if (confirmation.compareTo("Success")==0) {
                        Intent intent = new Intent(
                                ConfirmPatternActivity.this,
                                Confirmation.class);
                        intent.putExtra("checkSuccess", "Success");

                        Log.v("patternconfirmation",confirmation);

//                        Toast.makeText(getApplicationContext(),
//                                "Account created", Toast.LENGTH_LONG).show();

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);


                    } else {

                        // Error occurred in registration. Get the error
                        Intent intent = new Intent(
                                ConfirmPatternActivity.this,
                                Confirmation.class);

                        intent.putExtra("checkSuccess", "Failed");
//                        Toast.makeText(getApplicationContext(),
//                                "Error in creating new account, Please try again", Toast.LENGTH_LONG).show();

                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Registration Error: ",error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "confirmpattern");
                params.put("emailaddress", emailaddress);
                params.put("patternstr", patternstr);
                params.put("amountcredited", amountcredited );
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

