package com.bazaar.smartbazar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

public class Loader extends AppCompatActivity {

    private boolean isBackPressedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);



    }

    @Override
    public void onBackPressed() {
        if (isBackPressedOnce){
            super.onBackPressed();
            this.finishAffinity();
            return;
        }
        Toast.makeText(this, "Press again to exit ", Toast.LENGTH_SHORT).show();
        isBackPressedOnce = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isBackPressedOnce = false;
            }
        }, 2000);
    }
}