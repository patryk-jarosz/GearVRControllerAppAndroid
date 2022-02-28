package com.app.gearvrcontrollerapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;

import androidx.annotation.IntRange;
import androidx.annotation.RequiresApi;

public class ControllerAccessibilityService extends AccessibilityService {
    int
            maxWidth = 0,
            maxHeight = 0;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v("ControllerAccessiblServ","onAccessibilityEvent!");
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        //super.onServiceConnected();
        registerReceiver();
        setupOverlayCursor();

//        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        Display display = window.getDefaultDisplay();
//        maxWidth = display.getWidth();
//        maxHeight = display.getHeight();

//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        int screenWidth = displaymetrics.widthPixels;
//        int screenHeight = displaymetrics.heightPixels;

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        maxWidth = metrics.widthPixels;
        maxHeight = metrics.heightPixels + getStatusBarHeight();
    }

    /** REGISTER RECEIVER   **/
    private void registerReceiver(){
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                boolean mDoBack = arg1.getExtras().getBoolean("onClickBack");
                boolean mDoHome = arg1.getExtras().getBoolean("onClickHome");
                boolean mDoVolUp = arg1.getExtras().getBoolean("onClickVolUp");
                boolean mDoVolDown = arg1.getExtras().getBoolean("onClickVolDown");
                boolean mDoTouchpadClick = arg1.getExtras().getBoolean("onClickTouchpad");
                int mPosX = arg1.getExtras().getInt("touchpadPosX");
                int mPosY = arg1.getExtras().getInt("touchpadPosY");

                if(mDoBack){
                    actionBack();
                }
                if(mDoHome){
                    actionHome();
                }
                if(mDoVolUp){
                    actionVolUp();
                }
                if(mDoVolDown){
                    actionVolDown();
                }
                if(mDoTouchpadClick){
                    Log.v("TAG","actionTouchpadClick");
                    actionTouchpadClick(mPosX,mPosY);
                }

                actionTouchpadXY(mPosX,mPosY);

//                int mPosX = arg1.getExtras().getInt("posX");
//                int mPosY = arg1.getExtras().getInt("posY");


//                boolean mIsPressing = arg1.getExtras().getBoolean("isPressing");
//                int mPosX = arg1.getExtras().getInt("posX");
//                int mPosY = arg1.getExtras().getInt("posY");
//
//                setCursorPos(mPosX,mPosY);
//
//                Log.v("TAG","onReceive");

            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY));
    }
    private static final String INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY = "INTENT_FILTER_ACTION_FROM_ACTIVITY_TO_ACCESSIBILITY";


    private void actionBack(){
        performGlobalAction(GLOBAL_ACTION_BACK);
    }
    private void actionHome(){
        performGlobalAction(GLOBAL_ACTION_HOME);

    }
    private void actionRecents(){
        performGlobalAction(GLOBAL_ACTION_RECENTS);
    }

    private void actionVolUp(){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }
    private void actionVolDown(){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }


    private void actionTouchpadClick(int x, int y){
        Log.v("TAG","actionTouchpadClick:xy="+x+","+y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            x = x/315 * maxWidth;
//            y = y/315 * maxHeight;
            actionTap(x,y);
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("NewApi")
    private void actionTap(int x, int y){
        if(!(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)){
            return;
        }


//        Path swipePath = new Path();
////        swipePath.moveTo(1000, 1000);
////        swipePath.lineTo(100, 1000);
//
//        swipePath.moveTo(x,y);
//
//        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
//        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 1));
//        dispatchGesture(gestureBuilder.build(), null, null);

        float percentageX = x / 315f;
        float percentageY = y / 315f;

        int mCurrentPosX = (int) (maxWidth * percentageX);
        int mCurrentPosY = (int) (maxHeight * percentageY);

        dispatchGesture(createClick(mCurrentPosX,mCurrentPosY),null,null);
    }

    /**
     * Create a description of a click gesture
     *
     * @param x The x coordinate to click. Must not be negative.
     * @param y The y coordinate to click. Must not be negative.
     *
     * @return A description of a click at (x, y)
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static GestureDescription createClick(@IntRange(from = 0) int x,
                                                 @IntRange(from = 0) int y) {

        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        clickPath.lineTo(x + 1, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, ViewConfiguration.getTapTimeout()));
        return gestureBuilder.build();
    }

    // (x, y) in screen coordinates
    @RequiresApi(api = Build.VERSION_CODES.N)
    private static GestureDescription createClick(float x, float y) {
        // for a single tap a duration of 1 ms is enough
        final int DURATION = 1;

        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(clickPath, 10L, 200L);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        return clickBuilder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private AccessibilityService.GestureResultCallback createClickCallback(){
        // callback invoked either when the gesture has been completed or cancelled
        return new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d("TAG", "gesture completed");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d("TAG", "gesture cancelled");
            }
        };
    }


    private void setupOverlayCursor(){

        int flagType;

        //check api type
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            flagType = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;//TYPE_APPLICATION_OVERLAY
        }else{
            flagType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        maxWidth = display.getWidth();
        maxHeight = display.getHeight()+getStatusBarHeight();


        WindowManager.LayoutParams overlayparams;
        overlayparams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                flagType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,// | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        //overlayparams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        overlayparams.x = 0;
        overlayparams.y = 0;



        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.system_overlay,null);
        mCursor = view.findViewById(R.id.imgview_cursor);

        //windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        window.addView(view, overlayparams);

        //listenForMouseCursorPos();
    }
    private ImageView mCursor;

    private void actionTouchpadXY(int x, int y){
//        x = (x/315) * maxWidth;
//        y = (y/315) * maxHeight;
//        mCursor.animate().x(x).y(y).setDuration(0).start();

        float percentageX = x / 315f;
        float percentageY = y / 315f;

        int mCurrentPosX = (int) (maxWidth * percentageX);
        int mCurrentPosY = (int) (maxHeight * percentageY);

        mCursor.animate().x(mCurrentPosX).y(mCurrentPosY).setDuration(0).start();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
