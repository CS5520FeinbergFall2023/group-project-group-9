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

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.stage.model.Circle;

public class CircleView extends View {

    // Constructor and circle list initialization
    Circle[] circles;
    private Matrix matrix;
    private Paint paint;
    private float scaleFactor = 1.05f;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging = false;
    Integer countDraw = 0;
    private float[] velocities;

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

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        countDraw ++;
//        canvas.clipRect(0, 0, getWidth(), getHeight());


        super.onDraw(canvas);
        Log.d("CIRCLEVIEW", "on draw count " + countDraw);

        canvas.save();
        canvas.concat(matrix);

        if (circles != null) {
            // Draw each circle on the canvas
            for (Circle c : circles) {

                // Set random text size based on circle radius
                float textSize = c.getRadius() / 3;
                paint.setTextSize(textSize);

                // Generate random text
                String randomText = circleTextMap.get(c);
                // Calculate text width and height
                float textWidth = paint.measureText(randomText);
                Paint.FontMetrics metrics = paint.getFontMetrics();
                float textHeight = metrics.descent - metrics.ascent;
                // Calculate centered coordinates for the text
                float textX = c.getX() - (textWidth / 2);
                float textY = c.getY() - (textHeight / 2);

                canvas.save();
                // Draw circle with black border
                canvas.drawCircle(c.getX(), c.getY(), c.getRadius(), paint);
                // Draw text inside the circle
                canvas.drawText(randomText, textX, textY, paint);
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

    private float[] transformPoint(float x, float y) {
        float[] point = new float[]{x, y};
        matrix.mapPoints(point);
        return point;
    }

    private void enforceBoundary(Circle circle) {
        float[] transformed = transformPoint(circle.getX(), circle.getY());
        float radius = circle.getRadius() * scaleFactor;

        // Check boundaries and adjust position if needed
        if (transformed[0] - radius < 0) {
            circle.setX(circle.getX() + (radius - transformed[0]));
        } else if (transformed[0] + radius > getWidth()) {
            circle.setX(circle.getX() - (transformed[0] + radius - getWidth()));
        }

        if (transformed[1] - radius < 0) {
            circle.setY(circle.getY() + (radius - transformed[1]));
        } else if (transformed[1] + radius > getHeight()) {
            circle.setY(circle.getY() - (transformed[1] + radius - getHeight()));
        }
    }

    private void updateCirclePositions(Canvas canvas) {
        for (int i = 0; i < circles.length; i++) {
            Circle c1 = circles[i];
            c1.move(canvas);

            // Transform coordinates to consider current zoom level and pan
            float[] transformed = transformPoint(c1.getX(), c1.getY());

            // Boundary check with transformed coordinates
            if (transformed[0] - c1.getRadius() < 0 ||
                    transformed[0] + c1.getRadius() > getWidth() * scaleFactor ||
                    transformed[1] - c1.getRadius() < 0 ||
                    transformed[1] + c1.getRadius() > getHeight() * scaleFactor) {
                velocities[i * 2] *= -1; // Flip x velocity
                velocities[i * 2 + 1] *= -1; // Flip y velocity
            }

            // Enforce boundary constraints
            enforceBoundary(c1);

            // Update positions and check for collisions
            c1.setX(c1.getX() + velocities[i * 2]);
            c1.setY(c1.getY() + velocities[i * 2 + 1]);
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

        if (distance < radius1 + radius2) {
            // Exchange velocities
            float tempVelX = velocities[index1 * 2];
            float tempVelY = velocities[index1 * 2 + 1];

            velocities[index1 * 2] = velocities[index2 * 2];
            velocities[index1 * 2 + 1] = velocities[index2 * 2 + 1];

            velocities[index2 * 2] = tempVelX;
            velocities[index2 * 2 + 1] = tempVelY;

            // Move circles slightly away from each other to avoid sticking together
            float overlap = 0.5f * ((radius1 + radius2) - distance);
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
            float overlap = 0.5f * ((radius1 + radius2) - distance);
            newDx /= distance;
            newDy /= distance;

            c1.setX(c1.getX() - overlap * newDx);
            c1.setY(c1.getY() - overlap * newDy);

            c2.setX(c2.getX() + overlap * newDx);
            c2.setY(c2.getY() + overlap * newDy);
        }
    }

    public void setCircles(List<Circle> circles, HashMap<Circle, String> circleTextMap) {
        Log.d("CIRCLEVIEW", "set circles");

        this.circles = circles.toArray(new Circle[0]);
        this.circleTextMap = circleTextMap;

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
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        Log.d("CIRCLEVIEW", "Touch event: " + event.getAction());
//        toastmsg("In onTouchEvent ->" + Thread.activeCount());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("CIRCLEVIEW", "ACTION_DOWN");
                lastTouchX = touchX;
                lastTouchY = touchY;
                isDragging = true;
                break;

            case MotionEvent.ACTION_MOVE:

                if (isDragging) {
                    Log.d("CIRCLEVIEW", "isDragging  in ACTION_MOVE");

                    float dx = touchX - lastTouchX;
                    float dy = touchY - lastTouchY;
                    matrix.postTranslate(dx, dy);
                    invalidate();
                    lastTouchX = touchX;
                    lastTouchY = touchY;
                }
                break;

            case MotionEvent.ACTION_UP:
                Log.d("CIRCLEVIEW", "ACTION_UP");
//                checkCircleClick(touchX, touchY);
                break;

            case MotionEvent.ACTION_CANCEL:
                Log.d("CIRCLEVIEW", "ACTION_CANCEL");
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
    public class PinchZoomListener extends SimpleOnScaleGestureListener{

        @Override
        public boolean onScale(ScaleGestureDetector detector){
            Log.d("CIRCLEVIEW", "Touch PinchZoomListener");

            float gestureFactor = detector.getScaleFactor();
            // zoom out
            if(gestureFactor > 1){
                Log.d("CIRCLEVIEW", "Touch PinchZoomListener zoom in");
                scaleFactor *= 1.05f;
            } else { //zoom in
                Log.d("CIRCLEVIEW", "Touch PinchZoomListener zoom out");
                scaleFactor /= 1.05f;
            }

            matrix.reset();
            matrix.postScale(scaleFactor, scaleFactor, getWidth() / 2f, getHeight() / 2f);
            invalidate();
            return true;

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector){
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
                    break; // Exit the loop once a circle is clicked
                }
            }
        }
    }

    private boolean isPointInsideCircle(float x, float y, Circle circle) {
        //Create a float array to represent the touch coordinates as a point.
        float[] point = {x, y};
        matrix.invert(matrix); // Invert the matrix to get the original coordinates
        matrix.mapPoints(point); // Map the touch coordinates to the original coordinates

        //Calculate the distance between the mapped touch coordinates and the circle's center using the Pythagorean theorem.
        float distance = (float) Math.sqrt(Math.pow(point[0] - circle.getX(), 2) + Math.pow(point[1] - circle.getY(), 2));
        return distance <= circle.getRadius();
    }


}
