package com.app.gearvrcontrollerapp.Utils;

abstract class AppEventDataParserBase implements AppEventDataParserBaseInterface {

}

interface AppEventDataParserBaseInterface{
    void onEventData(byte[] eventData);
}
