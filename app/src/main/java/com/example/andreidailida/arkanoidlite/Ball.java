package com.example.andreidailida.arkanoidlite;

import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

import static java.lang.StrictMath.abs;

public class Ball {
    RectF rect;
    float xVelocity;
    float yVelocity;
    float ballWidth = 10;
    float ballHeight = 10;

    // Constructor
    public Ball(){
        xVelocity = 100;
        yVelocity = -400;
        rect = new RectF();

    }

    // Getter for drawing
    public RectF getRect(){
        return rect;
    }

    // Update method
    public void update(long fps){
        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top + (yVelocity / fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top - ballHeight;
    }

    // Reversing direction methods
    public void reverseYVelocity(){

        yVelocity = -yVelocity;
    }

    public void reverseXVelocity(){

        xVelocity = - xVelocity;
    }

    // After bouncing from platform
    // randomly changing direction
    public void setRandomXVelocity(){
        Random generator = new Random();
        int answer = generator.nextInt(2);

        if(answer == 0){
            reverseXVelocity();
        }
    }

    // Increasing ball speed
    public void increaseSpeed() {
        if(abs(xVelocity) < 700) {
            if (xVelocity > 0) xVelocity += 20;
            else xVelocity -= 20;
        }
        if(abs(yVelocity) < 550)
        {
            if (yVelocity > 0) yVelocity += 10;
            else yVelocity -= 10;
        }
    }

   // For moving
    public void clearObstacleY(float y){
        rect.bottom = y;
        rect.top = y - ballHeight;
    }

    public void clearObstacleX(float x){
        rect.left = x;
        rect.right = x + ballWidth;
    }

    // reset ball position
    public void reset(float x, float y){
        rect.left = x / 2;
        rect.top = y - 20;
        rect.right = x / 2 + ballWidth;
        rect.bottom = y - 20 - ballHeight;
        xVelocity = 100;
        yVelocity = -400;

    }



}
