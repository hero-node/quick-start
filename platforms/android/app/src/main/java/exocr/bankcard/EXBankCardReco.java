package exocr.bankcard;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

public final class EXBankCardReco {
	private static final String tag = EXBankCardReco.class.getSimpleName();

	/////////////////////////////////////////////////////////////
	// NDK STUFF
	static {
		System.loadLibrary("exbankcardrec");
	}
	
	/////////////////////////////////////////////////////////////	
	//decode result stream
	public static boolean DecodeResult(byte []bResultBuf, int nResultLen, EXBankCardInfo cardinfo) {
		if(cardinfo == null) return false;
		//decode
		int hic, lwc;
		int i, j;
		int x, y, w, h, code;
		int nCharCount = 0;
		int nCharNum;
		int nBankNameLen = 0;
		boolean bOK = false;
		byte szBankName[] = new byte[72];
		
		//number check
		if(nResultLen < 70) return false;
		
		//////////////////////////////////////////////
		i = 0;
		hic = bResultBuf[i++]&0xFF; lwc = bResultBuf[i++]&0xFF; code = (hic<<8)+lwc; 
		cardinfo.nType = code;
		
		hic = bResultBuf[i++]&0xFF; lwc = bResultBuf[i++]&0xFF; code = (hic<<8)+lwc; 
		cardinfo.nRate = code;
		
		for(j = 0; j < 64; ++j) { szBankName[j] = bResultBuf[i++]; }
		for(j = 0; j < 64; ++j) { if(szBankName[j] == 0x00){ nBankNameLen = j; break; }}
		try {
			cardinfo.strBankName = new String(szBankName, 0, nBankNameLen, "GBK");    
		} catch (UnsupportedEncodingException e) {
            return false;
		}
		
		hic = bResultBuf[i++]&0xFF; lwc = bResultBuf[i++]&0xFF; code = (hic<<8)+lwc; 
		nCharNum = code;
		
		//decode the result info
		while(i < nResultLen-9){
			hic = bResultBuf[i++]&0xFF; lwc = bResultBuf[i++]&0xFF; code = (hic<<8)+lwc; 
			hic = bResultBuf[i++]&0xFF; lwc = bResultBuf[i++]&0xFF; x = (hic<<8)+lwc;
			hic = bResultBuf[i++]&0xFF; lwc = bResultBuf[i++]&0xFF; y = (hic<<8)+lwc;
			hic = bResultBuf[i++]&0xFF; lwc = bResultBuf[i++]&0xFF; w = (hic<<8)+lwc;
			hic = bResultBuf[i++]&0xFF; lwc = bResultBuf[i++]&0xFF; h = (hic<<8)+lwc;
			cardinfo.numbers[nCharCount] = (char)code;
			cardinfo.rects[nCharCount] = new Rect(x, y, x+w, y+h);
			nCharCount++;
		}
		cardinfo.numbers[nCharCount] = 0;
		cardinfo.charCount = nCharCount;	
		cardinfo.strNumbers = new String(cardinfo.numbers, 0, cardinfo.charCount);
		
		//is it correct, check it!
		if (cardinfo.charCount < 10	|| cardinfo.charCount > 24 || nCharCount != nCharNum) {
			bOK = false;
		} else {
			bOK = true;
		}
		return bOK;
	}
	
	//get the rect bitmap from the whole Image(NV21)
	public static boolean corpCardNumImage(byte []data, int width, int height, int format, Rect guideRect, EXBankCardInfo cardInfo){
		boolean bOk = true;
		if(cardInfo.charCount < 1){
			bOk = false;
			return bOk;
		}
		int i;
		int nAvgW = 0;
		int nAvgH = 0;
		int nCount = 0;
		Rect rect = new Rect(cardInfo.rects[0]);
		nAvgW  = cardInfo.rects[0].width();
		nAvgH  = cardInfo.rects[0].height();
		nCount = 1;
		for(i = 1; i < cardInfo.charCount; ++i){
			rect.union(cardInfo.rects[i]);
			if(cardInfo.numbers[i] != ' '){
				nAvgW += cardInfo.rects[i].width();
				nAvgH += cardInfo.rects[i].height();
				nCount ++;
			}
		}
		nAvgW /= nCount;
		nAvgH /= nCount;
		//releative to the big image
		rect.offset(guideRect.left, guideRect.top);
		
		rect.top -= nAvgH;  if(rect.top < 0) rect.top = 0;
		rect.bottom += nAvgH; if(rect.bottom >= height) rect.bottom = height-1;
		rect.left -= nAvgW; if(rect.left < 0) rect.left = 0;
		rect.right += nAvgW; if(rect.right >= width) rect.right = width-1;
		
		//rects offset to the small rect
		for(i = 0; i < cardInfo.charCount; ++i){
			cardInfo.rects[i].offset(guideRect.left-rect.left, guideRect.top-rect.top);
		}
		//
		if(cardInfo.bitmap != null) cardInfo.bitmap.recycle();
		cardInfo.bitmap = CardScanner.corpBitmap(data, width, height, format, rect);
		
		if(cardInfo.fullImage != null) cardInfo.fullImage.recycle();
		cardInfo.fullImage = CardScanner.corpBitmap(data, width, height, format, guideRect);
		
		return bOk;
	}
	
	//natives/////////////////////////////////////////////////////
	public static native int nativeInit(byte []dbpath);
	public static native int nativeDone();
	public static native int nativeRecoRawdat(byte []imgdata, int width, int height, int imgfmt, int lft, int rgt, int top, int btm, byte []bresult, int maxsize);
	public static native int nativeRecoBitmap(Bitmap bitmap, int lft, int rgt, int top, int btm, byte[]bresult, int maxsize);
	//recognize still image, the result get from the buffer, and the return image from return, and the ret code from rets
	public static native Bitmap nativeRecoStillImage(Bitmap bitmap, int tryhard, int bwantimg, byte[]bresult, int maxsize, int []rets);
	public static native float nativeFocusScore(byte []imgdata, int width, int height, int imgfmt, int lft, int rgt, int top, int btm);
	public static native int nativeCheckSignature(Context context);
	
}