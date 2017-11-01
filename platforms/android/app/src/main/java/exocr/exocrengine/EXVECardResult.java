package exocr.exocrengine;

import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public final class EXVECardResult  implements Parcelable {
	public final static boolean DISPLAY_LOGO = true;
	
	public String imgtype;
	public int nColorType;   //1 color, 0 gray
	public Bitmap stdCardIm = null;
	
	public String szPlateNo;				//号牌号码
	public String szVehicleType;			//车辆类型
	public String szOwner;				//所有人
	public String szAddress;			//住址
	public String szModel;				//品牌型号
	public String szUseCharacter;		//使用性质
	public String szEngineNo;			//发动机号
	public String szVIN;					//车辆识别代码
	public String szRegisterDate;		//注册日期
	public String szIssueDate;			//发证日期
	//////////////////////////////////////////////////////////////////////////
	//以下矩形是相对于整个stdimage的坐标系
	public Rect rtPlateNo;
	public Rect rtVehicleType;
	public Rect rtOwner;
	public Rect rtAddress;
	public Rect rtModel;
	public Rect rtUseCharacter;
	public Rect rtEngineNo;
	public Rect rtVIN;
	public Rect rtRegisterDate;
	public Rect rtIssueDate;
	
	public EXVECardResult() {
		imgtype = "Preview";
	}
	
	 // parcelable
    private EXVECardResult(Parcel src) {
    	szPlateNo = src.readString();
    	szVehicleType = src.readString();
    	szOwner = src.readString();
    	szAddress = src.readString();
    	szModel = src.readString();
    	szUseCharacter = src.readString();
    	szEngineNo = src.readString();
    	szVIN = src.readString();
    	szRegisterDate = src.readString();
    	szIssueDate = src.readString();
    }
    
    
	////////////////////////////////////////////////////////////
	/** decode from stream
	 *  return the len of decoded data int the buf */
	public static EXVECardResult decode(byte []bResultBuf, int reslen) {
		byte code;
		int i, j, rdcount;
		String content = null;
		
		EXVECardResult vecard = new EXVECardResult();		
		////////////////////////////////////////////////////////////
		//type
		rdcount = 0;
		while(rdcount < reslen){
			code = bResultBuf[rdcount++];
			i = 0;
			j = rdcount;
			while(rdcount < reslen){
				i++; rdcount++;
				if(bResultBuf[rdcount] == 0x20) break;
			}
			try {
				content = new String(bResultBuf, j, i, "GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if       (code == 0x31){ vecard.szPlateNo = content;//号牌号码
			}else if (code == 0x32){ vecard.szVehicleType = content;//车辆类型
			}else if (code == 0x33){ vecard.szOwner = content;//所有人
			}else if (code == 0x34){ vecard.szAddress = content;//住址
			}else if (code == 0x35){ vecard.szModel = content;//品牌型号
			}else if (code == 0x36){ vecard.szUseCharacter = content;//使用性质
			}else if (code == 0x37){ vecard.szEngineNo = content;//发动机号
			}else if (code == 0x38){ vecard.szVIN = content;//车辆识别代码
			}else if (code == 0x39){ vecard.szRegisterDate = content;//注册日期
			}else if (code == 0x3a){ vecard.szIssueDate = content;//发证日期
			}
			rdcount++;
		}
		//is it correct, check it!
		return vecard;
	}

	//rects存放各个块的矩形，4个一组，这么做是为了将JNI的接口简单化
	// [0, 1, 2, 3]  号牌号码
	// [4, 5, 6, 7]	 车辆类型
	// [8, 9, 10,11] 所有人
	// [12,13,14,15] 住址
	// [16,17,18,19] 品牌型号
	// [20,21,22,23] 使用性质
	// [24,25,26,27] 发动机号
	// [28,29,30,31] 车辆识别代码
	// [32,33,34,35] 注册日期
	// [36,37,38,39] 发证日期
	public void setRects( int []rects)
	{
		rtPlateNo 	 	= new Rect(rects[ 0], rects[ 1], rects[ 2], rects[ 3]);
		rtVehicleType	= new Rect(rects[ 4], rects[ 5], rects[ 6], rects[ 7]);
		rtOwner			= new Rect(rects[ 8], rects[ 9], rects[10], rects[11]);
		rtAddress		= new Rect(rects[12], rects[13], rects[14], rects[15]);
		rtModel			= new Rect(rects[16], rects[17], rects[18], rects[19]);
		rtUseCharacter	= new Rect(rects[20], rects[21], rects[22], rects[23]);
		rtEngineNo		= new Rect(rects[24], rects[25], rects[26], rects[27]);
		rtVIN			= new Rect(rects[28], rects[29], rects[30], rects[31]);
		rtRegisterDate	= new Rect(rects[32], rects[33], rects[34], rects[35]);
		rtIssueDate		= new Rect(rects[36], rects[37], rects[38], rects[39]);
	}
	
	public void SetViewType(String viewtype) {
		this.imgtype = viewtype;
	}
	
	public void SetColorType(int aColorType) {
		nColorType = aColorType;
	}
	
	public void SetBitmap(Bitmap imcard) {
		if(stdCardIm != null) stdCardIm.recycle();
		stdCardIm = imcard;
	}
	
	public Bitmap GetPlateNoBitmap(){
		if(stdCardIm == null) return null;
		Bitmap bmIDNum = Bitmap.createBitmap(stdCardIm, rtPlateNo.left, rtPlateNo.top, rtPlateNo.width(), rtPlateNo.height());
		return bmIDNum;
	}
	public Bitmap GetOwnerBitmap(){
		if(stdCardIm == null) return null;
		Bitmap bmIDNum = Bitmap.createBitmap(stdCardIm, rtOwner.left, rtOwner.top, rtOwner.width(), rtOwner.height());
		return bmIDNum;
	}

	/** @return raw text to show */
	public String getText() {
		String text = "\nVeiwType = " + imgtype;
		if(nColorType == 1){
			text += "  类型:  彩色";
		}else{
			text += "  类型:  扫描";
		}
		text += "\n号牌号码:" + szPlateNo;
	    text += "\n车辆类型:" + szVehicleType;
	    text += "\n所有人:" + szOwner;
	    text += "\n住址:" + szAddress;
	    text += "\n品牌型号:" + szModel;
	    text += "\n使用性质:" + szUseCharacter;
	    text += "\n发动机号:" + szEngineNo;
	    text += "\n车辆识别代码:" + szVIN;
	    text += "\n注册日期:" + szRegisterDate;
	    text += "\n发证日期:" + szIssueDate;
		return text;
	}
	
    public static final Parcelable.Creator<EXVECardResult> CREATOR = new Parcelable.Creator<EXVECardResult>() {

        @Override
        public EXVECardResult createFromParcel(Parcel source) {
            return new EXVECardResult(source);
        }

        @Override
        public EXVECardResult[] newArray(int size) {
            return new EXVECardResult[size];
        }
    };
    
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		/*
		 	public String szPlateNo;				//号牌号码
	public String szVehicleType;			//车辆类型
	public String szOwner;				//所有人
	public String szAddress;			//住址
	public String szModel;				//品牌型号
	public String szUseCharacter;		//使用性质
	public String szEngineNo;			//发动机号
	public String szVIN;					//车辆识别代码
	public String szRegisterDate;		//注册日期
	public String szIssueDate;			//发证日期
		 */
		
		arg0.writeString(szPlateNo);
		arg0.writeString(szVehicleType);
		arg0.writeString(szOwner);
		arg0.writeString(szAddress);
		arg0.writeString(szModel);
		arg0.writeString(szUseCharacter);
		arg0.writeString(szEngineNo);
		arg0.writeString(szVIN);
		arg0.writeString(szRegisterDate);
		arg0.writeString(szIssueDate);
		
		
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return szPlateNo + "\n" + szVehicleType + "\n" + szOwner + "\n" + szAddress + "\n" 
				+ szModel + "\n" + szUseCharacter + "\n" + szEngineNo + "\n" + szVIN + "\n" 
				 + szRegisterDate + "\n" + szIssueDate;
	}
}
