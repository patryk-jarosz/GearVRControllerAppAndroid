package com.app.gearvrcontrollerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.VelocityTrackerCompat;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;

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

    private MaterialCardView mCardParent;

    private LinearLayout mLinearParentRoot;

    private OneUICompCategoryView mBtnConnect,mBtnDisconnect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        mBtnConnect = findViewById(R.id.activitymain2_oneuicompcategoryview_connect);
        mBtnDisconnect = findViewById(R.id.activitymain2_oneuicompcategoryview_disconnect);

        mBtnConnect.setOneUICompCategoryViewListener(new OneUICompCategoryView.OneUICompCategoryViewListener() {
            @Override
            public void onClick(OneUICompCategoryView OneUICompCategoryView) {

            }
        });
        mBtnDisconnect.setOneUICompCategoryViewListener(new OneUICompCategoryView.OneUICompCategoryViewListener() {
            @Override
            public void onClick(OneUICompCategoryView OneUICompCategoryView) {

            }
        });

        //final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);

        mLinearParentRoot = findViewById(R.id.activitymain2_linear_parent_root);

        mCardParent = findViewById(R.id.activitymain2_card_parent);

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


    private AppApplication getAppApplication(){
        return (AppApplication) getApplication();
    }


    private void onThemeChange(){
        //  RE-APPLY ALL COLORS
        mParent.setBackgroundColor(Color.TRANSPARENT);//getColorBackground()


        mTxtLinearTop.setTextColor(getAppApplication().getColorText());
        mTxtLinearTop.setBackground(getAppApplication().getGradientColorBackground());

        mTxtToolbar.setTextColor(getAppApplication().getColorText());

        mCardParent.setCardBackgroundColor(getAppApplication().getColorBackground());
        mLinearParentRoot.setBackgroundColor(getAppApplication().getColorBackground());

        mBtnConnect.onThemeChange();
        mBtnDisconnect.onThemeChange();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int mStatusBar = (!getAppApplication().isDarkMode()) ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
            int mNavBar = (!getAppApplication().isDarkMode()) ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0;
            getWindow().getDecorView().setSystemUiVisibility(mStatusBar|mNavBar);
        }
    }







}