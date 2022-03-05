package com.app.gearvrcontrollerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

public class MainActivity2 extends AppCompatActivity {
    private AppBarLayout mAppBarLayout;
    private LinearLayout mLinearTop;
    private TextView mTxtToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);

        mLinearTop = findViewById(R.id.lineartop);
        mTxtToolbar = findViewById(R.id.txttoolbar);

        mAppBarLayout = findViewById(R.id.appBarLayout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //mLinearTop
                float mPercentageCollapsed = (float)Math.abs(verticalOffset) / (appBarLayout.getTotalScrollRange()/2);
                float mPercentageExpanded = 1f - mPercentageCollapsed;
                mLinearTop.animate().alpha(mPercentageExpanded).setDuration(0).start();
//                mTxtToolbar.animate().alpha(mPercentageCollapsed).setDuration(0).start();

                float mCalcMini = mPercentageCollapsed;

                //  .7f -> 0f
                //  1f  -> 1f

                if(mPercentageCollapsed >= .7f){
                    //.3*x=.5
                    //.3x=.5
                    //1.67
                }

                if(mCalcMini <= 0){
                    mCalcMini = 0;
                }
                mTxtToolbar.animate().alpha(mCalcMini).setDuration(0).start();

                if(Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
                    //  Collapsed

                }else{
                    //Expanded

                }
            }
        });
    }
}