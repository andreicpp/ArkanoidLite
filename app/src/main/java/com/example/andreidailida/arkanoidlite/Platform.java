package com.example.andreidailida.arkanoidlite;

import android.graphics.Paint;
import android.graphics.RectF;

public class Platform {

    private RectF rect;

    private float length = 130;
    private float height = 30;

    // Start points
    private float x;
    private float y;

    private float platformSpeed;

    // Available states for platform
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // Actual platform state
    private int platformMoving = STOPPED;

    // Constructor
    public Platform(int screenX, int screenY){

        x = screenX / 2 - length/2 ;
        y = screenY - height;

        // Initialize rectangle
        rect = new RectF(x, y, x + length, y + height);

        // How fast is the platform in pixels per second
        platformSpeed = 550;

    }

    // Getters for drawing
    public RectF getRect(){
        return rect;
    }

    public float getLength()
    {
        return length;
    }

    public float getX()
    {
        return x;
    }

    // Change directions of moving platform
    public void setMovementState(int state){
        platformMoving = state;
    }

    // Update for platform
    public void update(long fps){
        if(platformMoving == LEFT){
            x = x - platformSpeed / fps;
        }

        if(platformMoving == RIGHT){
            x = x + platformSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }

    // Increasing speed of platform
    public void increaseSpeed() {
        platformSpeed+=15;
    }


}

