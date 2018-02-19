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

public class ViewSlider extends View implements Runnable {

    // Constants
    public final static long DEFAULT_LOOP_INTERVAL = 50; // 50 ms

    // Variables
    private OnSliderMoveListener onSliderMoveListener; // Listener
    private Thread thread = new Thread(this);
    private long loopInterval = DEFAULT_LOOP_INTERVAL;

    private float xPosition = 0; // Touch x position
    private float yPosition = 0; // Touch y position
    private float centerX = 0;
    private float centerY = 0; // Center view y position
    private float dy = 0;

    Bitmap base;
    Bitmap stick;
    // private Paint redText;

    private int sliderHeight;
    private int stickDiameter;

    public ViewSlider(Context context) {
        super(context);
    }

    public ViewSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }

    public ViewSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initJoystickView();
    }

    protected void initJoystickView() {

        /**
         * All necessary bitmaps for this view are initialized.
         */
        base = BitmapFactory.decodeResource(getResources(), R.drawable.sliderbase);
        stick = BitmapFactory.decodeResource(getResources(), R.drawable.slider);

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
         * The relations of the different parts of the slider visualization
         * are calculated every time this Views size is changed. This is usually the
         * case only at the start of the application.
         *
         * The sizes of all parts are all calculated in relation to the size of the View.
         */
        int d = hNew;
        stickDiameter = (int)(d * 0.15);
        sliderHeight = (int)(d * 0.75);
        xPosition = getWidth()/2;
        yPosition = (getHeight()+sliderHeight)/2;
        base = Bitmap.createScaledBitmap(base, (int)(sliderHeight*0.3), sliderHeight, true);
        stick = Bitmap.createScaledBitmap(stick, (int)(stickDiameter*1.5), stickDiameter, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int d = measure(heightMeasureSpec);
        setMeasuredDimension((int)(d*0.3), d);
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
        canvas.drawBitmap(stick, xPosition-stick.getWidth()/2, yPosition-stick.getHeight()/2, null);

        //canvas.drawText(String.valueOf(dy), 5, 10, redText);

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        yPosition = event.getY();

        /**
         * dy is calculated from the center of the View
         * to the placement of the finger on the touchscreen
         */
        dy = centerY - yPosition;

        /**
         * yPosition will be held in the bounds of the Slider
         */
        if (dy > (sliderHeight/2))
            yPosition = (int)centerY - sliderHeight/2;
            if (dy < (-sliderHeight/2))
            yPosition = (int)centerY + sliderHeight/2;
        /*
        dy = centerY - yPosition;
        dy = dy/(sliderHeight/2)*500+500;
        */

        /**
         * Touch events are registered here
         */
        invalidate();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            /*
            dy = centerY - yPosition;
            dy = dy/(sliderHeight/2)*500+500;
            */
            thread.interrupt();
            if (onSliderMoveListener != null)
                onSliderMoveListener.onValueChanged(getValue());
        }
        if (onSliderMoveListener != null
                && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.start();
            if (onSliderMoveListener != null)
                onSliderMoveListener.onValueChanged(getValue());
        }
        return true;
    }

    private float getValue() {
        /**
         * The current difference of the slider in x-direction from the
         * center position is calculated here. dy is measured from the bottom,
         * therefore the addition of +500 is necessary.
         */
        dy = centerY - yPosition;
        dy = dy/(sliderHeight/2)*500+500;
        return dy;
    }

    public void setOnSliderMoveListener(OnSliderMoveListener listener,
                                          long repeatInterval) {
        this.onSliderMoveListener = listener;
        this.loopInterval = repeatInterval;
    }
    public static interface OnSliderMoveListener {
        public void onValueChanged(float value);
    }
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (onSliderMoveListener != null)
                        onSliderMoveListener.onValueChanged(getValue());
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
