package com.app.gearvrcontrollerapp.New;

import android.content.Context;
import android.util.Log;

import static android.view.ViewConfiguration.getLongPressTimeout;

public class ControllerInputManager {
    private Context mContext;
    private ControllerInputManagerListener mListener;

    private byte[] lastEventData = new byte[]{0};
    private long lastEventTimestamp = 0;
    private ControllerInputStateObject currentStateObj;

    private int DURATION_LONG_PRESS = 500;
    private int DURATION_LONG_PRESS_INTERVAL = 500;

    private long startTimePressingHome = 0;
    private long startTimePressingBack = 0;


    public ControllerInputManager(Context mContext, ControllerInputManagerListener mListener){
        this.mContext = mContext;
        this.mListener = mListener;
        this.currentStateObj = new ControllerInputStateObject();
        DURATION_LONG_PRESS = getLongPressTimeout();
    }

    /** ON EVENT DATA RECEIVED  **/
    public void onEventData(byte[] eventData){
        //  TODO: parse and notify activity.
        lastEventData = eventData;
        lastEventTimestamp = System.currentTimeMillis();
        //  NEW STATE JUST RECEIVED
        ControllerInputStateObject mReceivedStateObj = new ControllerInputStateObject(eventData);
        /** CHECKING LONG PRESS **/
        if(!currentStateObj.isPressingHome && mReceivedStateObj.isPressingHome){
            //  NOW NEED TO STORE TIMESTAMP
            startTimePressingHome = System.currentTimeMillis();
        }
        if(!currentStateObj.isPressingBack && mReceivedStateObj.isPressingBack){
            //  NOW NEED TO STORE TIMESTAMP
            startTimePressingBack = System.currentTimeMillis();
        }


        /** COMPARE AND HANDLE EVENTS - START  **/
        //  TRIGGER
        boolean mTouchpadShouldClick = shouldOnClick(currentStateObj.isPressingTouchpad,mReceivedStateObj.isPressingTouchpad);
        //  TOUCHPAD
        if(mReceivedStateObj.touchpadPosX == 0  && mReceivedStateObj.touchpadPosY == 0){
            //NOT TOUCHING
        }else{
            mListener.onTouchpad(mTouchpadShouldClick,mReceivedStateObj.touchpadPosX,mReceivedStateObj.touchpadPosY);
        }
        //  BACK
        if(shouldOnClick(currentStateObj.isPressingBack,mReceivedStateObj.isPressingBack)){
            //  CHECK IF WAS LONG CLICKED
            if(shouldOnLongClick(startTimePressingBack,mReceivedStateObj.timestampState)){
                mListener.onLongClickBack();
            }else{
                mListener.onClickBack();
            }
        }
        //  HOME
        if(shouldOnClick(currentStateObj.isPressingHome,mReceivedStateObj.isPressingHome)){
            //  CHECK IF WAS LONG CLICKED
            if(shouldOnLongClick(startTimePressingHome,mReceivedStateObj.timestampState)){
                mListener.onLongClickHome();
                Log.v("TAG","mListener.onLongClickHome()");
            }else{
                mListener.onClickHome();
            }
        }
        //  VOL UP
        if(shouldOnClick(currentStateObj.isPressingVolUp,mReceivedStateObj.isPressingVolUp)){ mListener.onClickVolUp(); }
        //  VOL DOWN
        if(shouldOnClick(currentStateObj.isPressingVolDown,mReceivedStateObj.isPressingVolDown)){ mListener.onClickVolDown(); }
        /** COMPARE AND HANDLE EVENTS - END  **/
        //  UPDATE STORED OBJ
        currentStateObj = mReceivedStateObj;
    }

    /** DETERMINE IF SHOULD PERFORM CLICK   **/
    public interface ControllerInputManagerListener{
        void onTouchpad(boolean shouldClick, int posX, int posY);//todo: change to separate isPressing -> onClickTouchpad.
        void onClickTrigger();
        void onClickBack();
        void onClickHome();
        void onClickVolUp();
        void onClickVolDown();

        void onLongClickHome();
        void onLongClickBack();
    }

    /** TRACK ALREADY PRESSING  **/
    private class ControllerInputStateObject{
        public long timestampState = 0;
        public int
                touchpadPosX = 0,
                touchpadPosY = 0;
        public boolean isPressingTouchpad = false;
        public boolean isPressingTrigger = false;
        public boolean isPressingBack = false;
        public boolean isPressingHome = false;
        public boolean isPressingVolUp = false;
        public boolean isPressingVolDown = false;

        //public long startTimePressingHome = 0;

        /** CONSTRUCTORS    **/
        public ControllerInputStateObject(){
            //  DEFAULT
        }
        public ControllerInputStateObject(byte[] eventData){
            //  WITH EVENT DATA
            updateEventData(eventData);
        }

        /** UPDATE EVENT DATA   **/
        public void updateEventData(byte[] eventData){

            boolean mWasPressingHome = isPressingHome;

            //const axisX = (((eventData[54] & 0xF) << 6) + ((eventData[55] & 0xFC) >> 2)) & 0x3FF;
            //const axisX = (((eventData[54] & 0xF) << 6) + ((eventData[55] & 0xFC) >> 2)) & 0x3FF;
            //const axisY = (((eventData[55] & 0x3) << 8) + ((eventData[56] & 0xFF) >> 0)) & 0x3FF;
            //const axisY = (((eventData[55] & 0x3) << 8) + ((eventData[56] & 0xFF) >> 0)) & 0x3FF;
            /*  UPDATE VALUES   */
            //  TOUCHPAD X
            touchpadPosX = (((eventData[54] & 0xF) << 6) + ((eventData[55] & 0xFC) >> 2)) & 0x3FF;
            //  TOUCHPAD Y
            touchpadPosY = (((eventData[55] & 0x3) << 8) + ((eventData[56] & 0xFF))) & 0x3FF;
            //  TRIGGER - PRESSED
            isPressingTrigger = (eventData[58] & (1)) != 0;
            //  HOME - PRESSED
            isPressingHome = (eventData[58] & (1 << 1)) != 0;
            //  BACK - PRESSED
            isPressingBack = (eventData[58] & (1 << 2)) != 0;
            //  TOUCHPAD - PRESSED
            isPressingTouchpad = (eventData[58] & (1 << 3)) != 0;
            //  VOL_UP - PRESSED
            isPressingVolUp = (eventData[58] & (1 << 4)) != 0;
            //  VOL_DOWN - PRESSED
            isPressingVolDown = (eventData[58] & (1 << 5)) != 0;
            /*  UPDATE TIMESTAMP   */
            timestampState = System.currentTimeMillis();

            /*  UPDATE TIMESTAMPS FOR PRESSING BEGAN (if applicable to this event)    */
            //if(!mWasPressingHome && isPressingHome){ startTimePressingHome = timestampState; }
        }
    }

    /** SHOULD ON_CLICK **/
    private boolean shouldOnClick(boolean wasPressing, boolean stillPressing){ return wasPressing && !stillPressing; }


    /** SHOULD ON_LONG_CLICK    **/
    private boolean shouldOnLongClick(long startTime, long endTime){
        Log.v("TAG","shouldOnLongClick="+(endTime - startTime >= DURATION_LONG_PRESS));
        Log.v("TAG","shouldOnLongClick,endTime="+endTime);
        Log.v("TAG","shouldOnLongClick,startTime="+startTime);
        return (endTime - startTime >= DURATION_LONG_PRESS);
    }
}