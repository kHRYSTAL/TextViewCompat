package me.khrystal.widget;

import android.content.Context;
import android.os.Build;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * usage: fix TextView move up when setMaxLines and LinkSpan, support long press when select
 * author: kHRYSTAL
 * create time: 2020-04-08
 * update time:
 * email: 723526676@qq.com
 */
public class TextViewCompat extends AppCompatTextView {

    private boolean isTextSelectable;
    private TextClickListener listener;

    public TextViewCompat(Context context) {
        super(context);
    }

    public TextViewCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTextSelectable) {
            return executeNoSelectableTouchEvent(event);
        } else {
            return executeSelectableDetailTouchEvent(event);
        }
    }

    /**
     * fix link dislocation when touch {@see https://yangqiuyan.github.io/2018/11/21/LinkMovementMethod/}
     * such as in RecyclerView
     * but show system popup not work if setTextIsSelectable
     */
    private boolean executeNoSelectableTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP && getText() != null) {
            if (getText() instanceof Spannable) {
                if (event.getEventTime() - event.getDownTime() < ViewConfiguration
                        .getLongPressTimeout() - 10) {
                    handleLinkClick(event);
                    return true;
                }
            } else {
                performClick();
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean executeSelectableDetailTouchEvent(MotionEvent event) {
        CharSequence text = getText();
        if (text != null && text instanceof Spannable) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                if (event.getEventTime() - event.getDownTime() < ViewConfiguration
                        .getLongPressTimeout() - 10) {
                    handleLinkClick(event); // custom handle
                    return true;
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

        InterceptClickSpan[] link = ((Spannable) getText()).getSpans(off, off, InterceptClickSpan.class);
        if (link.length > 0) {
            link[0].onClick(this);
        } else {
            performClick();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0 && isTextSelectable) {
            requestFocus();
        }
    }

    /**
     * fix setEllipsize(END)&&setMaxLines(Number) not display '...'
     * the width must wrap_content
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        StaticLayout layout = null;
        Field field = null;
        try {
            Field staticField = DynamicLayout.class.getDeclaredField("sStaticLayout");
            staticField.setAccessible(true);
            layout = (StaticLayout) staticField.get(DynamicLayout.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (layout != null) {
            try {
                field = StaticLayout.class.getDeclaredField("mMaximumVisibleLineCount");
                field.setAccessible(true);
                field.setInt(layout, getMaxLines());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (layout != null && field != null) {
            try {
                field.setInt(layout, Integer.MAX_VALUE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param selectable if you want solute system popup not show, you must set width is match_parent
     *                   so this is conflict if you want show '...' in end
     */
    @Override
    public void setTextIsSelectable(boolean selectable) {
        super.setTextIsSelectable(selectable);
        this.isTextSelectable = selectable;
    }


    public void setSpanClickListener(TextClickListener listener) {
        this.listener = listener;
    }

    /**
     * if you want span can click, you need extend or use this span
     * and setSpanClickListener
     */
    public class InterceptClickSpan extends ClickableSpan {

        private Context context;
        private String text;

        InterceptClickSpan(Context context, String text) {
            this.context = context;
            this.text = text;
        }

        @Override
        public void onClick(@NonNull View arg0) {
            if (listener != null) {
                listener.onClickListener(context, text);
            }
        }
    }

    public interface TextClickListener {
        public void onClickListener(Context context, String link);
    }
}
