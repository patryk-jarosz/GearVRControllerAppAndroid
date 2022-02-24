package com.app.gearvrcontrollerapp;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ControllerInputDisplay {
    private Button mBtnConnect, mBtnDisconnect;
    private TextView mMacAddress,mConnectionStatus,mTouchpadXY,mTrigger,mBack,mHome,mVolUp,mVolDown;
    private Context mContext;

    private ControllerInputDisplayListener mListener;

    public ControllerInputDisplay(Context mContext, LinearLayout mLinearParent){
        this.mContext = mContext;

        mTouchpadXY = mLinearParent.findViewById(R.id.include_controller_values_touchpad);
        mTrigger = mLinearParent.findViewById(R.id.include_controller_values_trigger);
        mBack = mLinearParent.findViewById(R.id.include_controller_values_back);
        mHome = mLinearParent.findViewById(R.id.include_controller_values_home);
        mVolUp = mLinearParent.findViewById(R.id.include_controller_values_volup);
        mVolDown = mLinearParent.findViewById(R.id.include_controller_values_voldown);

        mMacAddress = mLinearParent.findViewById(R.id.include_controller_values_macaddress);
        mConnectionStatus = mLinearParent.findViewById(R.id.include_controller_values_connectionstatus);
        mBtnConnect = mLinearParent.findViewById(R.id.include_controller_values_btn_connect);
        mBtnDisconnect = mLinearParent.findViewById(R.id.include_controller_values_btn_disconnect);

        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnConnect.setEnabled(false);
                mBtnDisconnect.setEnabled(true);
                mListener.onClickConnect();
            }
        });
        mBtnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnConnect.setEnabled(true);
                mBtnDisconnect.setEnabled(false);
                mListener.onClickDisconnect();
            }
        });

    }

    public void onMacAddress(String macAddress){ setText("mac address: "+macAddress,mMacAddress); }
    public void onConnectionStatus(boolean connected){ setText("connnected: "+connected,mConnectionStatus); }

    public void onTouchpadXY(int x, int y){ setText("touchpad: x="+x+",y="+y,mTouchpadXY); }
    public void onTrigger(boolean pressed){ setText("trigger:"+pressed,mTrigger); }
    public void onBack(boolean pressed){ setText("back:"+pressed,mBack); }
    public void onHome(boolean pressed){ setText("home:"+pressed,mHome); }
    public void onVolUp(boolean pressed){ setText("vol up:"+pressed,mVolUp); }
    public void onVolDown(boolean pressed){ setText("vol down:"+pressed,mVolDown); }

    //  internal
    private void setText(String text, TextView textView){ textView.setText(text); }

    //  INTERFACE
    public interface ControllerInputDisplayListener{
        void onClickConnect();
        void onClickDisconnect();
    }

    public void setListener(ControllerInputDisplayListener mListener){
        this.mListener = mListener;
    }
}
