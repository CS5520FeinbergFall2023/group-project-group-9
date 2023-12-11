package edu.northeastern.stage.ui.explore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.widget.Toast;
import edu.northeastern.stage.MainActivity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.stage.MainActivity;
import edu.northeastern.stage.R;
import edu.northeastern.stage.model.Circle;
import edu.northeastern.stage.model.music.Track;

public class CircleView extends View {

    // Constructor and circle list initialization
    Circle[] circles;
    private Matrix matrix;
    private Paint paint;
    private Paint textPaint;
    private float scaleFactor = 1.05f;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging = false;
    Integer countDraw = 0;
    private float[] velocities;
    Boolean textPresent = false;

    private CircleClickListener listener;

    public void setCircleClickListener(CircleClickListener listener) {
        this.listener = listener;
    }

    Map<Circle, String> circleTextMap = new HashMap<>();

    ScaleGestureDetector objScaleGestureDetector;

    // Constructors for XML inflation
    public CircleView(Context context) {
        super(context);
        Log.d("CIRCLEVIEW", "circleview context");
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("CIRCLEVIEW", "circleview context + attrs");
        init();
    }

    public CircleView(Context context, AttributeSet attrs, List<Circle> circles, int defStyle) {
        super(context, attrs, defStyle);
        this.circles = circles.toArray(new Circle[0]);
        Log.d("CIRCLEVIEW", "circleview context + attrs + defstyle");
        init();
    }

    public CircleView(Context context, List<Circle> circles) {
        super(context);
        this.circles = circles.toArray(new Circle[0]);
        Log.d("CIRCLEVIEW", "circleview context + list of circles");
        init();
    }

    private void init() {
        Log.d("CIRCLEVIEW", "init");

        objScaleGestureDetector = new ScaleGestureDetector(this.getContext(), new PinchZoomListener());

        matrix = new Matrix();
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        int greenColor = ContextCompat.getColor(getContext(), R.color.green);
        textPaint.setColor(greenColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawColor(Color.rgb(245, 245, 220));

        countDraw++;
//        canvas.clipRect(0, 0, getWidth(), getHeight());

        super.onDraw(canvas);
        Log.d("CIRCLEVIEW", "on draw count " + countDraw);

        canvas.save();
        canvas.concat(matrix);

        if (circles != null) {
            // Draw each circle on the canvas
            for (Circle c : circles) {
                int greenColor = ContextCompat.getColor(getContext(), R.color.medium_green_50);
                paint.setColor(greenColor);
                paint.setStyle(Paint.Style.FILL);

                if(textPresent){
                    // Set random text size based on circle radius
                    float textSize = c.getRadius() / 3;
                    paint.setTextSize(textSize);
                    String fullText  = circleTextMap.get(c);
                    String[] lines = fullText.split("//");
                    float textWidth = maxTextWidth(lines);
                    // adjust the width to fit inside the circle
                    while (textWidth > c.getRadius() * 2 && textSize > 0) {
                        textSize--;
                        paint.setTextSize(textSize);
                        textWidth = maxTextWidth(lines);
                    }
                    if (textSize > 2) textSize -= 3;
                    Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                    float lineHeight = fontMetrics.descent - fontMetrics.ascent;
                    float textHeight = lineHeight * lines.length;

                    float textY = c.getY() - (textHeight / 2); // Starting Y position to vertically center the text
                    for (String line : lines) {
                        float textWidthCurr = paint.measureText(line);
                        float textX = c.getX() - (textWidthCurr / 2); // Center the text horizontally
                        textY += -fontMetrics.ascent; // Move text down by the ascent to position it correctly
                        canvas.drawText(line, textX, textY, textPaint);
                        textY += fontMetrics.descent; // Move down to the next line
                    }
                }

                canvas.save();
                // Draw circle with black border
                canvas.drawCircle(c.getX(), c.getY(), c.getRadius(), paint);
            }
        }

        velocities = new float[circles.length * 2]; // x and y velocities for each circle
        // Update circle positions based on velocities
        updateCirclePositions(canvas);

        canvas.save();
        canvas.concat(matrix);
        canvas.restore();
//        drawZoomControls(canvas);

        if(countDraw < 8) {
            for (Integer i = 0; i < 3; i++) {
                //best scale factor so far for current configs (phone size, maxAttempts, number of circles)
                scaleFactor /= 1.05f;
            }
            matrix.reset();
            //best division factors for width and height so far for current configs (phone size, maxAttempts, number of circles)
            matrix.postScale(scaleFactor, scaleFactor, getWidth() / 1.25f, getHeight() / 1.25f);
            invalidate();
        }
//        postInvalidate();
    }

    private float maxTextWidth(String[] lines) {
        float maxWidth = 0;
        for (String line: lines) {
            float textWidth = paint.measureText(line);
            maxWidth = Math.max(maxWidth, textWidth);
        }
        return maxWidth;
    }

    private float[] transformPoint(float x, float y) {
        float[] point = new float[]{x, y};
        matrix.mapPoints(point);
        return point;
    }

    private void enforceBoundary(Circle circle, int index) {
        float[] transformed = transformPoint(circle.getX(), circle.getY());
        float radius = circle.getRadius() * scaleFactor;

        // Check left and right boundaries
        if (transformed[0] - radius < 0) {
            circle.setX(circle.getX() + (0 - (transformed[0] - radius)));
            velocities[index * 2] *= -1; // Reverse x velocity
        } else if (transformed[0] + radius > getWidth()) {
            circle.setX(circle.getX() - ((transformed[0] + radius) - getWidth()));
            velocities[index * 2] *= -1; // Reverse x velocity
        }

        // Check top and bottom boundaries
        if (transformed[1] - radius < 0) {
            circle.setY(circle.getY() + (0 - (transformed[1] - radius)));
            velocities[index * 2 + 1] *= -1; // Reverse y velocity
        } else if (transformed[1] + radius > getHeight()) {
            circle.setY(circle.getY() - ((transformed[1] + radius) - getHeight()));
            velocities[index * 2 + 1] *= -1; // Reverse y velocity
        }
    }

    private void updateCirclePositions(Canvas canvas) {
        for (int i = 0; i < circles.length; i++) {
            Circle c1 = circles[i];
            c1.move(canvas);

            // Update positions
            c1.setX(c1.getX() + velocities[i * 2]);
            c1.setY(c1.getY() + velocities[i * 2 + 1]);

            // Enforce boundary constraints and reverse direction if needed
            enforceBoundary(c1, i);

            // Apply friction to slow down gradually
//            velocities[i * 2] *= FRICTION;
//            velocities[i * 2 + 1] *= FRICTION;

            // Check for collisions with other circles
            for (int j = i + 1; j < circles.length; j++) {
                Circle c2 = circles[j];
                handleCollision(c1, c2, i, j);
            }
        }
        invalidate();
    }

    private void handleCollision(Circle c1, Circle c2, int index1, int index2) {
        float[] pos1 = transformPoint(c1.getX(), c1.getY());
        float[] pos2 = transformPoint(c2.getX(), c2.getY());

        float radius1 = c1.getRadius() * scaleFactor;
        float radius2 = c2.getRadius() * scaleFactor;

        float dx = pos2[0] - pos1[0];
        float dy = pos2[1] - pos1[1];
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float overlap = (radius1 + radius2 - distance) + 3;

        if (distance < radius1 + radius2) {
            // Exchange velocities
            float tempVelX = velocities[index1 * 2];
            float tempVelY = velocities[index1 * 2 + 1];

            velocities[index1 * 2] = velocities[index2 * 2];
            velocities[index1 * 2 + 1] = velocities[index2 * 2 + 1];

            velocities[index2 * 2] = tempVelX;
            velocities[index2 * 2 + 1] = tempVelY;

            // Move circles slightly away from each other to avoid sticking together
            dx /= distance;
            dy /= distance;

            c1.setX(c1.getX() - overlap * dx);
            c1.setY(c1.getY() - overlap * dy);

            c2.setX(c2.getX() + overlap * dx);
            c2.setY(c2.getY() + overlap * dy);
        }

        float newDx = c2.getX() - c1.getX();
        float newDy = c2.getY() - c1.getY();
        float newDistance = (float) Math.sqrt(newDx * newDx + newDy * newDy);

        if (newDistance < radius1 + radius2) {
            float tempVelX = velocities[index1 * 2];
            float tempVelY = velocities[index1 * 2 + 1];

            velocities[index1 * 2] = velocities[index2 * 2];
            velocities[index1 * 2 + 1] = velocities[index2 * 2 + 1];

            velocities[index2 * 2] = tempVelX;
            velocities[index2 * 2 + 1] = tempVelY;

            // Move circles slightly away from each other to avoid sticking together
            newDx /= distance;
            newDy /= distance;

            c1.setX(c1.getX() - overlap * newDx);
            c1.setY(c1.getY() - overlap * newDy);

            c2.setX(c2.getX() + overlap * newDx);
            c2.setY(c2.getY() + overlap * newDy);
        }
    }

//    private void updateCirclePositions(Canvas canvas) {
//
//        //moving and also bouncing off of each other
//        for (int i = 0; i < circles.length; i++) {
//            Circle c1 = circles[i];
//            //Move first
//            c1.move(canvas);
//            //Draw them
////            canvas.drawCircle(c1.getX(), c1.getY(), c1.getRadius(), c1.paint);
//
////            Update circle position based on velocity
//            c1.setX(c1.getX() + velocities[i * 2]);
//            c1.setY(c1.getY() + velocities[i * 2 + 1]);
//
//            // Boundary check
//            if(c1.getX() - c1.getRadius() < 0 ||
//                    c1.getX() + c1.getRadius() > getWidth()) {
//                // Flip x velocity
//                velocities[i*2] *= -1;
//            }
//
//            if(c1.getY() - c1.getRadius() < 0 ||
//                    c1.getY() + c1.getRadius() > getHeight()) {
//                // Flip y velocity
//                velocities[i*2 + 1] *= -1;
//            }
//
//            // Check for collisions with other circles
//            for (int j = i + 1; j < circles.length; j++) {
//                Circle c2 = circles[j];
//                handleCollision(c1, c2, i, j, canvas);
//            }
//        }
//        invalidate();
//    }
//
//    private void handleCollision(Circle c1, Circle c2, int index1, int index2, Canvas canvas) {
//        float dx = c2.getX() - c1.getX();
//        float dy = c2.getY() - c1.getY();
//        float distance = (float) Math.sqrt(dx * dx + dy * dy);
//
//        if (distance < c1.getRadius() + c2.getRadius()) {
//            // Circles are colliding, adjust their velocities for bouncing effect
//            float angle = (float) Math.atan2(dy, dx);
//            float cosAngle = (float) Math.cos(angle);
//            float sinAngle = (float) Math.sin(angle);
//
//            // Calculate new velocities for both circles
//            float v1n = velocities[index1 * 2] * cosAngle + velocities[index1 * 2 + 1] * sinAngle;
//            float v1t = -velocities[index1 * 2] * sinAngle + velocities[index1 * 2 + 1] * cosAngle;
//
//            float v2n = velocities[index2 * 2] * cosAngle + velocities[index2 * 2 + 1] * sinAngle;
//            float v2t = -velocities[index2 * 2] * sinAngle + velocities[index2 * 2 + 1] * cosAngle;
//
//            // Swap normal velocities (bounce off each other)
//            velocities[index1 * 2] = v2n * cosAngle - v1t * sinAngle;
//            velocities[index1 * 2 + 1] = v2n * sinAngle + v1t * cosAngle;
//
//            velocities[index2 * 2] = v1n * cosAngle - v2t * sinAngle;
//            velocities[index2 * 2 + 1] = v1n * sinAngle + v2t * cosAngle;
//
//            // Move circles slightly away to avoid continuous collisions
//            float overlap = (c1.getRadius() + c2.getRadius() - distance) / 2;
//            c1.setX(c1.getX() - overlap * cosAngle);
//            c1.setY(c1.getY() - overlap * sinAngle);
//
//            c2.setX(c2.getX() + overlap * cosAngle);
//            c2.setY(c2.getY() + overlap * sinAngle);
//        }
//    }

    public void setCircles(List<Circle> circles, HashMap<Circle, String> circleTextMap) {
        Log.d("CIRCLEVIEW", "set circles TEXT PRESENT");

        this.circles = circles.toArray(new Circle[0]);
        this.circleTextMap = circleTextMap;
        this.textPresent = true;

        invalidate(); // Request a redraw
    }

    public void setCircles(List<Circle> circles) {
        Log.d("CIRCLEVIEW", "set circles NO TEXT");

        this.circles = circles.toArray(new Circle[0]);
        this.circleTextMap = circleTextMap;
        this.textPresent = false;

        invalidate(); // Request a redraw
    }

    private void drawZoomControls(Canvas canvas) {
        Paint controlsPaint = new Paint();
        controlsPaint.setColor(Color.BLUE);
        controlsPaint.setTextSize(50);

        canvas.drawText("+", getWidth() - 80, getHeight() - 80, controlsPaint);
        canvas.drawText("-", getWidth() - 70, getHeight() - 20, controlsPaint);
    }


    //include the + & - back again after fixing the canvas touch that makes the circles disappear (rectangle border remains)
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        float touchX = event.getX();
//        float touchY = event.getY();
//
//        Log.d("CIRCLEVIEW", "Touch event: " + event.getAction());
////        toastmsg("In onTouchEvent ->" + Thread.activeCount());
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                Log.d("CIRCLEVIEW", "ACTION_DOWN");
//                lastTouchX = touchX;
//                lastTouchY = touchY;
//                isDragging = true;
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//
//                if (isDragging) {
//                    Log.d("CIRCLEVIEW", "isDragging  in ACTION_MOVE");
//
//                    float dx = touchX - lastTouchX;
//                    float dy = touchY - lastTouchY;
//                    matrix.postTranslate(dx, dy);
//                    invalidate();
//                    lastTouchX = touchX;
//                    lastTouchY = touchY;
//                }
//                break;
//
//            case MotionEvent.ACTION_UP:
//                Log.d("CIRCLEVIEW", "ACTION_UP");
//                checkCircleClick(touchX, touchY);
//                break;
//
//            case MotionEvent.ACTION_CANCEL:
//                Log.d("CIRCLEVIEW", "ACTION_CANCEL");
//                isDragging = false;
//                break;
//        }
//
//        objScaleGestureDetector.onTouchEvent(event);
//
//        return true;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = touchX;
                lastTouchY = touchY;
                isDragging = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float dx = touchX - lastTouchX;
                    float dy = touchY - lastTouchY;
                    matrix.postTranslate(dx, dy);
                    invalidate();
                    lastTouchX = touchX;
                    lastTouchY = touchY;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }

        objScaleGestureDetector.onTouchEvent(event);
        return true;
    }



    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private boolean isZoomInButtonTapped(float x, float y) {
        return x > getWidth() - 100 && y > getHeight() - 100 && x < getWidth() && y < getHeight();
    }

    private boolean isZoomOutButtonTapped(float x, float y) {
        return x > getWidth() - 100 && y > getHeight() - 30 && x < getWidth() && y < getHeight() - 10;
    }


    //check if we still want to leave the pinch in and out seeing that texts don't get zoomed in
    public class PinchZoomListener extends SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)); // Optional: Limit the scale factor

            matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }
    }


    private void toastmsg(String msg){
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void checkCircleClick(float touchX, float touchY) {
        if (circles != null) {
            for (Circle c : circles) {
                // Check if the touch point is within the bounds of the circle
                if (isPointInsideCircle(touchX, touchY, c)) {
                    // Handle the circle click, for example, display a message or perform an action
                    toastmsg("" + c + " text value is " + circleTextMap.get(c));
                    Log.d("CircleView", "Current selected track :  -> " + c.getTrackObject());
                    Log.d("CircleView", "Listener   -> " + listener);

//                    ((MainActivity)requireActivity()).navigateToFragment("MUSIC_REVIEW_FRAGMENT", true, null);

                    if(listener != null) {
                        listener.onCircleClicked(c.getTrackObject());
                    }
                    break; // Exit the loop once a circle is clicked
                }
            }
        }
    }

    private boolean isPointInsideCircle(float x, float y, Circle circle) {
        float[] point = {x, y};
        Matrix invertedMatrix = new Matrix();
        matrix.invert(invertedMatrix);
        invertedMatrix.mapPoints(point);

        float distance = (float) Math.sqrt(Math.pow(point[0] - circle.getX(), 2) + Math.pow(point[1] - circle.getY(), 2));
        return distance <= circle.getRadius();
    }

    public interface CircleClickListener {
        void onCircleClicked(JsonObject clickedTrack);
    }



}
