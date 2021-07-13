package com.example.microsoftengageapp2021.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.microsoftengageapp2021.R;

public class Startup extends AppCompatActivity {
    Animation top_anim, bottom_anim;
    ImageView logo;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //to remove status bar

        setContentView(R.layout.activity_startup);

        top_anim = AnimationUtils.loadAnimation(this,R.anim.top_anim);
        bottom_anim = AnimationUtils.loadAnimation(this,R.anim.bottom_anim);

        logo= findViewById(R.id.logo);
        title= findViewById(R.id.title);

        logo.setAnimation(top_anim);
        title.setAnimation(bottom_anim);

        int SPLASH_SCREEN = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Startup.this, Login.class);

                //transition from main screen to log in screen
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(logo, "logoTrans");
                pairs[1] = new Pair<View, String>(title, "msgTrans");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                        (Startup.this,pairs);
                startActivity(intent,options.toBundle());
            }
        }, SPLASH_SCREEN);
    }
}