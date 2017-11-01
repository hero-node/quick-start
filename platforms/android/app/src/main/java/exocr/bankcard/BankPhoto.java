package exocr.bankcard;

import java.io.FileNotFoundException;

import com.exocr.exocr.MainActivity;

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

public class BankPhoto {
	private static final String TAG = BankPhoto.class.getSimpleName();
	private MainActivity mActivity;
	private EXBankCardInfo mCardInfo;
	static Bitmap markedCardImage = null;
	private boolean bSucceed;
	
	private ProgressDialog pd;
	
	/** Construction */
	public BankPhoto(MainActivity mainActivity) {
		mActivity = mainActivity;
		EXBankCardReco.nativeCheckSignature(mActivity.getApplicationContext());
		mCardInfo = new EXBankCardInfo();
	}
	
	//定义Handler对象
	private Handler mHandler =new Handler(){
	   @Override
	   public void handleMessage(Message msg){
	      super.handleMessage(msg);
	      pd.dismiss();
	      if(bSucceed) {
	    	  if (mCardInfo != null) {
	    		  Intent intent = new Intent(mActivity, BankPhotoResultActivity.class);			  
	    		  intent.putExtra(CardRecoActivity.EXTRA_SCAN_RESULT, mCardInfo);
	    		  mCardInfo = null;			
	    		  intent.putExtras(mActivity.getIntent()); // passing on any received params (such as isCvv and language)
	    		  intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
	    				  | Intent.FLAG_ACTIVITY_NO_HISTORY
	    				  | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//	    		  mActivity.startActivityForResult(intent, mActivity.MY_SCAN_REQUEST_CODE_BANK);
	    	  }
	      } else {
	    	  AlertDialog alertDialog = new AlertDialog.Builder(mActivity). 
	                  setTitle("提示"). 
	                  setMessage("无法识别该图片，请手动输入银行卡信息").
	                  setPositiveButton("确定", new DialogInterface.OnClickListener() {  
	                      @Override 
	                      public void onClick(DialogInterface dialog, int which) { 
	                          // TODO Auto-generated method stub
	                    	  if (mCardInfo != null) {
	            	    		  Intent intent = new Intent(mActivity, BankPhotoResultActivity.class);			  
	            	    		  intent.putExtra(CardRecoActivity.EXTRA_SCAN_RESULT, mCardInfo);
	            	    		  mCardInfo = null;			
	            	    		  intent.putExtras(mActivity.getIntent()); // passing on any received params (such as isCvv and language)
	            	    		  intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
	            	    				  | Intent.FLAG_ACTIVITY_NO_HISTORY
	            	    				  | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//	            	    		  mActivity.startActivityForResult(intent, mActivity.MY_SCAN_REQUEST_CODE_BANK);
	            	    	  }
	                      } 
	                  }). 
	                  create(); 
	          alertDialog.show(); 
	      }
	   }
	};
	
	public void openPhoto() {		
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
        /* 开启Pictures画面Type设定为image */  
        intent.setType("image/*");  
        /* 取得相片后返回 */  
//        mActivity.startActivityForResult(intent, MainActivity.PHOTO_BANK);
	}
	
	private void _photoRec(Bitmap bitmap) {
		byte []result = new byte[4096];
		int []rets = new int[16];
		int ret = 0;
		
		//recgonise stillImage
		Bitmap cardim = EXBankCardReco.nativeRecoStillImage(bitmap, 1, 1, result, 4096, rets);
		Log.i("nativeRecoStillImage", "return="+rets[0]);
		
		ret = rets[0];
		if(ret > 0){			
			bSucceed = EXBankCardReco.DecodeResult(result, ret, mCardInfo);
			mCardInfo.bitmap = cardim;
		} else {
			bSucceed = false;
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
			markedCardImage = mCardInfo.bitmap;
		}
//		if (!cardim.isRecycled()) {
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
            if(bitmap == null) {
            	return;
            }
            pd = ProgressDialog.show(mActivity, null, "正在识别，请稍后");
            /*解析bitmap, 生成cardInfo*/
			new Thread() {
				public void run() {
					// 识别图片
					_photoRec(bitmap);
//					if (!bitmap.isRecycled()) {
//						bitmap.recycle();
//					}
					// 更新UI
					mHandler.sendEmptyMessage(0);
				}
			}.start();                    
        } catch (FileNotFoundException e) {  
            Log.e("Exception", e.getMessage(),e);  
        } 
	}
}
