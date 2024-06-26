package kore.botssdk.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import kore.botssdk.R;

public class CustomTextViewSemiBold extends AppCompatTextView {

    String customFont;

    public CustomTextViewSemiBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        style(context, attrs);
    }

    public CustomTextViewSemiBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        style(context, attrs);

    }

    private void style(Context context, AttributeSet attrs) {

        Typeface tfRegular = ResourcesCompat.getFont(context, R.font.latosemibold);
        setTypeface(tfRegular);
    }
}
