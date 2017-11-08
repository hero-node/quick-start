package exocr.bankcard;

/* CardIOActivity.java
 * See the file "LICENSE.md" for the full license governing this code.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;

import com.tiger.cash.R;


public final class CardRecoActivity extends Activity {
	public static final String EXTRA_NO_CAMERA = "exocr.bankcard.noCamera";
	public static final String EXTRA_SCAN_RESULT = "exocr.bankcard.scanResult";
	public static final String EXTRA_GUIDE_COLOR = "exocr.bankcard.guideColor";
	public static final String EXTRA_SCAN_INSTRUCTIONS = "exocr.bankcard.scanInstructions";
	public static final String EXTRA_CAPTURED_CARD_IMAGE = "exocr.bankcard.capturedCardImage";
	public static final String EXTRA_RETURN_CARD_IMAGE = "exocr.bankcard.returnCardImage";
	public static final String EXTRA_SCAN_OVERLAY_LAYOUT_ID = "exocr.bankcard.scanOverlayLayoutId";
	public static final String EXTRA_KEEP_APPLICATION_THEME = "exocr.bankcard.keepApplicationTheme";

	public static final String BANK_RECO_RESULT = "exocr.bankcard.recoResult";
	public static final String BANK_FINAL_RESULT = "exocr.bankcard.finalResult";
	public static final String BANK_EDITED = "exocr.bankcard.edited";
	public static final int BANK_RETURN_RESULT = 150;
	
	private static int lastResult = 0xca8d10; // arbitrary. chosen to be well
												// above
												// Activity.RESULT_FIRST_USER.
	public static final int RESULT_CARD_INFO = lastResult++;
	public static final int RESULT_ENTRY_CANCELED = lastResult++;
	public static final int RESULT_SCAN_NOT_AVAILABLE = lastResult++;
	public static final int RESULT_SCAN_SUPPRESSED = lastResult++;
	public static final int RESULT_CONFIRMATION_SUPPRESSED = lastResult++;

	private static final String TAG = CardRecoActivity.class.getSimpleName();

	private static final int FRAME_ID = 1;
	private static final int UIBAR_ID = 2;
	private static final long[] VIBRATE_PATTERN = { 0, 70, 10, 40 };
	private static final int TOAST_OFFSET_Y = -75;

	private static int uniqueOMatic = 10;
	private static final int REQUEST_DATA_ENTRY = uniqueOMatic++;
	
	private boolean bCamera;
	private PopupWindow popupWindow;
	private static final int MSG_POPUP = 1001;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MSG_POPUP) {
				//UI更新
				//竖屏
//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				// 一个自定义的布局，作为显示的内容
				View contentView = CardRecoActivity.this.getLayoutInflater().inflate(R.layout.popupview, null);
				// 设置按钮的点击事件
				Button button = (Button) contentView.findViewById(R.id.okButton);
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						popupWindow.dismiss();
						Intent intent = new Intent(CardRecoActivity.this, com.exocr.exocr.MainActivity.class);
						CardRecoActivity.this.setResult(REQUEST_DATA_ENTRY, intent);  
						finish();
					}
				});

				popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
				popupWindow.setTouchable(true);
				//是否阻塞
//				popupWindow.setBackgroundDrawable(new BitmapDrawable());
				// 设置好参数之后再show
				popupWindow.showAtLocation(CardRecoActivity.this.mOverlay, Gravity.CENTER, 0, 0);
			}
		}
	};

	private OverlayView mOverlay;
	// TODO: the preview is accessed by the scanner. Not the best practice.
	Preview mPreview;

	private EXBankCardInfo mCardInfo;
	private Rect mGuideFrame;

	private FrameLayout mMainLayout;
	private CardScanner mCardScanner;

	/**
	 * Static variable for the decorated card image. This is ugly, but works.
	 * Parceling and unparceling card image data to pass to the next
	 * {@link android.app.Activity} does not work because the image data is too
	 * big and causes a somewhat misleading exception. Compressing the image
	 * data yields a reduction to 30% of the original size, but still gives the
	 * same exception. An alternative would be to persist the image data in a
	 * file. That seems like a pretty horrible idea, as we would be persisting
	 * very sensitive data on the device.
	 */
	public static Bitmap markedCardImage = null;
	public static Bitmap cardFullImage = null;

	// ///////////////////////////////////////////////////////////////////
	static public String PRODUCT_VERSION = "2.0.1.1";
	static public String PRODUCT_NAME = "EXBankCardRec";
	static public String PRODUCT_SIG = "EXOCR_BANKCARD_SIG_20150327";

	// ------------------------------------------------------------------------
	// ACTIVITY LIFECYCLE
	// ------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate() ================================================================");
		super.onCreate(savedInstanceState);
		final Intent clientData = this.getIntent();
	
		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//检测摄像头权限
		bCamera = hardwareSupportCheck();	
		if (bCamera) {
			Log.i("DEBUG_TIME1", "CardRecoActivity_onCreate=" + System.currentTimeMillis());
			// hardwareSupportCheck();
			Log.i("DEBUG_TIME1", "CardRecoActivity_onCreate=" + System.currentTimeMillis());
			// get settings
			try {
				mGuideFrame = new Rect();
				mCardScanner = new CardScanner(this);
				mCardScanner.prepareScanner();
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				setPreviewLayout();
			} catch (Exception e) {
				handleGeneralExceptionError(e);
			}

			Log.i("DEBUG_TIME", "CardRecoActivity_onCreate=" + System.currentTimeMillis());
		} else {	//摄像头权限受限
			mOverlay = new OverlayView(this, null, false);
			Runnable runnable=new Runnable(){
				   @Override
				   public void run() {
				    // TODO Auto-generated method stub
					// 子线程发送信息
						Message msg = mHandler.obtainMessage(MSG_POPUP);
						msg.sendToTarget();
				   } 
				};
			mHandler.postDelayed(runnable, 100);		
		}
	}
    public static boolean hardwareSupportCheck() {
		// Camera needs to open
		Camera c = null;
		boolean ret = true;
		try {
			c = Camera.open();
		} catch (RuntimeException e) {
			// throw new RuntimeException();
			ret = false;
		}
		if (c == null) { // 没有背摄像头
			return false;
		}
		if (ret) {
			c.release();
			c = null;
		}
		return ret;
    }

	private void handleGeneralExceptionError(Exception e) {
		String localizedError = "ERROR_CAMERA_UNEXPECTED_FAIL";

		Toast toast = Toast.makeText(this, localizedError, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, TOAST_OFFSET_Y);
		toast.show();
	}

	/**
	 * Suspend/resume camera preview as part of the {@link android.app.Activity}
	 * life cycle (side note: we reuse the same buffer for preview callbacks to
	 * greatly reduce the amount of required GC).
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (bCamera) {
			Log.i(TAG, "onResume() ----------------------------------------------------------");

			Log.i("DEBUG_TIME", "CardRecoActivity_onResume1=" + System.currentTimeMillis());
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			Log.i("DEBUG_TIME", "CardRecoActivity_onResume2=" + System.currentTimeMillis());
			if (!restartPreview()) {
				Log.e(TAG, "Could not connect to camera.");
				showErrorMessage("ERROR_CAMERA_UNEXPECTED_FAIL");
				nextActivity();
			} else {
				// Turn flash off
				setFlashOn(false);
			}
			Log.i("DEBUG_TIME", "CardRecoActivity_onResume3=" + System.currentTimeMillis());
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause() xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		super.onPause();
		setFlashOn(false);
		if (mCardScanner != null) {
			mCardScanner.pauseScanning();
		}
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()");
		mOverlay = null;

		if (mCardScanner != null) {
			mCardScanner.endScanning();
			mCardScanner = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, String.format( "onActivityResult(requestCode:%d, resultCode:%d, ...",
				requestCode, resultCode));

		Log.i("DEBUG_TIME", "CardRecoActivity_onActivityResult="+System.currentTimeMillis());
		if (resultCode == BANK_RETURN_RESULT) {
			this.setResult(resultCode, data);
			finish();
		}
		if (resultCode == RESULT_CARD_INFO || resultCode == RESULT_ENTRY_CANCELED) {
			if (data != null && data.hasExtra(EXTRA_SCAN_RESULT)) {
				Log.v(TAG, "data entry result: " + data.getParcelableExtra(EXTRA_SCAN_RESULT));
			}
			setResultAndFinish(resultCode, data);
		}else{
			mOverlay.setScannerAlpha(4);
		}
	}

	/**
	 * This {@link android.app.Activity} overrides back button handling to
	 * handle back presses properly given the various states this
	 * {@link android.app.Activity} can be in.
	 * 
	 * This method is called by Android, never directly by application code.
	 */
	@Override
	public void onBackPressed() {
		Log.d(TAG, "CardRecoActivity.onBackPressed()");

		if (mOverlay.isAnimating()) {
			try {
				restartPreview();
			} catch (RuntimeException re) {
				Log.w(TAG, "*** could not return to preview: " + re);
			}
		} else if (mCardScanner != null) {
			super.onBackPressed();
		}
	}

	// ------------------------------------------------------------------------
	// STATIC METHODS
	// ------------------------------------------------------------------------
	/**
	 * Please include the return value of this method in any support requests.
	 * 
	 * @return An string describing the version of the card.io library.
	 */
	public static String sdkVersion() {
		return PRODUCT_VERSION;
	}

	@SuppressWarnings("deprecation")
	public static Date sdkBuildDate() {
		return new Date();
	}
	
	public static String sdkSignature(){
		return PRODUCT_SIG;
	}

	// end static

	void InvalidateOverlapView() {
		Log.d(TAG, "mFirstPreviewFrame");

		SurfaceView sv = mPreview.getSurfaceView();
		int lft = sv.getLeft();
		int top = sv.getTop();
		int rgt = sv.getRight();
		int btm = sv.getBottom();

		Log.i("DEBUG_TIME", "CardRecoActivity_InvalidateOverlapView="+System.currentTimeMillis());
		// 基于surfaceview 计算矩形
		// get the card rect on the
		mGuideFrame = mCardScanner.getGuideFrame();
		float scalor = (1.0f * sv.getWidth()) / mCardScanner.mPreviewWidth;
		mGuideFrame.left = (int) (scalor * mGuideFrame.left);
		mGuideFrame.top = (int) (scalor * mGuideFrame.top);
		mGuideFrame.right = (int) (scalor * mGuideFrame.right);
		mGuideFrame.bottom = (int) (scalor * mGuideFrame.bottom);
		// adjust for surface view y offset
		mGuideFrame.offset(lft, top);

		if (mOverlay != null) {
			// mOverlay.setCameraPreviewRect(new Rect(sv.getLeft(), sv.getTop(),
			// sv.getRight(), sv.getBottom()));
			mOverlay.setCameraPreviewRect(new Rect(lft, top, rgt, btm));
			// set guide rect
			mOverlay.setGuideAndRotation(mGuideFrame);
		}
	}

	private void nextActivity() {
		Log.d(TAG, "CardIOActivity.nextActivity()");
		Log.i("DEBUG_TIME", "CardRecoActivity_nextActivity="+System.currentTimeMillis());
		
		new Handler().post( new Runnable(){
			@Override
			public void run() {
				Log.d(TAG, "CardIOActivity.nextActivity().post(Runnable)");
				getWindow().clearFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN);
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
				//Intent intent = new Intent(CardRecoActivity.this, DataEntryActivity.class);
				Intent intent = new Intent(CardRecoActivity.this, DataEntryActivity.class);
				
				if (mCardInfo != null) {
					intent.putExtra(EXTRA_SCAN_RESULT, mCardInfo);
					mCardInfo = null;
				}
				intent.putExtras(getIntent()); // passing on any received params (such as isCvv and language)
				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
						| Intent.FLAG_ACTIVITY_NO_HISTORY
						| Intent.FLAG_ACTIVITY_NO_ANIMATION);
				Log.i("DEBUG_TIME", "CardRecoActivity_nextActivity2="+System.currentTimeMillis());
				startActivityForResult(intent, REQUEST_DATA_ENTRY);
			}
		});
	}

	/*** Show an error message using toast. */
	private void showErrorMessage(final String msgStr) {
		Toast toast = Toast.makeText(CardRecoActivity.this, msgStr, Toast.LENGTH_LONG);
		toast.show();
	}

	private boolean restartPreview() {
		Log.d(TAG, "restartPreview()");

		mCardInfo = null;
		assert mPreview != null;
		boolean success = mCardScanner.resumeScanning(mPreview.getSurfaceHolder());
		return success;
	}

	// Called by OverlayView
	void toggleFlash() {
		setFlashOn(!mCardScanner.isFlashOn());
	}

	void setFlashOn(boolean b) {
		boolean success = (mPreview != null && mOverlay != null && mCardScanner.setFlashOn(b));
		if (success) {
			mOverlay.setTorchOn(b);
		}
	}

	void triggerAutoFocus() {
		mCardScanner.triggerAutoFocus(true);
	}

	/**
	 * Manually set up the layout for this {@link android.app.Activity}. It may
	 * be possible to use the standard xml layout mechanism instead, but to know
	 * for sure would require more work
	 */
	private void setPreviewLayout() {

		Log.i("DEBUG_TIME", "CardRecoActivity_setPreviewLayout1="+System.currentTimeMillis());
		// top level container
		mMainLayout = new FrameLayout(this);
		mMainLayout.setBackgroundColor(Color.BLACK);
		mMainLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		FrameLayout previewFrame = new FrameLayout(this);
		previewFrame.setId(FRAME_ID);

		mPreview = new Preview(this, null, mCardScanner.mPreviewWidth, mCardScanner.mPreviewHeight);
		mPreview.setLayoutParams(new FrameLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.TOP));
		previewFrame.addView(mPreview);

		mOverlay = new OverlayView(this, null, deviceSupportsTorch(this));
		mOverlay.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		if (getIntent() != null) {
			int color = getIntent().getIntExtra(EXTRA_GUIDE_COLOR, 0);
			if (color != 0) {
				// force 100% opaque guide colors.
				int alphaRemovedColor = color | 0xFF000000;
				mOverlay.setGuideColor(alphaRemovedColor);
			} else {
				// default to greeeeeen
				mOverlay.setGuideColor(Color.GREEN);
			}

			String scanInstructions = getIntent().getStringExtra(EXTRA_SCAN_INSTRUCTIONS);
			if (scanInstructions != null) {
				mOverlay.setScanInstructions(scanInstructions);
			}
		}
		Log.i("DEBUG_TIME", "CardRecoActivity_setPreviewLayout1="+System.currentTimeMillis());
		// mOverlay.setBackgroundColor(Color.GRAY);
		previewFrame.addView(mOverlay);
		RelativeLayout.LayoutParams previewParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		previewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		previewParams.addRule(RelativeLayout.ABOVE, UIBAR_ID);
		mMainLayout.addView(previewFrame, previewParams);

		// FLAG_TRANSLUCENT_NAVIGATION
		if(Build.VERSION.SDK_INT >= 19){
			getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}

		Log.i("DEBUG_TIME", "CardRecoActivity_setPreviewLayout1="+System.currentTimeMillis());
		this.setContentView(mMainLayout);
	}

	private void setResultAndFinish(final int resultCode, final Intent data) {
		
		setResult(resultCode, data);
		markedCardImage = null;
		finish();
		
	}

	// for torch test
	public Rect getTorchRect() {
		if (mOverlay == null) {
			return null;
		}
		return mOverlay.getTorchRect();
	}

	void onCardDetected(EXBankCardInfo dInfo) {
		Log.d(TAG, "processDetections");

		try {
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_PATTERN, -1);
		} catch (SecurityException e) {
			Log.e(TAG,
					"Could not activate vibration feedback. Please add <uses-permission android:name=\"android.permission.VIBRATE\" /> to your application's manifest.");
		} catch (Exception e) {
			Log.w(TAG, "Exception while attempting to vibrate: ", e);
		}
		Log.i("DEBUG_TIME", "CardRecoActivity_onCardDetected1="+System.currentTimeMillis());
		
		mCardScanner.pauseScanning();
		if (dInfo.charCount > 0) {
			mCardInfo = dInfo;
			Intent intent = new Intent();
			intent.putExtra("bankcard",dInfo);
			setResult(RESULT_OK,intent);
			finish();
		}
	}

	public static boolean deviceSupportsTorch(Context context) {
		return context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FLASH);
	}

}
