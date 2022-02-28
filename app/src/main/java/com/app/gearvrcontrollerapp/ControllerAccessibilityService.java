package com.app.gearvrcontrollerapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

public class ControllerAccessibilityService extends AccessibilityService {

    boolean mIsInitialEvent = true;


    int maxWidth=0, maxHeight=0;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v("ControllerAccessiblServ","onAccessibilityEvent!");
//        dispatchGesture()
//        if(mIsInitialEvent){
//            mIsInitialEvent=false;
//            performGlobalAction(GLOBAL_ACTION_RECENTS);
//            Log.v("ControllerAccessiblServ","mIsInitialEvent!");
//        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        //super.onServiceConnected();
        registerReceiver();

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        maxWidth = display.getWidth();
        maxHeight = display.getHeight();

//        // Set the type of events that this service wants to listen to. Others
//        // won't be passed to this service.
//        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
//                AccessibilityEvent.TYPE_VIEW_FOCUSED;
//
//        // If you only want this service to work with specific applications, set their
//        // package names here. Otherwise, when the service is activated, it will listen
//        // to events from all applications.
//        info.packageNames = new String[]
//                {"com.example.android.myFirstApp", "com.example.android.mySecondApp"};
//
//        // Set the type of feedback your service will provide.
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
//
//        // Default services are invoked only if no package-specific ones are present
//        // for the type of AccessibilityEvent generated. This service *is*
//        // application-specific, so the flag isn't necessary. If this was a
//        // general-purpose service, it would be worth considering setting the
//        // DEFAULT flag.
//
//        // info.flags = AccessibilityServiceInfo.DEFAULT;
//
//        info.notificationTimeout = 100;
//
//        this.setServiceInfo(info);
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
            x = x/315 * maxWidth;
            y = y/315 * maxHeight;
            actionTap(x,y);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void actionTap(int x, int y){
        // accessibilityService: contains a reference to an accessibility service
        // callback: can be null if you don't care about gesture termination
        boolean result = dispatchGesture(createClick(x, y), createClickCallback(), null);
        Log.d("TAG", "Gesture dispatched? " + result);
//        Path swipePath = new Path();
//        swipePath.moveTo(x, y);
//        swipePath.lineTo(x, y);
//        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
//        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 50));
//        dispatchGesture(gestureBuilder.build(), null, null);
    }






    // (x, y) in screen coordinates
    @RequiresApi(api = Build.VERSION_CODES.N)
    private static GestureDescription createClick(float x, float y) {
        // for a single tap a duration of 1 ms is enough
        final int DURATION = 1;

        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
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
}
