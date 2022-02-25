package com.app.gearvrcontrollerapp;

import android.content.res.TypedArray;
import android.util.Log;

import java.util.ArrayList;

public class ControllerInputParser {
    private static final double TIMESTAMP_FACTOR = 0.001; // to seconds

    public ControllerInputParser(){}

    public void onNotificationReceived(String buffer){
//        const buffer  = e.target.value.buffer;
//        const eventData = new Uint8Array(buffer);
        byte[] eventData = new byte[]{Byte.parseByte(buffer)};

        int mAxisX = parseAxisX(eventData);
        int mAxisY = parseAxisY(eventData);

        //console.log("Buffer="+JSON.stringify(buffer));

        //Int32Array
        long timestamp = 0;
        if(isGoodBufferSize(buffer)){//buffer.length >= 3
            //timestamp = ((new Uint8Array(buffer.slice(0, 3))[0]) & 0xFFFFFFFF) / 1000 * TIMESTAMP_FACTOR;
            timestamp = (long) (((new byte[]{Byte.parseByte(buffer.substring(0,3))}[0]) & 0xFFFFFFFF) / 1000 * TIMESTAMP_FACTOR);
            //console.log("IS GOOD SIZE!");
        }

        // com.samsung.android.app.vr.input.service/ui/c.class:L222
        //int temperature = eventData[57];
        boolean triggerButton    = (eventData[58] & (1 << 0)) == 0;
        boolean homeButton       = (eventData[58] & (1 << 1)) == 0;
        boolean backButton       = (eventData[58] & (1 << 2)) == 0;
        boolean touchpadButton   = (eventData[58] & (1 << 3)) == 0;
        boolean volumeUpButton   = (eventData[58] & (1 << 4)) == 0;
        boolean volumeDownButton = (eventData[58] & (1 << 5)) == 0;

        Log.v("ControllerInputParser","onNotificationReceived!!!, triggerButton="+triggerButton);
    }

    private int parseAxisX(byte[] eventData){ return (((eventData[54] & 0xF) << 6) + ((eventData[55] & 0xFC) >> 2)) & 0x3FF; }
    private int parseAxisY(byte[] eventData){ return (((eventData[55] & 0x3) << 8) + ((eventData[56] & 0xFF) >> 0)) & 0x3FF; }
    private boolean isGoodBufferSize(String b){ return b.length() >= 3; }


}
