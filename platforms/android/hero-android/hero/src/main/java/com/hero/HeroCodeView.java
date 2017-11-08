package com.hero;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by drjr92 on 17-3-14.
 */
public class HeroCodeView extends HeroTextField {
    private Paint paint;
    private Paint paintContent;
    private Paint paintArc;
    private float radiusBg;
    private float radiusArc;
    private int padding = 1;
    private int drawWidth=1;
    private int PaintLastArcAnimDuration = 200;
    private PaintLastArcAnim paintLastArcAnim;
    private int textLength;
    private int maxLineSize = 6;
    private boolean addText = true;
    private float interpolatedTime;
    private boolean isShadow = false;

    public HeroCodeView(Context context) {
        this(context, null);
        init();
    }

    public HeroCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public HeroCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF rectIn = new RectF(padding, padding, getMeasuredWidth() - padding, getMeasuredHeight() - padding);
        canvas.drawRoundRect(rectIn, radiusBg, radiusBg, paintContent);

        RectF rect = new RectF(padding, padding, getMeasuredWidth() - padding, getMeasuredHeight() - padding);
        paint.setStrokeWidth(drawWidth);
        canvas.drawRoundRect(rect, radiusBg, radiusBg, paint);

        float cx, cy = getMeasuredHeight() / 2;
        float half = getMeasuredWidth() / maxLineSize / 2;
        paint.setStrokeWidth(drawWidth);
        for (int i = 1; i < maxLineSize; i++) {
            float x = getMeasuredWidth() * i / maxLineSize;
            canvas.drawLine(x, 0, x, getMeasuredHeight(), paint);
        }

        if (!isShadow) {
            paintArc.setTextSize(dip2px(textSize));
            for (int i = 0; i < textLength; i++) {
                cx = getMeasuredWidth() * i / maxLineSize + half;
                String text = String.valueOf(getText().toString().charAt(i));
                canvas.drawText(text, cx - getFontlength(paintArc, text) / 2.0f, cy + getFontHeight(paintArc, text) / 2.0f, paintArc);
            }

        } else {
            for (int i = 0; i < maxLineSize; i++) {
                cx = getMeasuredWidth() * i / maxLineSize + half;
                if (addText) {
                    if (i < textLength - 1) {
                        canvas.drawCircle(cx, cy, radiusArc, paintArc);
                    } else if (i == textLength - 1) {

                        canvas.drawCircle(cx, cy, radiusArc * interpolatedTime, paintArc);
                    }
                } else {
                    if (i < textLength) {
                        canvas.drawCircle(cx, cy, radiusArc, paintArc);
                    } else if (i == textLength) {
                        canvas.drawCircle(cx, cy, radiusArc - radiusArc * interpolatedTime, paintArc);
                    }
                }
            }
        }

    }


    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        Editable editable = this.getText();
        if (editable.length() - textLength >= 0) {
            addText = true;
        } else {
            addText = false;
        }
        textLength = editable.length();

        if (textLength > maxLineSize) {
            int selEndIndex = Selection.getSelectionEnd(editable);
            String str = editable.toString();
            String newStr = str.substring(0, maxLineSize);
            this.setText(newStr);
            editable = this.getText();

            int newLen = editable.length();
            if (selEndIndex > newLen) {
                selEndIndex = editable.length();
            }
            Selection.setSelection(editable, selEndIndex);

        } else {
            if (paintLastArcAnim != null) {
                clearAnimation();
                startAnimation(paintLastArcAnim);
            } else {
                invalidate();
            }
        }
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        super.on(jsonObject);

        if (jsonObject.has("count")) {
            maxLineSize = jsonObject.optInt("count", 6);
            this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLineSize)});
        }
        if (jsonObject.has("secure")) {
            isShadow = jsonObject.optBoolean("secure", false);
        }
        if (jsonObject.has("borderWidth")) {
            drawWidth = jsonObject.getInt("borderWidth");
        }
        if (jsonObject.has("cornerRadius")) {
            radiusBg = dip2px((float) jsonObject.getDouble("cornerRadius"));
        }
        if (jsonObject.has("borderColor")) {
            int borderColor = HeroView.parseColor("#" + jsonObject.getString("borderColor"));
            paint.setColor(borderColor);
        }

    }

    private void init() {
        paintLastArcAnim = new PaintLastArcAnim();
        paintLastArcAnim.setDuration(PaintLastArcAnimDuration);
        radiusBg = dip2px(4);
        radiusArc = dip2px(6);
        textLength = 0;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GRAY);

        paintContent = new Paint();
        paintContent.setAntiAlias(true);
        paintContent.setStyle(Paint.Style.FILL);
        paintContent.setColor(Color.WHITE);

        paintArc = new Paint();
        paintArc.setAntiAlias(true);
        paintArc.setStyle(Paint.Style.FILL);
        paintArc.setColor(Color.argb(155, 0, 0, 0));
        setCursorVisible(false);
        setFocusableInTouchMode(true);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    private class PaintLastArcAnim extends Animation {
        @Override
        protected void applyTransformation(float time, Transformation t) {
            super.applyTransformation(time, t);
//            radiusArc_last = radiusArc * interpolatedTime;
            interpolatedTime = time;
            postInvalidate();
        }
    }

    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public float getFontlength(Paint paint, String str) {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.width();
    }

    public float getFontHeight(Paint paint, String str) {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.height();

    }

}
