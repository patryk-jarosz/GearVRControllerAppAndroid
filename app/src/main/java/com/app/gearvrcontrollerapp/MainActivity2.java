package com.app.gearvrcontrollerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.VelocityTrackerCompat;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

public class MainActivity2 extends AppCompatActivity {
    private AppBarLayout mAppBarLayout;
    private LinearLayout mLinearTop;
    private TextView mTxtToolbar,mTxtLinearTop;
    private CoordinatorLayout mParent;
    private VelocityTracker mVelocityTracker;
    private float mLastAbsVelocityY;
    private static final int FLUNG_DIRECTION_UP = 0;
    private static final int FLUNG_DIRECTION_DOWN = 1;
    private static final int FLUNG_DIRECTION_UNKNOWN = 2;
    private int mLastFlungDirection = FLUNG_DIRECTION_UNKNOWN;

    private OneUIAppBarHelper mOneUIAppBarHelper;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        //final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);

        mParent = findViewById(R.id.activitymain2_coordinatorlayout_parent);
        mLinearTop = findViewById(R.id.lineartop);
        mTxtToolbar = findViewById(R.id.txttoolbar);
        mTxtLinearTop = findViewById(R.id.lineartop_txt);

        mAppBarLayout = findViewById(R.id.appBarLayout);
        mOneUIAppBarHelper = new OneUIAppBarHelper(this, mAppBarLayout, new OneUIAppBarHelper.OneUIAppBarHelperListener() {
            @Override
            public void onScroll(float alphaToolBar, float alphaContentBar) {
                //  APPLY ALPHA - TOOLBAR
                mLinearTop.animate().alpha(alphaToolBar).setDuration(0).start();
                //  APPLY ALPHA - CONTENTBAR
                mTxtToolbar.animate().alpha(alphaContentBar).setDuration(0).start();
            }
        });

        onThemeChange();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(mOneUIAppBarHelper != null){
            boolean mWasConsumed = mOneUIAppBarHelper.onDispatchTouchEvent(ev);
            if(mWasConsumed){
                return true;
            }
        }

        return super.dispatchTouchEvent(ev);
    }


    private boolean isDarkMode(){
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
                return false;
        }
        return false;
    }

    private void onThemeChange(){
        //  RE-APPLY ALL COLORS
        mParent.setBackgroundColor(getColorBackground());

        mTxtLinearTop.setTextColor(getColorText());
        mTxtLinearTop.setBackground(getGradientColorBackground());

        mTxtToolbar.setTextColor(getColorText());
    }

    private int getColorBackground(){
        return (isDarkMode()) ? Color.parseColor("#010101") : Color.parseColor("#F2F2F2");
    }
    private int getColorSurface(){
        return (isDarkMode()) ? Color.parseColor("#171717") : Color.parseColor("#FFFFFF");
    }
    private int getColorText(){
        return (isDarkMode()) ? Color.parseColor("#FAFAFA") : Color.parseColor("#252525");
    }
    private int getColorSubText(){
        return (isDarkMode()) ? Color.parseColor("#6A6A6A") : Color.parseColor("#919191");
    }

    private GradientDrawable getGradientColorBackground(){
        int[] colors = {Color.TRANSPARENT,getColorBackground()};
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    }
}