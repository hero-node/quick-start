package com.exocr.exocr;

import android.app.Activity;

public class MainActivity extends Activity{
//	private static final String TAG = MainActivity.class.getSimpleName();
//	private int MY_SCAN_REQUEST_CODE = 100; // arbitrary int
//	//对于不同的识别应用，定义不同的请求码
//	public int MY_SCAN_REQUEST_CODE_BANK = 101; //银行卡识别请求码
//	public int MY_SCAN_REQUEST_CODE_ID = 102; //身份证识别请求码
//	public int MY_SCAN_REQUEST_CODE_VE = 103; //驾驶证识别请求码
//	public int MY_SCAN_REQUEST_CODE_QR = 104; //二维码识别请求码
//	//银行卡截图
//	private Bitmap BankCardImage = null;
//	private Bitmap BankFullImage = null;
//	//身份证截图
//	private Bitmap IDCardFrontFullImage = null;
//	private Bitmap IDCardBackFullImage = null;
//	private Bitmap IDCardFaceImage = null;
//	//行驶证截图
//	private Bitmap VECardFullImage = null;
//
//	public enum PhotoTypeEnum {
//        bank, id, ve;
//    }
//	public static final int PHOTO_BANK = 0x1024;
//	public static final int PHOTO_ID = 0x1025;
//	public static final int PHOTO_VE = 0x1026;
//
//	private BankPhoto bankPhoto;
//	private IDPhoto idPhoto;
//	private VEPhoto vePhoto;
//
//	private int getStatusBarHeight() {
//        Class<?> c = null;
//        Object obj = null;
//        Field field = null;
//        int x = 0, sbar = 0;
//        try {
//            c = Class.forName("com.android.internal.R$dimen");
//            obj = c.newInstance();
//            field = c.getField("status_bar_height");
//            x = Integer.parseInt(field.get(obj).toString());
//            sbar = getResources().getDimensionPixelSize(x);
//            return sbar;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }
//
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
//
//		WindowManager wm = this.getWindowManager();
//
//		int screenHeight =  wm.getDefaultDisplay().getHeight();
//		int h = getStatusBarHeight();
//		screenHeight -= h;
//
//	    ImageView barIV =  (ImageView) findViewById(R.id.barIV);
//	    LinearLayout.LayoutParams linearParamsBar =(LinearLayout.LayoutParams) barIV.getLayoutParams();
//	    linearParamsBar.height = screenHeight / 3;
//
//	    ImageView  bankIV = (ImageView) findViewById(R.id.bankCardIV);
//		LinearLayout.LayoutParams linearParamsBank =(LinearLayout.LayoutParams) bankIV.getLayoutParams();
//		linearParamsBank.height = screenHeight / 6;
//
//		ImageView  CZKIV = (ImageView) findViewById(R.id.czkCardIV);
//		LinearLayout.LayoutParams linearParamsCZK =(LinearLayout.LayoutParams) CZKIV.getLayoutParams();
//		linearParamsCZK.height = screenHeight / 6;
//
//
//		ImageView  IDIV = (ImageView) findViewById(R.id.idCardIV);
//		LinearLayout.LayoutParams linearParamsID =(LinearLayout.LayoutParams) IDIV.getLayoutParams();
//		linearParamsID.height = screenHeight / 6;
//
//		ImageView  VEIV = (ImageView) findViewById(R.id.veCardIV);
//		LinearLayout.LayoutParams linearParamsVE =(LinearLayout.LayoutParams) VEIV.getLayoutParams();
//		linearParamsVE.height = screenHeight / 6;
//
//
//		ImageView  VINIV = (ImageView) findViewById(R.id.VINCardIV);
//		LinearLayout.LayoutParams linearParamsVIN =(LinearLayout.LayoutParams) VINIV.getLayoutParams();
//		linearParamsVIN.height = screenHeight / 6;
//
//		ImageView  FaceIV = (ImageView) findViewById(R.id.FaceCardIV);
//		LinearLayout.LayoutParams linearParamsFace =(LinearLayout.LayoutParams) FaceIV.getLayoutParams();
//		linearParamsFace.height = screenHeight / 6;
//
//		ImageView  QrIV = (ImageView) findViewById(R.id.QrIV);
//		LinearLayout.LayoutParams linearParamsBtmQr =(LinearLayout.LayoutParams) QrIV.getLayoutParams();
//		linearParamsBtmQr.height = screenHeight / 6;
//
//
//		ImageView  PhotoImportIV = (ImageView) findViewById(R.id.PhotoImportIV);
//		LinearLayout.LayoutParams linearParamsPhotoImport =(LinearLayout.LayoutParams) PhotoImportIV.getLayoutParams();
//		linearParamsPhotoImport.height = screenHeight / 6;
//
//
//		ImageView  aboutIV = (ImageView) findViewById(R.id.aboutIV);
//		LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) aboutIV.getLayoutParams();
//		linearParams.height = screenHeight / 6;
//
//		aboutIV.setLayoutParams(linearParams);
//
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//	}
//
//
//	private boolean isValid(){
//		// This method is set up as an onClick handler in the layout xml
//				// e.g. android:onClick="onScanPress"
//				Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料
//				t.setToNow(); // 取得系统时间。
//				int year = t.year;
//				int month = t.month;
//				int day = t.monthDay;
//				int hour = t.hour;    // 0-23
//
//				if(year == 2015 && month == 6 && day < 28 ){
//					return true;
//				}else{
//					return false;
//				}
//	}
//	public void onBankBtnPress(View v) {
//			Intent scanIntent = new Intent(this, CardRecoActivity.class);
//			startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE_BANK);
//	}
//
//	public void onIDBtnPress(View v) {
//			Intent scanIntent = new Intent(this, exocr.idcard.IDCardEditActivity.class);
//			startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE_ID);
//	}
//
//	public void onVEBtnPress(View v) {
//			exocr.vecard.CaptureActivity.hardwareSupportCheck();
//			Intent scanIntent = new Intent(this, exocr.vecard.CaptureActivity.class);
//			startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE_VE);
//
//	}
//
//	public void onCZKBtnPress(View v) {
//	}
//	public void onQRBtnPress(View v) {
//	}
//	public void onPhotoImportPress(View v) {
//		Log.d(TAG, "图片识别");
//		AlertDialog.Builder builder = new Builder(MainActivity.this);
//
//		builder.setTitle("请选择需要识别的类型");
//        builder.setItems(getResources().getStringArray(R.array.ItemArray), new DialogInterface.OnClickListener()
//        {
//            public void onClick(DialogInterface menu, int item)
//            {
//            	// TODO
//            	Log.d(TAG, "click:"+item);
//                if (item == PhotoTypeEnum.bank.ordinal()) {
//                	Log.d(TAG, "BANK clicked");
//                	bankPhoto = new BankPhoto(MainActivity.this);
//                	bankPhoto.openPhoto();
//                } else if (item == PhotoTypeEnum.id.ordinal()) {
//                	Log.d(TAG, "ID clicked");
//                	idPhoto = new IDPhoto(MainActivity.this);
//                	idPhoto.openPhoto();
//                } else if (item == PhotoTypeEnum.ve.ordinal()) {
//                	Log.d(TAG, "VE clicked");
//                	vePhoto = new VEPhoto(MainActivity.this);
//                	vePhoto.openPhoto();
//                }
//                menu.dismiss();
//            }
//        });
//        builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
//
//            public void onClick(DialogInterface menu, int which)
//            {
//                // TODO
//            	menu.dismiss();
//
//            }
//        });
//        builder.show();
//	}
//
//	// 读取银行卡识别及最终结果并存储于结构体中
//	private void getBankCardResult(int resultCode, Intent data) {
//        if (data != null && data.hasExtra(exocr.bankcard.CardRecoActivity.BANK_RECO_RESULT)) {
//            if (resultCode == exocr.bankcard.CardRecoActivity.BANK_RETURN_RESULT) {
//                Bundle extras = data.getExtras();
//                if (extras != null) {
//                    EXBankCardInfo recoResult = extras.getParcelable(exocr.bankcard.CardRecoActivity.BANK_RECO_RESULT); // 识别结果
//                    EXBankCardInfo finalResult = extras.getParcelable(exocr.bankcard.CardRecoActivity.BANK_FINAL_RESULT); // 最终结果，可能被修改过
//                    boolean edited = extras.getBoolean(exocr.bankcard.CardRecoActivity.BANK_EDITED); // 是否修改过
//                    Log.d(TAG, "recogResult:" + recoResult.toString());
//                    Log.d(TAG, "finalResult:" + finalResult.toString());
//                    Log.d(TAG, "edited:" + edited);
//                    //获取照片
//                    BankCardImage = exocr.bankcard.CardRecoActivity.markedCardImage;
//                    BankFullImage = exocr.bankcard.CardRecoActivity.cardFullImage;
//                }
//            }
//        }
//	}
//
//	// 读取身份证识别及最终结果
//	private void getIDCardResult(int resultCode, Intent data) {
//        if (data != null && data.hasExtra(exocr.idcard.IDCardEditActivity.ID_RECO_RESULT)) {
//            if (resultCode == exocr.idcard.IDCardEditActivity.ID_RETURN_RESULT) {
//                Bundle extras = data.getExtras();
//                if (extras != null) {
//                    EXIDCardResult recoResult = extras.getParcelable(exocr.idcard.IDCardEditActivity.ID_RECO_RESULT);
//                    EXIDCardResult finalResult = extras.getParcelable(exocr.idcard.IDCardEditActivity.ID_FINAL_RESULT);
//                    boolean edited = extras.getBoolean(exocr.idcard.IDCardEditActivity.ID_EDITED);
//                    Log.d(TAG, "recogResult:" + recoResult.toString());
//                    Log.d(TAG, "finalResult:" + finalResult.toString());
//                    Log.d(TAG, "edited:" + edited);
//                    //获取照片
//                    IDCardFrontFullImage = exocr.idcard.CaptureActivity.IDCardFrontFullImage;
//                    IDCardBackFullImage = exocr.idcard.CaptureActivity.IDCardBackFullImage;
//                    IDCardFaceImage = exocr.idcard.CaptureActivity.IDCardFaceImage;
//                }
//            }
//        }
//	}
//
//	// 读取驾驶证识别及最终结果
//	private void getVECardResult(int resultCode, Intent data) {
//        if (data != null && data.hasExtra(exocr.vecard.CaptureActivity.VE_RECO_RESULT)) {
//            if (resultCode == exocr.vecard.CaptureActivity.VE_RETURN_RESULT) {
//                Bundle extras = data.getExtras();
//                if (extras != null) {
//                    EXVECardResult recoResult = extras.getParcelable(exocr.vecard.CaptureActivity.VE_RECO_RESULT);
//                    EXVECardResult finalResult = extras.getParcelable(exocr.vecard.CaptureActivity.VE_FINAL_RESULT);
//                    boolean edited = extras.getBoolean(exocr.vecard.CaptureActivity.VE_EDITED);
//                    Log.d(TAG, "recogResult:" + recoResult.toString());
//                    Log.d(TAG, "finalResult:" + finalResult.toString());
//                    Log.d(TAG, "edited:" + edited);
//                    //获取照片
//                    VECardFullImage = exocr.vecard.CaptureActivity.VECardFullImage;
//                }
//            }
//        }
//	}
//
//	// 当识别结束时，相应的activity被销毁后，此函数会被调用
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		Log.d(TAG, String.format("onActivityResult(requestCode:%d, resultCode:%d, ...", requestCode, resultCode));
//
//		// 根据不同的请求码，调用不同的处理函数，从而区分银行卡、身份证、行驶证或图片导入等识别
//		if (requestCode == MY_SCAN_REQUEST_CODE_BANK) {
//			getBankCardResult(resultCode, data);
//		} else if (requestCode == MY_SCAN_REQUEST_CODE_ID) {
//			getIDCardResult(resultCode, data);
//		} else if (requestCode == MY_SCAN_REQUEST_CODE_VE) {
//			getVECardResult(resultCode, data);
//		} else if (resultCode == RESULT_OK) {
//			switch (requestCode) {
//			case MainActivity.PHOTO_BANK:
//				Log.d(TAG, "BANK received data");
//				bankPhoto.photoRec(data);
//				bankPhoto = null;
//				break;
//			case MainActivity.PHOTO_ID:
//				Log.d(TAG, "ID received data");
//				idPhoto.photoRec(data);
//				idPhoto = null;
//				break;
//			case MainActivity.PHOTO_VE:
//				Log.d(TAG, "VE received data");
//				vePhoto.photoRec(data);
//				vePhoto = null;
//				break;
//			default:
//				break;
//			}
//		}
//	}

}
