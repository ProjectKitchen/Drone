package at.technikum_wien.fhtw_drohne;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ViewJoystick extends View implements Runnable {

    // Constants
    public final static long DEFAULT_LOOP_INTERVAL = 50; // 50 ms

    // Variables
    private OnJoystickMoveListener onJoystickMoveListener; // Listener
    private Thread thread = new Thread(this);
    private long loopInterval = DEFAULT_LOOP_INTERVAL;

    private float xPosition = 0; // Touch x position
    private float yPosition = 0; // Touch y position
    private float centerX = 0; // Center view x position
    private float centerY = 0; // Center view y position
    private float dx = 0;
    private float dy = 0;

    Bitmap base;
    Bitmap stick;
    private Paint blackLine;
    //private Paint redText;

    private int joystickWidth;
    private int stickDiameter;

    public ViewJoystick(Context context) {
        super(context);
    }

    public ViewJoystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }

    public ViewJoystick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initJoystickView();
    }

    protected void initJoystickView() {

        /**
         * All necessary bitmaps and colors for this view are initialized.
         */
        base = BitmapFactory.decodeResource(getResources(), R.drawable.joystickbase);
        stick = BitmapFactory.decodeResource(getResources(), R.drawable.stick);

        blackLine = new Paint();
        blackLine.setStrokeWidth(20);
        blackLine.setColor(Color.BLACK);
        /*
        redText = new Paint(Paint.ANTI_ALIAS_FLAG);
        redText.setColor(Color.RED);
        redText.setStyle(Paint.Style.FILL_AND_STROKE);
        */
    }

    @Override
    protected void onSizeChanged(int wNew, int hNew, int wOld, int hOld) {
        super.onSizeChanged(wNew, hNew, wOld, hOld);

        /**
         * The relations of the different parts of the joystick visualization
         * are calculated every time this Views size is changed. This is usually the
         * case only at the start of the application.
         *
         * The sizes of all parts are all calculated in relation to the size of the View.
         */
        xPosition = getWidth()/2;
        yPosition = getHeight()/2;
        int d = Math.min(wNew, hNew);
        stickDiameter = (int)(d * 0.25);
        joystickWidth = (int)(d * 0.75);
        blackLine.setStrokeWidth((int)(d*0.15));
        base = Bitmap.createScaledBitmap(base, joystickWidth, joystickWidth, true);
        stick = Bitmap.createScaledBitmap(stick, stickDiameter, stickDiameter, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        setMeasuredDimension(d, d);
    }
    private int measure(int measureSpec) {
        int result = 0;
        // Decode the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            result = 200;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;
        // paint control lever
        canvas.drawBitmap(base, centerX-base.getWidth()/2, centerY-base.getHeight()/2, null);
        canvas.drawLine(centerX, centerY, xPosition, yPosition, blackLine);
        canvas.drawCircle(centerX, centerY, blackLine.getStrokeWidth()/2, blackLine);
        canvas.drawBitmap(stick, xPosition-stick.getWidth()/2, yPosition-stick.getHeight()/2, null);
        /*
        canvas.drawText(String.valueOf(dx), 5, 10, redText);
        canvas.drawText(String.valueOf(dy), 5, 20, redText);
        */
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xPosition = event.getX();
        yPosition = event.getY();

        /**
         * dx and dy are calculated from the center of the View
         * to the placement of the finger on the touchscreen
         */
        dx = xPosition - centerX;
        dy = centerY - yPosition;

        /**
         * xPosition and yPosition will be held in the bounds of the Joystick
         */
        if (dx > (joystickWidth/2))
            xPosition = (int)centerX + joystickWidth/2;
        if (dx < (-joystickWidth/2))
            xPosition = (int)centerX - joystickWidth/2;
        if (dy > (joystickWidth/2))
            yPosition = (int)centerY - joystickWidth/2;
        if (dy < (-joystickWidth/2))
            yPosition = (int)centerY + joystickWidth/2;

        /*
        dx = xPosition - centerX;
        dx = dx/(joystickWidth/2)*500;
        dy = centerY - yPosition;
        dy = dy/(joystickWidth/2)*500;
        */

        /**
         * Touch events are registered here
         */
        invalidate();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            xPosition = centerX;
            yPosition = centerY;
            /*
            dx = xPosition - centerX;
            dy = centerY - yPosition;
            */
            thread.interrupt();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getValueX(), getValueY());
        }
        if (onJoystickMoveListener != null
                && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.start();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getValueX(), getValueY());
        }
        return true;
    }

    private float getValueX() {
        /**
         * The current difference of the joystick in x-direction from the
         * center position is calculated here.
         */
        dx = xPosition - centerX;
        dx = dx/(joystickWidth/2)*500;
        return dx;
    }

    private float getValueY() {
        /**
         * The current difference of the joystick in y-direction from the
         * center position is calculated here.
         */
        dy = centerY - yPosition;
        dy = dy/(joystickWidth/2)*500;
        return dy;
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener listener, long repeatInterval) {
        this.onJoystickMoveListener = listener;
        this.loopInterval = repeatInterval;
    }

    public static interface OnJoystickMoveListener {
        public void onValueChanged(float valueX, float valueY);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (onJoystickMoveListener != null)
                        onJoystickMoveListener.onValueChanged(getValueX(), getValueY());
                }
            });
            try {
                Thread.sleep(loopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
