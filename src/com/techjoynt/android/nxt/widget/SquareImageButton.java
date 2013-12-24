package com.techjoynt.android.nxt.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class SquareImageButton extends ImageButton {

    public SquareImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageButton(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (width > height) {
            width = height;
        } else {
            height = width;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        );
    }
}
