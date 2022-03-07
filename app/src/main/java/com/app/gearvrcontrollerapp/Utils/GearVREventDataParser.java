package com.app.gearvrcontrollerapp.Utils;

public class GearVREventDataParser extends AppEventDataParserBase {
    private GearVREventDataParserListener mListener;
    private GearVRParsedStateObject mCurrentStateObj;

    public GearVREventDataParser(GearVREventDataParserListener mListener){
        this.mListener = mListener;
        this.mCurrentStateObj = new GearVRParsedStateObject();
    }

    /** RECEIVE EVENT DATA FOR PARSING  **/
    @Override
    public void onEventData(byte[] eventData) {
        //  PARSE RECEIVED.
        GearVRParsedStateObject mReceivedStateObj = new GearVRParsedStateObject();
        mReceivedStateObj.timestamp = System.currentTimeMillis();
        mReceivedStateObj.posXY = extractTouchpadXY(eventData);
        mReceivedStateObj.isPressingTouchpad = extractTouchpad(eventData);
        mReceivedStateObj.isPressingTrigger = extractTrigger(eventData);
        mReceivedStateObj.isPressingBack = extractBack(eventData);
        mReceivedStateObj.isPressingHome = extractHome(eventData);
        mReceivedStateObj.isPressingVolUp = extractVolUp(eventData);
        mReceivedStateObj.isPressingVolDown = extractVolDown(eventData);
        //  COMPARE CURRENT WITH RECEIVED.
        onDataParsed(mCurrentStateObj, mReceivedStateObj);
        //  STORE FOR NEXT CALL TO 'onEventData'.
        mCurrentStateObj = mReceivedStateObj;

    }
    /** NOTIFY VIA LISTENER OF ANY PARSED EVENTS   **/
    private void onDataParsed(GearVRParsedStateObject objCurrent, GearVRParsedStateObject objReceived){

    }

    /** STATE EXTRACTORS    **/
    private int[] extractTouchpadXY(byte[] eventData){
        return new int[]{ (((eventData[54] & 0xF) << 6) + ((eventData[55] & 0xFC) >> 2)) & 0x3FF, (((eventData[55] & 0x3) << 8) + ((eventData[56] & 0xFF))) & 0x3FF};
    }
    private boolean extractTouchpad(byte[] eventData){
        return (eventData[58] & (1 << 3)) != 0;
    }
    private boolean extractTrigger(byte[] eventData){
        return (eventData[58] & (1)) != 0;
    }
    private boolean extractBack(byte[] eventData){
        return (eventData[58] & (1 << 2)) != 0;
    }
    private boolean extractHome(byte[] eventData){
        return (eventData[58] & (1 << 1)) != 0;
    }
    private boolean extractVolUp(byte[] eventData){
        return (eventData[58] & (1 << 4)) != 0;
    }
    private boolean extractVolDown(byte[] eventData){
        return (eventData[58] & (1 << 5)) != 0;
    }

    /** INTERFACE   **/
    public interface GearVREventDataParserListener{
        void onTouchpadTouching(int[] posXY, boolean isInitial);
        void onTouchpadReleased();

        void onClickTouchpad();

        void onDownTrigger();
        void onUpTrigger();

        void onClickBack();
        void onLongClickBack();

        void onClickHome();
        void onLongClickHome();

        void onClickVolUp();
        void onLongClickVolUp();

        void onClickVolDown();
        void onLongClickVolDown();
    }
    /** STATE OBJECT    **/
    private static class GearVRParsedStateObject{
        public long timestamp = 0;
        public int[] posXY = new int[]{ 0, 0 };
        public boolean
                isPressingTouchpad,
                isPressingTrigger,
                isPressingBack,
                isPressingHome,
                isPressingVolUp,
                isPressingVolDown;
    }
    /** GLOBAL STATE OBJECT **/
    private class GearVRGlobalEventsObject{
        /** VARIABLES   **/
        public GearVRTouchpadProperty touchpad = new GearVRTouchpadProperty(GearVRButtonProperty.PROPERTY_ID_TOUCHPAD);
        public GearVRButtonProperty trigger = new GearVRButtonProperty(GearVRButtonProperty.PROPERTY_ID_TRIGGER);
        public GearVRButtonProperty back = new GearVRButtonProperty(GearVRButtonProperty.PROPERTY_ID_BACK);
        public GearVRButtonProperty home = new GearVRButtonProperty(GearVRButtonProperty.PROPERTY_ID_HOME);
        public GearVRButtonProperty volUp = new GearVRButtonProperty(GearVRButtonProperty.PROPERTY_ID_VOL_UP);
        public GearVRButtonProperty volDown = new GearVRButtonProperty(GearVRButtonProperty.PROPERTY_ID_VOL_DOWN);


        /** GLOBAL STATE OBJECT - PROPERTIES    **/
        private class GearVRPropertyBase{
            //  CONSTANTS
            public static final int PROPERTY_ID_UNKNOWN = -1;
            public static final int PROPERTY_ID_TOUCHPAD = 0;
            public static final int PROPERTY_ID_TRIGGER = 1;
            public static final int PROPERTY_ID_BACK = 2;
            public static final int PROPERTY_ID_HOME = 3;
            public static final int PROPERTY_ID_VOL_UP = 4;
            public static final int PROPERTY_ID_VOL_DOWN = 5;
            //  VARIABLES
            public int propertyId = PROPERTY_ID_UNKNOWN;
            public long timestampDown = 0;
            public long timestampUp = 0;
            //  CONSTRUCTOR
            public GearVRPropertyBase(int propertyId){
                this.propertyId = propertyId;
            }

        }

        private class GearVRButtonProperty extends GearVRPropertyBase{

            public GearVRButtonProperty(int propertyId){
                super(propertyId);

            }
        }
        private class GearVRTouchpadProperty extends GearVRPropertyBase{

            public GearVRTouchpadProperty(int propertyId){
                super(propertyId);

            }
        }
    }

}
