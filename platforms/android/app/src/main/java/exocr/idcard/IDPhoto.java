package exocr.idcard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.exocr.exocr.MainActivity;
import com.hero.HeroActivity;
import com.hero.HeroFragmentActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import exocr.exocrengine.EXIDCardResult;
import exocr.exocrengine.EXOCREngine;

public class IDPhoto {
	private static final String TAG = IDPhoto.class.getSimpleName();
	private HeroFragmentActivity mActivity;
	private EXIDCardResult mCardInfo;
	private byte dbpath[];
	static Bitmap markedCardImage = null;
	private boolean bSucceed;
	
	private ProgressDialog pd;

	
	/** Construction */
	public IDPhoto(HeroFragmentActivity mainActivity) {
		mActivity = mainActivity;
		EXOCREngine.nativeCheckSignature(mActivity.getApplicationContext());
		String path = mActivity.getApplicationContext().getFilesDir().getAbsolutePath();
		//InitDict(RESOURCEFILEPATH);
	    InitDict(path);
	}
    
	// 定义Handler对象
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pd.dismiss();
			if (bSucceed) {
				if (mCardInfo != null) {
					Intent intent = new Intent(mActivity, IDPhotoResultActivity.class);
					intent.putExtra(CaptureActivity.EXTRA_SCAN_RESULT, mCardInfo);
					mCardInfo = null;
					intent.putExtras(mActivity.getIntent()); // passing on anyreceived params (such as isCvv and language)
					intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY
							| Intent.FLAG_ACTIVITY_NO_ANIMATION);
//					mActivity.startActivityForResult(intent, mActivity.MY_SCAN_REQUEST_CODE_ID);
				}
			} else {
				AlertDialog alertDialog = new AlertDialog.Builder(mActivity).setTitle("提示")
						.setMessage("无法识别该图片，请手动输入身份证信息")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (mCardInfo != null) {
							Intent intent = new Intent(mActivity, IDPhotoResultActivity.class);
							intent.putExtra(CaptureActivity.EXTRA_SCAN_RESULT, mCardInfo);
							mCardInfo = null;
							intent.putExtras(mActivity.getIntent()); 
							intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY
									| Intent.FLAG_ACTIVITY_NO_ANIMATION);
							mActivity.startActivityForResult(intent, 0);
						}
					}
				}).create();
				alertDialog.show();
			}
		}
	};

	public boolean InitDict(String dictpath)
	{
		dbpath = new byte[256];
		//if the dict not exist, copy from the assets
		if(CheckExist(dictpath+"/zocr0.lib") == false ){
			File destDir = new File(dictpath);
			if (!destDir.exists()) { destDir.mkdirs(); }
			  
			boolean a = copyFile("zocr0.lib", dictpath+"/zocr0.lib");
			if (a == false){
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle("exidcard dict Copy ERROR!\n");
				builder.setMessage(dictpath+" can not be found!");
				builder.setCancelable(true);
				builder.create().show();
				return false;
			}
		}
				
		String filepath = dictpath;
		
		//string to byte
		for (int i = 0; i < filepath.length(); i++)
			dbpath[i] = (byte)filepath.charAt(i);
		dbpath[filepath.length()] = 0;
		
		int nres = EXOCREngine.nativeInit(dbpath);
		
		if (nres < 0){
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle("exidcard dict Init ERROR!\n");
			builder.setMessage(filepath+" can not be found!");
			builder.setCancelable(true);
			builder.create().show();
			return false;
		}else{
			//just test
			//ExTranslator.nativeExTran(imgdata, width, height, pixebyte, pitch, flft, ftop, frgt, fbtm, result, maxsize)
		}
		
		//sign ocr sdk
		EXOCREngine.nativeCheckSignature(mActivity.getApplicationContext());
		
		return true;
	}
	
	public boolean copyFile(String from, String to) {
		// 例：from:890.salid;
		// to:/mnt/sdcard/to/890.salid
		boolean ret = false;
		try {
			int bytesum = 0;
			int byteread = 0;

			InputStream inStream = mActivity.getResources().getAssets().open(from);// 将assets中的内容以流的形式展示出来
			File file = new File(to);
			OutputStream fs = new FileOutputStream(file);// to为要写入sdcard中的文件名称
			byte[] buffer = new byte[1024];
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread;
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
			fs.close();
			ret = true;

		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	// check one file
	public boolean CheckExist(String filepath) {
		int i;
		File file = new File(filepath);
		if (file.exists()) {
			return true;
		}
		return false;
	}
	
	public void openPhoto() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
        /* 开启Pictures画面Type设定为image */  
        intent.setType("image/*");  
        /* 取得相片后返回 */  
        mActivity.startActivityForResult(intent, 0);
	}
	
	private void _photoRec(Bitmap bitmap) {
		byte []result = new byte[4096];
		int []rets = new int[16];
		int ret = 0;
		
		//recgonise stillImage
		Bitmap cardim = EXOCREngine.nativeRecoIDCardStillImage(bitmap, 0, 1, result, 4096, rets);
		Log.i("nativeRecoStillImage", "return="+rets[0]);
		
		ret = rets[0];
		if(ret > 0){			
			mCardInfo = EXIDCardResult.decode(result, ret);
			mCardInfo.SetBitmap(cardim);
			bSucceed = true;
		} else {
			bSucceed = false;
			mCardInfo = new EXIDCardResult();
			if (markedCardImage != null && !markedCardImage.isRecycled()) {
				markedCardImage.recycle();
			}	
			markedCardImage = bitmap;
			return;
		}
		
		if (bSucceed) {
			if (markedCardImage != null && !markedCardImage.isRecycled()) {
				markedCardImage.recycle();
			}
			markedCardImage = mCardInfo.stdCardIm;
		}
//		if (cardim.isRecycled()) {
//			cardim.recycle();
//		}	
	}
	
	public void photoRec(Intent data) {
		Uri uri = data.getData();  
        Log.d(TAG, uri.toString());  
        ContentResolver cr = mActivity.getContentResolver();  
		try {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			final Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null, opt);
			if (bitmap == null) {
				return;
			}
			pd = ProgressDialog.show(mActivity, null, "正在识别，请稍后");
			/* 解析bitmap, 生成cardInfo */
			new Thread() {
				public void run() {
					// 识别图片
					_photoRec(bitmap);
//					if (!bitmap.isRecycled()) {
//						bitmap.recycle();
//					}
					EXOCREngine.nativeDone();
					// 更新UI
					mHandler.sendEmptyMessage(0);
				}
			}.start();
		} catch (FileNotFoundException e) {
			Log.e("Exception", e.getMessage(), e);
		}
	}       
}
