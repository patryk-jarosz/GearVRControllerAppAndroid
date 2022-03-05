package com.app.gearvrcontrollerapp;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.VelocityTrackerCompat;

import com.google.android.material.appbar.AppBarLayout;

public class OneUIAppBarHelper {
    private Context mContext;
    private static final int FLUNG_DIRECTION_UP = 0;
    private static final int FLUNG_DIRECTION_DOWN = 1;
    private static final int FLUNG_DIRECTION_UNKNOWN = 2;

    private AppBarLayout mAppBarLayout;
    //private CoordinatorLayout mParent;
    private VelocityTracker mVelocityTracker;

    private float mLastAbsVelocityY;
    private int mLastFlungDirection = FLUNG_DIRECTION_UNKNOWN;

    private boolean didFireTouchUp = false;
    private boolean shouldExpandOnFire = false;

    private OneUIAppBarHelperListener mListener;

    private static final float FIRE_SENSITIVITY = 1f;//LOWER NUMBER, EASIER TO FIRE. //was 5f.


    public OneUIAppBarHelper(Context mContext, AppBarLayout mAppBarLayout, OneUIAppBarHelperListener mListener){
        this.mContext = mContext;
        this.mAppBarLayout = mAppBarLayout;
        this.mListener = mListener;

        setup();
    }
    private void setup(){
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float mPercentageCollapsed = (float)Math.abs(verticalOffset) / (appBarLayout.getTotalScrollRange()/2f);
                float mPercentageExpanded = 1f - mPercentageCollapsed;

                float mCalcMini = (mPercentageCollapsed - .5f) * 2;
                if(mCalcMini > 1f){
                    mCalcMini = 1f;
                }
                float mValToSet = (Math.max(0f, mCalcMini) == Math.min(mCalcMini, 1f)) ? mCalcMini : 0;
                float mCalcLarge = (mPercentageExpanded - .4f) * 2;

                mListener.onScroll(mCalcLarge,mValToSet);
//                mLinearTop.animate().alpha(mCalcLarge).setDuration(0).start();
//                mTxtToolbar.animate().alpha(mValToSet).setDuration(0).start();

                if(mLastAbsVelocityY >= FIRE_SENSITIVITY){
                    //  WAS FLUNG
                    shouldExpandOnFire = mLastAbsVelocityY >= FIRE_SENSITIVITY;
                }else{
                    //  SNAP BY CURRENT OFFSET
                    shouldExpandOnFire = mPercentageCollapsed <= .6f;
                }


                if(Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
                    //  Collapsed
                }else{
                    //Expanded
                }
                didFireTouchUp = false;
            }
        });
    }

    /** onDispatchTouchEvent - RETURNS DID CONSUME    **/
    public boolean onDispatchTouchEvent(MotionEvent ev){
        int index = ev.getActionIndex();
        int action = ev.getActionMasked();
        int pointerId = ev.getPointerId(index);

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(ev);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                mVelocityTracker.computeCurrentVelocity(1,100f);//100f//50f
                mLastAbsVelocityY = Math.abs(VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));
                mLastFlungDirection = (VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId) < 0) ? FLUNG_DIRECTION_DOWN : FLUNG_DIRECTION_UP;
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.

                //Log.v("TAG", "X velocity: " + VelocityTrackerCompat.getXVelocity(mVelocityTracker,pointerId));
                //Log.v("TAG", "Y velocity: " + VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
        }

        if(ev.getAction() == MotionEvent.ACTION_UP){
            if(!didFireTouchUp){
                fireTouchUp();
                return true;
            }
            //return false;
        }
        return false;
    }



    private void fireTouchUp(){
        didFireTouchUp = true;

        if(mLastAbsVelocityY <= FIRE_SENSITIVITY){
            mLastFlungDirection = FLUNG_DIRECTION_UNKNOWN;
        }

        if(mLastFlungDirection == FLUNG_DIRECTION_UNKNOWN){
            mAppBarLayout.setExpanded(shouldExpandOnFire,true);
        }else{
            shouldExpandOnFire = mLastFlungDirection == FLUNG_DIRECTION_UP;
            mAppBarLayout.setExpanded(shouldExpandOnFire,true);
        }
    }

    interface OneUIAppBarHelperListener{
        void onScroll(float alphaToolBar,float alphaContentBar);
    }
}
