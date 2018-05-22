package com.tang.trade.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.ImageView;

import com.tang.trade.tang.R;

/**
 * 拦截器
 * <p>
 * Created by Administrator on 2018/4/13.
 */

@SuppressLint("AppCompatCustomView")
public class LimitEditText extends EditText implements TextWatcher {

    protected boolean isInput, isVisible = true;
    private LimitEditText.InterceptListener listener;
    private EnableListener enableListener;

    public LimitEditText(Context context) {
        super(context);
    }

    public LimitEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        addTextChangedListener(this);
    }

    public LimitEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isInput() {
        return isInput;
    }

    /**
     * 输入法
     *
     * @param outAttrs
     * @return
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new mInputConnecttion(super.onCreateInputConnection(outAttrs),
                false, listener);
    }

    /**
     * 拦截操作
     */
    public void setInterceptListener(LimitEditText.InterceptListener listener) {
        this.listener = listener;
    }

    /**
     * 没有输入 按钮不可以点击
     * 有输入 才能操作
     */
    public void setEnableListener(EnableListener enableListener) {
        this.enableListener = enableListener;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s.toString())) {
            //没有数据
            isInput = false;
            if (enableListener != null) {
                enableListener.noInput();
            }
        } else {
            //输入了内容
            isInput = true;
            if (enableListener != null) {
                enableListener.yesInput(s.toString());
            }
        }

    }

    /**
     * 密码是否可见
     *
     * @param eye
     */
    public void showPassword(ImageView eye) {
        if (isVisible) {
            setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            eye.setImageResource(R.mipmap.common_eye_true);
            isVisible = false;
        } else {
            setTransformationMethod(PasswordTransformationMethod.getInstance());
            eye.setImageResource(R.mipmap.common_eye_false);
            isVisible = true;
        }
        setSelection(length());
    }


    public interface EnableListener {
        void noInput();

        void yesInput(String msg);
    }

    public interface InterceptListener {
        boolean check(CharSequence str);
    }
}


class mInputConnecttion extends InputConnectionWrapper implements
        InputConnection {
    public LimitEditText.InterceptListener listener;

    public mInputConnecttion(InputConnection target, boolean mutable, LimitEditText.InterceptListener listener) {
        super(target, mutable);
        this.listener = listener;
    }

    /**
     * 对输入的内容进行拦截
     *
     * @param text
     * @param newCursorPosition
     * @return
     */
    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        // 不能输入汉字
        if (text.toString().matches("[\u4e00-\u9fa5]+")) {
            return false;
        }
        // 不能输入符号
        if (com.tang.trade.utils.TextUtils.isSpecial(text.toString())) {
            return false;
        }
        // 不能输入表情
        if (com.tang.trade.utils.TextUtils.containsEmoji(text.toString())) {
            return false;
        }

        if (listener != null) {
            // 拦截
            if (listener.check(text)) {
                return false;
            }

        }
        return super.commitText(text, newCursorPosition);
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        return super.sendKeyEvent(event);
    }

    @Override
    public boolean setSelection(int start, int end) {
        return super.setSelection(start, end);
    }


}