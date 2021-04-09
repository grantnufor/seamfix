package com.chisomanuforom.mac.seamfix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        if (!isTaskRoot()) {
            finish();
            return;
        }

        Thread timerThread = new Thread(){
            public void run(){//delaying screen for 4 seconds
                try{
                    sleep(4000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{


                    //opening up the Subscriber Enrollment form.
                    Intent intent = new Intent(SplashActivity.this, EnrollActivity.class);
                    startActivity(intent);

                    //finish();//Terminating this splash screen


                }
            }
        };
        timerThread.start();


    }
}