package com.app.gearvrcontrollerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;

public class InitialActivity extends AppCompatActivity {
    private OneUIAppBarHelper mOneUIAppBarHelper;

    private AppBarLayout mAppBarLayout;
    private TextView mTxtExpandedTitle,mTxtCollapsedTitle;

    private LinearLayout mLinearParent,mLinearExpanded,mLinearCollapsed;

    private MaterialCardView mCardParent,mCardContentWrapper0,mCardContentWrapper1,
    mCardDivider0,mCardDivider1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        mLinearParent = findViewById(R.id.activity_initial_linear_parent);
        mCardParent = findViewById(R.id.activity_initial_card_parent);

        mCardContentWrapper0 = findViewById(R.id.activity_initial_content_card_wrapper_0);
        mCardContentWrapper1 = findViewById(R.id.activity_initial_content_card_wrapper_1);

        mCardDivider0 = findViewById(R.id.activity_initial_content_card_divider_0);
        mCardDivider1 = findViewById(R.id.activity_initial_content_card_divider_1);


        mAppBarLayout = findViewById(R.id.activity_initial_appbarlayout);
        mLinearExpanded = findViewById(R.id.activity_initial_linear_expanded);
        mLinearCollapsed = findViewById(R.id.activity_initial_linear_collapsed);
        mTxtExpandedTitle = findViewById(R.id.activity_initial_text_expanded_title);
        mTxtCollapsedTitle = findViewById(R.id.activity_initial_text_collapsed_title);

        mOneUIAppBarHelper = new OneUIAppBarHelper(this, mAppBarLayout, new OneUIAppBarHelper.OneUIAppBarHelperListener() {
            @Override
            public void onScroll(float alphaToolBar, float alphaContentBar) {
                //  APPLY ALPHA - TOOLBAR
                mLinearExpanded.animate().alpha(alphaToolBar).setDuration(0).start();
                //  APPLY ALPHA - CONTENTBAR
                mLinearCollapsed.animate().alpha(alphaContentBar).setDuration(0).start();
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
        return (AppApplication)getApplication();
    }



    private void onThemeChange(){
        //getAppApplication().getColorBackground()
        mLinearParent.setBackgroundColor(getAppApplication().getColorBackground());
        mCardParent.setCardBackgroundColor(getAppApplication().getColorBackground());

        mTxtExpandedTitle.setTextColor(getAppApplication().getColorText());
        mTxtCollapsedTitle.setTextColor(getAppApplication().getColorText());

        mTxtExpandedTitle.setBackground(getAppApplication().getGradientColorBackground());

        mCardContentWrapper0.setCardBackgroundColor(getAppApplication().getColorSurface());
        mCardContentWrapper1.setCardBackgroundColor(getAppApplication().getColorSurface());

        mCardDivider0.setCardBackgroundColor(getAppApplication().getColorSubText());
        mCardDivider1.setCardBackgroundColor(getAppApplication().getColorSubText());


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int mStatusBar = (!getAppApplication().isDarkMode()) ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
            int mNavBar = (!getAppApplication().isDarkMode()) ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0;
            getWindow().getDecorView().setSystemUiVisibility(mStatusBar|mNavBar);
        }
        getWindow().setNavigationBarColor(getAppApplication().getColorBackground());
        getWindow().getDecorView().setBackgroundColor(getAppApplication().getColorBackground());

    }

}