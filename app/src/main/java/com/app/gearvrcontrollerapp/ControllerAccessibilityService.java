package com.app.gearvrcontrollerapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class ControllerAccessibilityService extends AccessibilityService {

    boolean mIsInitialEvent = true;

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
}
