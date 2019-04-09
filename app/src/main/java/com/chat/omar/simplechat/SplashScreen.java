package com.chat.omar.simplechat;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //"splash_screen.xml" is visible in 750 ms
        //A runnable is added to the message queue
        new Handler().postDelayed(new Runnable(){
            //Runnable that changes activity to log in
            @Override
            public void run() {
                Intent logIn = new Intent(SplashScreen.this,LogIn.class);
                SplashScreen.this.startActivity(logIn);
                SplashScreen.this.finish();
            }
        }, 750);
    }
}
