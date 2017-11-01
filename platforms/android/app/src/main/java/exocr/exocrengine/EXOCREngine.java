package exocr.exocrengine;

import android.content.Context;
import android.graphics.Bitmap;


public final class EXOCREngine{
	private static final String tag = "EXOCREngine";
	
	public static final int mMaxStreamBuf = 4096;
	//time
	public long timestart;
	public long timeend;
	
	//Results
	public byte []bResultBuf;
	public int    nResultLen;
	
	/////////////////////////////////////////////////////////////
	// NDK STUFF
	static {
		System.loadLibrary("exocrenginec");
	}
	////////////////////////////////////////////////////////////
	public EXOCREngine(){
		bResultBuf = new byte[mMaxStreamBuf];
		nResultLen = 0;
	}
	
	//natives/////////////////////////////////////////////////////
	public static native int nativeInit(byte []dbpath);
	public static native int nativeDone();
	public static native int nativeCheckSignature(Context context);
	//////////////////////////////////////////////////////////////
	//IDCardRecognition
	public static native int nativeRecoIDCardBitmap(Bitmap bitmap, byte[]bresult, int maxsize);
	public static native Bitmap nativeRecoIDCardStillImage(Bitmap bitmap, int tryhard, int bwantimg, byte[] bresult, int maxsize, int []rets);
	public static native int nativeRecoIDCardRawdat(byte []imgdata, int width, int height, int pitch,  int imgfmt, byte []bresult, int maxsize);	
	public static native Bitmap nativeGetIDCardStdImg(byte []NV21, int width, int height, byte []bresult, int maxsize, int []rects);

	/////////////////////////////////////////////////////////////
	//VECardRecognition
	public static native int nativeRecoVECardBitmap(Bitmap bitmap, byte[]bresult, int maxsize);
	public static native Bitmap nativeRecoVECardStillImage(Bitmap bitmap, int tryhard, int bwantimg, byte[] bresult, int maxsize, int []rets);
	public static native int nativeRecoVECardRawdat(byte []imgdata, int width, int height, int pitch,  int imgfmt, byte []bresult, int maxsize);
	public static native Bitmap nativeGetVECardStdImg(byte []NV21, int width, int height, byte []bresult, int maxsize, int []rects);
}