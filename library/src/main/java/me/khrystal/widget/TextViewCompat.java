package me.khrystal.widget;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * usage: fix TextView move up when setMaxLines and LinkSpan, support long press when select
 * <p> textview need support click link, and select text, setTextIsSelectable and setMovementMethod would conflict, at least some phone</p>
 * solution is LinkMovement feature use handleLinkClick to implement
 * <p> long press to select text in some phone(mi 6), will touch off ACTION_CANCEL, it will caused text select but popup not display</p>
 * solution is when touch off ACTION_CANCEL, judge whether or not select text, if yes, make ACTION_CANCEL change to ACTION_UP give super,
 * it may be broke system logic
 * <p>base on question 2, some phone(Huawei), always has problem, we can find if view has focus can show popup</p>
 * solution is in onWindowVisibilityChanged, request focus
 * author: kHRYSTAL
 * create time: 2020-04-08
 * update time:
 * email: 723526676@qq.com
 */
public class TextViewCompat extends AppCompatTextView {

    private boolean handleLinkClick;

    public TextViewCompat(Context context) {
        super(context);
    }

    public TextViewCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setHandleLinkClick(boolean handleLinkClick) {
        this.handleLinkClick = handleLinkClick;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!handleLinkClick) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP && getText() != null && getText() instanceof Spannable) {
                if (event.getEventTime() - event.getDownTime() < ViewConfiguration
                        .getLongPressTimeout() - 10) {
                    handleLinkClick(event);
                    return true;
                }
            }
        }

        if (getText() != null && getText() instanceof Spannable) {

            int action = event.getAction();

            if (action == MotionEvent.ACTION_CANCEL) {
                int selectionStart = getSelectionStart();
                int selectionEnd = getSelectionEnd();
                if (selectionStart != selectionEnd && selectionStart >= 0) {
                    action = MotionEvent.ACTION_UP;
                    event.setAction(MotionEvent.ACTION_UP);
                }
            }

            if (action == MotionEvent.ACTION_UP) {
                if (event.getEventTime() - event.getDownTime() < ViewConfiguration
                        .getLongPressTimeout() - 10) {
                    handleLinkClick(event);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void handleLinkClick(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= getTotalPaddingLeft();
        y -= getTotalPaddingTop();

        x += getScrollX();
        y += getScrollY();

        Layout layout = getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        ClickableSpan[] link = ((Spannable) getText()).getSpans(off, off, ClickableSpan.class);
        if (link.length > 0) {
            link[0].onClick(this);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0 && handleLinkClick) {
            requestFocus();
        }
    }
}
