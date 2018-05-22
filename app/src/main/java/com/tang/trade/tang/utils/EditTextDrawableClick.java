package com.tang.trade.tang.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhuhaikuan on 2018/1/5.
 */

public class EditTextDrawableClick extends android.support.v7.widget.AppCompatEditText {
    private DrawableLeftListener mLeftListener;
    private DrawableRightListener mRightListener;
    private DrawableTopListener mTopListener;
    private DrawableBottomListener mBottomListener;

    private final int DRAWABLE_LEFT = 0;
    private final int DRAWABLE_TOP = 1;
    private final int DRAWABLE_RIGHT = 2;
    private final int DRAWABLE_BOTTOM = 3;

    public EditTextDrawableClick(Context context) {
        super(context);
    }

    public EditTextDrawableClick(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextDrawableClick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mLeftListener != null) {
                Drawable drawableLeft = getCompoundDrawables()[DRAWABLE_LEFT];
                if (drawableLeft != null && event.getX() <= getLeft() + drawableLeft.getIntrinsicWidth()) {
                    mLeftListener.onDrawableLeftClick(this);
                }
            }

            if (mTopListener != null) {
                Drawable drawableTop = getCompoundDrawables()[DRAWABLE_TOP];
                if (drawableTop != null && event.getY() <= getTop() + drawableTop.getIntrinsicHeight()) {
                    mTopListener.onDrawableTopClick(this);
                }
            }

            if (mRightListener != null) {
                Drawable drawableRight = getCompoundDrawables()[DRAWABLE_RIGHT];
                if (drawableRight != null && event.getX() > getWidth() - getPaddingRight() - drawableRight.getIntrinsicWidth()) {
                    Log.d("DEBUG", "getX=" + event.getX() + "getY=" + event.getY() + "\n" + "getRawX=" + event.getRawX()
                            + "getRawY=" + event.getY() + "\n" + "test=" + getLeft() + "\n" + "test:hehe" + getPaddingLeft());
                    mRightListener.onDrawableRightClick(this);
                }
            }

            if (mBottomListener != null) {
                Drawable drawableBottom = getCompoundDrawables()[DRAWABLE_BOTTOM];
                if (drawableBottom != null && event.getX() > getHeight() - drawableBottom.getIntrinsicWidth()) {
                    mBottomListener.onDrawableBottomClick(this);
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public void setDrawableLeftListener(DrawableLeftListener mListener) {
        this.mLeftListener = mListener;
    }

    public void setDrawableTopListener(DrawableTopListener mListener) {
        this.mTopListener = mListener;
    }

    public void setDrawableRightListener(DrawableRightListener mListener) {
        this.mRightListener = mListener;
    }

    public void setDrawableBottomListener(DrawableBottomListener mListener) {
        this.mBottomListener = mListener;
    }

    public interface DrawableLeftListener {
        void onDrawableLeftClick(View view);
    }

    public interface DrawableTopListener {
        void onDrawableTopClick(View view);
    }

    public interface DrawableRightListener {
        void onDrawableRightClick(View view);
    }

    public interface DrawableBottomListener {
        void onDrawableBottomClick(View view);
    }
}
