package pl.merhold.threestateseekbar.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.SeekBar;


/**
 * Created by Paweł Ławiński on 2014-03-22.
 */

public class ThreeStateButton extends SeekBar {

    private static long LONG_TIME_PRESS = 700;
    private State mState;
    private String mCounter;
    private int mCounterPaddingBottom;
    private boolean isCounting;
    private Paint mPaintCounter;
    private Handler timeHandle = new Handler();
    private Handler holdTimeHandler = new Handler();
    private OnStateChangeListener mListener;
    private Drawable mThumb;
    private long startTime = 0L;
    private long holdTime = 0L;
    private long startHoldTime = 0L;
    private Runnable dragTimer = new Runnable() {
        @Override
        public void run() {
            holdTime = (SystemClock.uptimeMillis() - startHoldTime);
            holdTimeHandler.postDelayed(this, 0);
        }
    };
    private long ms = 0L;
    private Runnable counterTimer = new Runnable() {
        @Override
        public void run() {
            ms = SystemClock.uptimeMillis() - startTime;
            mCounter = getFormattedString(ms);
            invalidate();
            timeHandle.postDelayed(this, 0);
        }
    };
    private float mInitializeX;
    private int mTouchSlop;
    private boolean isDragging;

    public ThreeStateButton(Context context) {
        super(context);
        init(context);
    }

    public ThreeStateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ThreeStateButton, 0, 0);

        try {
            mPaintCounter.setColor(array.getInt(R.styleable.ThreeStateButton_textColor, Color.WHITE));
            mPaintCounter.setTextSize(array.getInt(R.styleable.ThreeStateButton_textSize, 180));
        } finally {
            array.recycle();
        }
    }

    private void init(Context context) {

        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();

        mCounter = getFormattedString(0);
        mCounterPaddingBottom = 10;
        isCounting = false;
        isDragging = false;

        Typeface tf = Typeface.createFromAsset(context.getAssets(), "roboto.ttf");
        mPaintCounter = new Paint();
        mPaintCounter.setTypeface(tf);
        mPaintCounter.setAntiAlias(true);
        mPaintCounter.setDither(true);

        setThumb(getResources().getDrawable(R.drawable.thumb));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_UP:
                setState(getStateByProgress(getProgress()));
                if (isDragging)
                    stopDrag();
                return true;
            case MotionEvent.ACTION_DOWN:
                mInitializeX = event.getX();
                float mInitializeY = event.getY();
                startHoldTime = SystemClock.uptimeMillis();
                holdTimeHandler.postDelayed(dragTimer, 0);
                stopTimer();
                break;
            case MotionEvent.ACTION_MOVE:
                if (holdTime > LONG_TIME_PRESS)
                    if (Math.abs(mInitializeX - event.getX()) < mTouchSlop) {
                        startDrag();
                    } else {
                        holdTimeHandler.removeCallbacks(dragTimer);
                        holdTime = 0;
                    }
                if (isDragging) {
                    float x = event.getRawX();
                    float y = event.getRawY();
                    this.setX(x - this.getWidth() / 2);
                    this.setY(y - this.getHeight());
                    return false;
                }
                break;

        }
        return super.onTouchEvent(event);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.drawText(mCounter, getWidth() / 2 - (mPaintCounter.measureText(mCounter) / 2), getHeight() - mCounterPaddingBottom, mPaintCounter);
        getProgressDrawable().setBounds(0, 0, getProgressDrawable().getBounds().right, getSeekBarThumb().getBounds().height() / 4 * 3);
        super.onDraw(canvas);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Rect bounds = new Rect();
        mPaintCounter.getTextBounds("0", 0, 1, bounds);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + bounds.height() + mCounterPaddingBottom * 2);
    }

    private State getStateByProgress(int progress) {
        if (progress < getMax() / 4)
            return State.START;
        else if ((progress >= getMax() / 4) && (progress <= getMax() / 4 * 3))
            return State.MIDDLE;
        else if (progress > getMax() / 4 * 3)
            return State.END;

        return State.START;
    }

    private boolean animateSeekBar(int aim) {
        ValueAnimator animation = ValueAnimator.ofInt(getProgress(), aim);
        animation.setDuration(200);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animProgress = (Integer) valueAnimator.getAnimatedValue();
                setProgress(animProgress);
            }
        });
        animation.start();
        return true;
    }

    private void updateProgressByState(State state) {
        switch (state) {
            case START:
                animateSeekBar(0);
                break;
            case MIDDLE:
                animateSeekBar(getMax() / 2);
                break;
            case END:
                animateSeekBar(getMax());
                break;
        }
    }

    public void setTextColor(int color) {
        mPaintCounter.setColor(color);
        invalidate();
    }

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        mState = state;
        updateProgressByState(mState);
        if (mListener != null) mListener.onStateChange(mState);
        if (isCounting) stopTimer();
        startTimer();
    }

    public void startTimer() {
        startTime = SystemClock.uptimeMillis();
        timeHandle.postDelayed(counterTimer, 0);
        isCounting = true;
    }

    public void stopTimer() {
        timeHandle.removeCallbacks(counterTimer);
        isCounting = false;
        mCounter = getFormattedString(0);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumb = thumb;
    }

    public Drawable getSeekBarThumb() {
        return mThumb;
    }

    private String getFormattedString(long ms) {
        return String.format("%d", ms / 1000);
    }

    public void setListener(OnStateChangeListener listener) {
        mListener = listener;
    }

    private void startDrag() {
        isDragging = true;
        this.animate().scaleX(0.5f);
        this.animate().scaleY(0.5f);
    }

    private void stopDrag() {
        isDragging = false;
        this.animate().scaleX(1.0f);
        this.animate().scaleY(1.0f);
    }

    public enum State {
        START, MIDDLE, END
    }

    public interface OnStateChangeListener {
        void onStateChange(State state);
    }

}
