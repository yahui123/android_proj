package com.tang.trade.tang.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhuhaikuan on 2018/1/5.
 */

public class EditTextDrawableClick extends android.support.v7.widget.AppCompatEditText {
    private final int DRAWABLE_LEFT = 0;
    private final int DRAWABLE_TOP = 1;
    private final int DRAWABLE_RIGHT = 2;
    private final int DRAWABLE_BOTTOM = 3;

    private OnDrawableClickListener mOnDrawableClickListener;

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

            if (mOnDrawableClickListener != null) {
                if (getCompoundDrawables()[DRAWABLE_LEFT] != null && event.getX() <= getLeft() + getCompoundDrawables()[DRAWABLE_LEFT].getIntrinsicWidth() + 40) {
                    mOnDrawableClickListener.onDrawableLeftClick(this);
                    hideSoftInput();
                } else if (getCompoundDrawables()[DRAWABLE_TOP] != null && event.getY() <= getTop() + getCompoundDrawables()[DRAWABLE_TOP].getIntrinsicHeight()) {
                    mOnDrawableClickListener.onDrawableTopClick(this);
                } else if (getCompoundDrawables()[DRAWABLE_RIGHT] != null && event.getX() > getWidth() - getPaddingRight() - getCompoundDrawables()[DRAWABLE_RIGHT].getIntrinsicWidth()) {
                    mOnDrawableClickListener.onDrawableRightClick(this);
                    hideSoftInput();
                } else if (getCompoundDrawables()[DRAWABLE_BOTTOM] != null && event.getX() > getHeight() - getCompoundDrawables()[DRAWABLE_BOTTOM].getIntrinsicWidth()) {
                    mOnDrawableClickListener.onDrawableBottomClick(this);
                    hideSoftInput();
                } else {
                    showSoftInput();
                }
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * when click the drawable area don't show the soft input
     */
    private void hideSoftInput() {
        setFocusableInTouchMode(false);
        setFocusable(false);
    }

    /**
     * when click not drawable area show the soft input
     */
    private void showSoftInput() {
        setFocusableInTouchMode(true);
        setFocusable(true);
    }

    public OnDrawableClickListener getOnDrawableClickListener() {
        return mOnDrawableClickListener;
    }

    public void setOnDrawableClickListener(OnDrawableClickListener onDrawableClickListener) {
        this.mOnDrawableClickListener = onDrawableClickListener;
    }

    public interface OnDrawableClickListener {
        void onDrawableBottomClick(View view);

        void onDrawableLeftClick(View view);

        void onDrawableTopClick(View view);

        void onDrawableRightClick(View view);
    }


}
