package com.app.gearvrcontrollerapp;

import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    //  COLORS
    public int getColorBackground(){
        return (isDarkMode()) ? Color.parseColor("#010101") : Color.parseColor("#F2F2F2");
    }
    public int getColorSurface(){
        return (isDarkMode()) ? Color.parseColor("#171717") : Color.parseColor("#FFFFFF");
    }
    public int getColorText(){
        return (isDarkMode()) ? Color.parseColor("#FAFAFA") : Color.parseColor("#252525");
    }
    public int getColorSubText(){
        return (isDarkMode()) ? Color.parseColor("#6A6A6A") : Color.parseColor("#919191");
    }

    public boolean isDarkMode(){
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
                return false;
        }
        return false;
    }

    public GradientDrawable getGradientColorBackground(){
        int[] colors = {Color.TRANSPARENT,getColorBackground()};
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    }
}
