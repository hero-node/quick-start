package exocr.bankcard;

/* Logo.java
 * See the file "LICENSE.md" for the full license governing this code.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;

class Logo {

    private static final int ALPHA = 100;
    private final Paint mPaint;

    private Bitmap mLogo;
    private final Context mContext;

    public Logo(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(ALPHA);
        mLogo = null;
        mContext = context;
    }

    void loadLogo() {
        if (mLogo != null) {
            return; // no change, don't reload
        }
        int density = DisplayMetrics.DENSITY_HIGH;
        mLogo = ViewUtil.base64ToBitmap(ViewUtil.exocr_logo96, mContext, density);
    }

    public void draw(Canvas canvas, float maxWidth, float maxHeight) {

        if (mLogo == null) {
            loadLogo();
        }

        canvas.save();

        float drawWidth, drawHeight;
        float targetAspectRatio = (float) mLogo.getHeight() / mLogo.getWidth();
        if ((maxHeight / maxWidth) < targetAspectRatio) {
            drawHeight = maxHeight;
            drawWidth = maxHeight / targetAspectRatio;
        } else {
            drawWidth = maxWidth;
            drawHeight = maxWidth * targetAspectRatio;
        }

        float halfWidth = drawWidth / 2;
        float halfHeight = drawHeight / 2;

        canvas.drawBitmap(mLogo, new Rect(0, 0, mLogo.getWidth(), mLogo.getHeight()), new RectF(
                -halfWidth, -halfHeight, halfWidth, halfHeight), mPaint);

        canvas.restore();
    }

}
