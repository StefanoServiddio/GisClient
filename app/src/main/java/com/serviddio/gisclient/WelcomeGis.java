package com.serviddio.gisclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class WelcomeGis extends AppCompatActivity {
    Button but;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_gis);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button but= (Button) findViewById(R.id.button);


        but.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v){
                Intent i= new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(i);
            }


        });
    }

}
