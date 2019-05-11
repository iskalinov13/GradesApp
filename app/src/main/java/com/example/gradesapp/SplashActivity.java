package com.example.gradesapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private TextView tv;
    private ImageView iv;
    private ImageView iv2;
    Animation left, right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        iv = findViewById(R.id.iv);
        iv2 = findViewById(R.id.iv2);
        iv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        iv2.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        left = AnimationUtils.loadAnimation(this, R.anim.leftcenter);
        right = AnimationUtils.loadAnimation(this, R.anim.rightcenter);




        final Intent intent = new Intent(this, WelcomeActivity.class);


        right.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                iv.startAnimation(right);
                iv2.startAnimation(left);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv.setLayerType(View.LAYER_TYPE_NONE, null);
                iv2.setLayerType(View.LAYER_TYPE_NONE, null);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        iv.startAnimation(right);
        iv2.startAnimation(left);
    }
}
