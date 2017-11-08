package exocr.bankcard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

class OverlayView extends View {
    private static final String TAG = OverlayView.class.getSimpleName();

    private static final float GUIDE_FONT_SIZE = 22.0f;
    private static final float GUIDE_LINE_PADDING = 8.0f;
    private static final float GUIDE_LINE_HEIGHT = GUIDE_FONT_SIZE + GUIDE_LINE_PADDING;
    private static final float CARD_NUMBER_MARKUP_FONT_SIZE = GUIDE_FONT_SIZE + 2;

    private static final int   GUIDE_STROKE_WIDTH = 11;
    private static final float CORNER_RADIUS_SIZE = 1 / 15.0f;
    
    private static final int TORCH_WIDTH = 70;
    private static final int TORCH_HEIGHT = 50;
    private static final int LOGO_MAX_WIDTH = 100;
    private static final int LOGO_MAX_HEIGHT = TORCH_HEIGHT;
    private static final int BUTTON_TOUCH_TOLERANCE = 20;

    private final WeakReference<CardRecoActivity> mScanActivityRef;
    
    private Rect mGuide;
    private int mState;
    public static String scanInstructions;
    public static String supportInstructions;
    
    // Keep paint objects around for high frequency methods to avoid re-allocating them.
    private final Paint mGuidePaint;
    private Rect mCameraPreviewRect;
    
    private final Torch mTorch;
    private final Logo mLogo;
    
    private Rect mTorchRect;
    private Rect mLogoRect;
    private boolean mShowLogo = true;
    private boolean mShowTorch= true;
    private float mScale = 1;
    
    //// colors for display
    private int maskColor = 0x60000000;
    private int instColor = Color.GREEN;
    private int guideColor;
    private int laserColor = Color.GREEN;
    private int supportColor = Color.LTGRAY;
    
    ////guide line
    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 100L;
    private int scannerAlpha;

    private static int cC = 0;
    
    public OverlayView(CardRecoActivity captureActivity, AttributeSet attributeSet, boolean showTorch) {
        super(captureActivity, attributeSet);

        mShowTorch = showTorch;
        mScanActivityRef = new WeakReference<CardRecoActivity>(captureActivity);
        // card.io is designed for an hdpi screen (density = 1.5);
        mScale = getResources().getDisplayMetrics().density / 1.5f;
        mTorch = new Torch(TORCH_WIDTH * mScale, TORCH_HEIGHT * mScale);
        mLogo  = new Logo(captureActivity);
        mGuidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //
        scanInstructions = "请将扫描线对准银行卡号并对齐左右边缘";
        supportInstructions = "本技术由易道博识提供";
        //scanInstructions = "";
        scannerAlpha = 4;
    }
    
    // Public methods used by CardIOActivity
    public void setGuideAndRotation(Rect rect) {
        Log.d(TAG, "setGuideAndRotation: " + rect);
        mGuide = rect;
        invalidate();
        
        Point topEdgeUIOffset;
        topEdgeUIOffset = new Point((int) (60 * mScale), (int) (40 * mScale));
 
        if (mCameraPreviewRect != null) {
            Log.d(TAG, "" + mCameraPreviewRect + ", " + topEdgeUIOffset + ", " + mCameraPreviewRect + ", " + topEdgeUIOffset);
            Point torchPoint = new Point(mCameraPreviewRect.left + topEdgeUIOffset.x, mCameraPreviewRect.top + topEdgeUIOffset.y);
            // mTorchRect used only for touch lookup, not layout
            mTorchRect = ViewUtil.rectGivenCenter(torchPoint, (int)(TORCH_WIDTH * mScale), (int)(TORCH_HEIGHT * mScale));
            // mLogoRect used only for touch lookup, not layout
            Point logoPoint = new Point(mCameraPreviewRect.right - topEdgeUIOffset.x, mCameraPreviewRect.top + topEdgeUIOffset.y);
            mLogoRect = ViewUtil.rectGivenCenter(logoPoint, (int) (LOGO_MAX_WIDTH * mScale), (int) (LOGO_MAX_HEIGHT * mScale)); 
        }
    }
    
    // Drawing methods
    private Rect guideStrokeRect(int x1, int y1, int x2, int y2) {
        Rect r;
        int t2 = (int) (GUIDE_STROKE_WIDTH / 2 * mScale);
        r = new Rect();

        r.left = Math.min(x1, x2) - t2;
        r.right = Math.max(x1, x2) + t2;

        r.top = Math.min(y1, y2) - t2;
        r.bottom = Math.max(y1, y2) + t2;

        return r;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mGuide == null || mCameraPreviewRect == null) {
        	return;
        }
        int tickLength;
        int width = canvas.getWidth();
		int height = canvas.getHeight();
		 Log.i("DEBUG_TIME", "OverlayView_onDraw="+System.currentTimeMillis());
		//
		tickLength = (mGuide.bottom - mGuide.top)/8;
        canvas.save();
        
		// Draw the exterior (i.e. outside the framing rect) darkened
        mGuidePaint.clearShadowLayer();
        mGuidePaint.setColor(maskColor);
		canvas.drawRect(0, 0, width, mGuide.top, mGuidePaint);
		canvas.drawRect(0, mGuide.top, mGuide.left, mGuide.bottom + 1, mGuidePaint);
		canvas.drawRect(mGuide.right + 1, mGuide.top, width, mGuide.bottom + 1, mGuidePaint);
		canvas.drawRect(0, mGuide.bottom + 1, width, height, mGuidePaint);
		
        // Draw guide lines
        mGuidePaint.clearShadowLayer();
        mGuidePaint.setStyle(Paint.Style.FILL);
        mGuidePaint.setColor(guideColor);
        /*
        // left
        canvas.drawRect(guideStrokeRect(mGuide.left, mGuide.top,    mGuide.left + tickLength, mGuide.top),    mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.left, mGuide.bottom, mGuide.left + tickLength, mGuide.bottom), mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.left, mGuide.top,    mGuide.left,              mGuide.bottom), mGuidePaint);
        
        //right
        canvas.drawRect(guideStrokeRect(mGuide.right, mGuide.top, mGuide.right - tickLength,  mGuide.top),    mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.right, mGuide.bottom, mGuide.right - tickLength, mGuide.bottom), mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.right, mGuide.top, mGuide.right,               mGuide.bottom), mGuidePaint); 
        */
        // left
        canvas.drawRect(guideStrokeRect(mGuide.left, mGuide.top, mGuide.left + tickLength, mGuide.top), mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.left, mGuide.bottom, mGuide.left + tickLength, mGuide.bottom),  mGuidePaint);      
        canvas.drawRect(guideStrokeRect(mGuide.left, mGuide.top + tickLength, mGuide.left, mGuide.top ), mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.left, mGuide.bottom - tickLength, mGuide.left, mGuide.bottom), mGuidePaint);
        
        // right
        canvas.drawRect(guideStrokeRect(mGuide.right, mGuide.top, mGuide.right - tickLength, mGuide.top), mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.right, mGuide.bottom, mGuide.right - tickLength, mGuide.bottom), mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.right, mGuide.top+ tickLength, mGuide.right, mGuide.top), mGuidePaint);
        canvas.drawRect(guideStrokeRect(mGuide.right, mGuide.bottom - tickLength, mGuide.right, mGuide.bottom), mGuidePaint);

        //draw scan line
        if(true){
			// Draw a red "laser scanner" line through the middle to show decoding is active
        	mGuidePaint.setColor(laserColor);
        	mGuidePaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
			int lft, top, rgt, btm;
           	lft = mGuide.left ;//+ GUIDE_STROKE_WIDTH*2/3;
           	rgt = mGuide.right;//- GUIDE_STROKE_WIDTH*2/3;
           	top = (int)(mGuide.height()*32.00/54.00)+mGuide.top - 3;
           	btm = top + 6;

           	canvas.drawRect(lft, top, rgt, btm, mGuidePaint);
			// Request another update at the animation interval, but only repaint the laser line,
			// not the entire viewfinder mask.
			postInvalidateDelayed(ANIMATION_DELAY, lft, top, rgt, btm);
        }
        
        //canvas.save();
        //draw scanInstructions text
        if (true) {
            // Draw guide text
            // Set up paint attributes
            float guideHeight = GUIDE_LINE_HEIGHT * mScale;
            float guideFontSize = GUIDE_FONT_SIZE * mScale;

            ViewUtil.setupTextPaintStyle(mGuidePaint);
            mGuidePaint.setTextAlign(Align.CENTER);
            mGuidePaint.setTextSize(guideFontSize);
            mGuidePaint.setColor(instColor);

            // Translate and rotate text
            canvas.translate(mGuide.left + mGuide.width()/2, mGuide.top + mGuide.height()/3);
           
            if (scanInstructions != null && scanInstructions != "") {
                String[] lines = scanInstructions.split("\n");
                float y = -(((guideHeight * (lines.length - 1)) - guideFontSize) / 2) - 3;

                for (int i = 0; i < lines.length; i++) {
                    canvas.drawText(lines[i] , 0, y, mGuidePaint);
                    
                    y += guideHeight;
                    
                }
                postInvalidateDelayed(ANIMATION_DELAY, mGuide.left, mGuide.top, mGuide.left + mGuide.width(), mGuide.top + mGuide.height()/2);
            }
        }
        canvas.restore();
        
        //draw supportInstructions text
        if (true) {
        	canvas.save();
            // Draw guide text
            // Set up paint attributes
            float guideHeight = GUIDE_LINE_HEIGHT * mScale;
            float guideFontSize = GUIDE_FONT_SIZE * mScale;

            ViewUtil.setupTextPaintStyle(mGuidePaint);
            mGuidePaint.setTextAlign(Align.CENTER);
            mGuidePaint.setTextSize(guideFontSize);
            mGuidePaint.setColor(supportColor);

            // Translate and rotate text
            canvas.translate(mGuide.left + mGuide.width()/2, mGuide.top + mGuide.height() - guideFontSize);
           
            if (supportInstructions != null && supportInstructions != "" && EXBankCardInfo.DISPLAY_LOGO) {
                String[] lines = supportInstructions.split("\n");
                float y = -(((guideHeight * (lines.length - 1)) - guideFontSize) / 2) - 3;

                for (int i = 0; i < lines.length; i++) {
                    canvas.drawText(lines[i] , 0, y, mGuidePaint);
                    
                    y += guideHeight;
                    
                }
                postInvalidateDelayed(ANIMATION_DELAY, mGuide.left, mGuide.top, mGuide.left + mGuide.width(), mGuide.top + mGuide.height()/2);
            }
            canvas.restore();
        }
        
        
        // draw logo
        if (mShowLogo && EXBankCardInfo.DISPLAY_LOGO) {
            canvas.save();
            canvas.translate(mLogoRect.exactCenterX(), mLogoRect.exactCenterY());
            mLogo.draw(canvas, LOGO_MAX_WIDTH * mScale, LOGO_MAX_HEIGHT * mScale);
            canvas.restore();
        }

        if (mShowTorch) {
            // draw torch
            canvas.save();
            canvas.translate(mTorchRect.exactCenterX(), mTorchRect.exactCenterY());
            mTorch.draw(canvas);
            canvas.restore();
        }
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            int action;
            action = event.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_DOWN) {
                Point p = new Point((int) event.getX(), (int) event.getY());
                Rect r = ViewUtil.rectGivenCenter(p, BUTTON_TOUCH_TOLERANCE, BUTTON_TOUCH_TOLERANCE);
                Log.d(TAG, "onTouchEvent: " + p);
                if (mShowTorch && mTorchRect != null && Rect.intersects(mTorchRect, r)) {
                    Log.d(TAG, "torch touched");
                    mScanActivityRef.get().toggleFlash();
                } else if (mLogoRect != null && Rect.intersects(mLogoRect, r)) {
                    Log.d(TAG, "logo touched");
                } else {//focus again
                    mScanActivityRef.get().triggerAutoFocus();
                }
            }
        } catch (NullPointerException e) {
            // Un-reproducible NPE reported on device without flash where flash detected and flash
            // button pressed (see https://github.com/paypal/PayPal-Android-SDK/issues/27)
            Log.d(TAG, "NullPointerException caught in onTouchEvent method");
        }
        return false;
    }

    /* create the card image with inside a rounded rect */
    public void decorateBitmap(Bitmap bitmap) {
        RectF roundedRect = new RectF(2, 2, bitmap.getWidth() - 2, bitmap.getHeight() - 2);
        float cornerRadius = bitmap.getHeight() * CORNER_RADIUS_SIZE;
        // Alpha canvas with white rounded rect
        Bitmap maskBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas maskCanvas = new Canvas(maskBitmap);
        maskCanvas.drawColor(Color.TRANSPARENT);
        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(Color.BLACK);
        maskPaint.setStyle(Paint.Style.FILL);
        maskCanvas.drawRoundRect(roundedRect, cornerRadius, cornerRadius, maskPaint);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        // Draw mask onto mBitmap
        Canvas canvas = new Canvas(bitmap);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(maskBitmap, 0, 0, paint);
        // Now re-use the above bitmap to do a shadow.
        paint.setXfermode(null);
        maskBitmap.recycle();
    }
    
    public int  getGuideColor() {
    	return guideColor;
    }
    public void setGuideColor(int color) {
    	guideColor = color;
    }
    public boolean getShowLogo()   {
    	return mShowLogo; 
    }
    public void setShowLogo(boolean bshow) {
    	mShowLogo = bshow;
    }
    public String getScanInstructions() {
    	return scanInstructions;
    }
    public void setScanInstructions(String scanInstructions) {
    	OverlayView.scanInstructions = scanInstructions;
    }    
    public boolean isAnimating() {
        return (mState != 0);
    }
    public void setCameraPreviewRect(Rect rect) {
        mCameraPreviewRect = rect;
    }
    public void setTorchOn(boolean b) {
        mTorch.setOn(b);
        invalidate();
    }
    public void setScannerAlpha(int alpha){
    	scannerAlpha = alpha;
    	scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
    }

    // for test
    public Rect getTorchRect() {
        return mTorchRect;
    }
}