package com.payera.payera01;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class Confirmation extends Activity {

    private String checkSuccess;
    private TextView checkSuccesstxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Intent intent = getIntent();
        checkSuccess = intent.getStringExtra("checkSuccess");


        checkSuccesstxt = (TextView) findViewById(R.id.checkSuccesstxt);
        checkSuccesstxt.setText(checkSuccess);

    }




    public void GoMainPage(View view) {


        Intent intent1 = new Intent(Confirmation.this, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent1);

    }
}













