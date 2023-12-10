package edu.northeastern.stage.model;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class Circle {

    private float x;
    private float y;
    private float radius;
    public int speed = 1;
    public int[] direction = new int[]{1,1}; //direction modifier (-1,1)
    public RectF oval;
    public Paint paint;

    // Constructor
    @SuppressLint("ResourceAsColor")
    public Circle(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.paint = new Paint();
        this.paint.setColor(android.R.color.white);
    }

    public void move(Canvas canvas) {
        this.x += speed*direction[0];
        this.y += speed*direction[1];
        this.oval = new RectF(x-radius/2,y-radius/2,x+radius/2,y+radius/2);

        //Do we need to bounce next time?
        Rect bounds = new Rect();
        this.oval.roundOut(bounds); ///store our int bounds

        if(!canvas.getClipBounds().contains(bounds)){
            if(this.x-radius<0 || this.x+radius > canvas.getWidth()){
                direction[0] = direction[0]*-1;
            }
            if(this.y-radius<0 || this.y+radius > canvas.getHeight()){
                direction[1] = direction[1]*-1;
            }
        }
    }

    // Getters

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }

    // Setters

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

}



