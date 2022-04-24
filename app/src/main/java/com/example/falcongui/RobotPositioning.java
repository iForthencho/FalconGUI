package com.example.falcongui;

import android.graphics.RectF;

public class RobotPositioning {
    private RectF mRect;
    private float mBallWidth;
    private float mBallHeight;

    public RobotPositioning(int screenX, int screenY){

        // Make the mBall size relative to the screen resolution
        mBallWidth = screenX / 100;
        mBallHeight = mBallWidth;

        // Initialize the Rect that represents the mBall
        mRect = new RectF();

    }

    // Give access to the Rect
    public RectF getRect(){
        return mRect;
    }

    // Change the position each frame
    public void update(long fps){
        mRect.left = mRect.left + 1;
        mRect.top = mRect.top + 1;
        mRect.right = mRect.left + mBallWidth;
        mRect.bottom = mRect.top - mBallHeight;
    }
}
