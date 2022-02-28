package com.app.gearvrcontrollerapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ControllerInputOverlayService extends Service {
    View view;
    private WindowManager windowManager;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams overlayparams;
    int flagType;
    ImageView mCursor;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();

        //check api type
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            flagType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;//TYPE_ACCESSIBILITY_OVERLAY;
        }else{
            flagType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        maxWidth = display.getWidth();
        maxHeight = display.getHeight();


        overlayparams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                flagType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        //overlayparams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        overlayparams.x = 0;
        overlayparams.y = 0;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                flagType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        //params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.system_overlay,null);
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                //Toast.makeText(ControllerInputOverlayService.this, "t", Toast.LENGTH_SHORT).show();
//                Log.v("TAG","TOUCHING!");
//                return false;
//            }
//        });
        mCursor = view.findViewById(R.id.imgview_cursor);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        windowManager.addView(view, overlayparams);

        listenForMouseCursorPos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (view != null) windowManager.removeView(view);
    }

    private void listenForMouseCursorPos(){
        BroadcastReceiver mysms=new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                boolean mIsPressing = arg1.getExtras().getBoolean("isPressing");
                int mPosX = arg1.getExtras().getInt("posX");
                int mPosY = arg1.getExtras().getInt("posY");

                setCursorPos(mPosX,mPosY);

                Log.v("TAG","onReceive");

            }
        };
        registerReceiver(mysms, new IntentFilter("your_action_name"));
    }

    int mPrevPosX = 0, mPrevPosY = 0;
    int mCurrentPosX = 10;
    int mCurrentPosY = 10;
    int factor = 100;
    int maxWidth = 0;
    int maxHeight = 0;
    public void setCursorPos(int x, int y){

        Log.v("setCursorPos","x,y="+x+","+y);

        float percentageX = x / 315f;
        float percentageY = y / 315f;

        mCurrentPosX = (int) (maxWidth * percentageX);
        mCurrentPosY = (int) (maxHeight * percentageY);

        mCursor.animate().x(mCurrentPosX).y(mCurrentPosY).setDuration(0).start();
    }
}
