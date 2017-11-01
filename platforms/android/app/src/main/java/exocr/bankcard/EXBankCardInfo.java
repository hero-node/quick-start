package exocr.bankcard;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.UUID;

/**
 * Describes a card.             version 2.0
 */
public final class EXBankCardInfo implements Parcelable {
	//是否展示logo
	public final static boolean DISPLAY_LOGO = true;
	//是否过滤银行
	public final static boolean FILTER_BANK = false;
	//recognition data
	public int charCount;
	public char []numbers;
	public Rect []rects;
	public String strNumbers;
	//bitmap to show
	public Bitmap bitmap;
	public Bitmap fullImage;
	//bank Name
	public String strBankName;
	
	public int nType;
	public int nRate;
	
	//time
	public long timestart;
	public long timeend;
	public float focusScore;
	
	// these should NOT be public
    String scanId;
	
	public EXBankCardInfo() {
		numbers = new char[32];
		rects   = new Rect[32];
		charCount = 0;
		bitmap = null; //bitmap result
		fullImage = null;
		strBankName = null;
		focusScore = 0;
		/////////////////////////////////////
		scanId = UUID.randomUUID().toString();
	}
	
    // parcelable
    private EXBankCardInfo(Parcel src) {
    	numbers = new char[32];
		rects   = new Rect[32];
		charCount = 0;
		bitmap = null; //bitmap result
		fullImage = null;
		strBankName = null;
		focusScore = 0;
		
    	charCount = src.readInt();
    	src.readCharArray(numbers);
    	for(int i = 0; i < charCount; ++i){
    		int left, top, right, bottom;
    		left = src.readInt();
    		top  = src.readInt();
    		right = src.readInt();
    		bottom = src.readInt();
    		rects[i] = new Rect(left, top, right, bottom);
    	}
    	strNumbers = src.readString();
    	strBankName = src.readString();
        scanId = src.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
    	dest.writeInt(charCount);
    	dest.writeCharArray(numbers);
    	for(int i = 0; i < charCount; ++i){
    		dest.writeInt(rects[i].left);
    		dest.writeInt(rects[i].top);
    		dest.writeInt(rects[i].right);
    		dest.writeInt(rects[i].bottom);
    	}
    	dest.writeString(strNumbers);
    	dest.writeString(strBankName);
        dest.writeString(scanId);
    }

    public static final Parcelable.Creator<EXBankCardInfo> CREATOR = new Parcelable.Creator<EXBankCardInfo>() {

        @Override
        public EXBankCardInfo createFromParcel(Parcel source) {
            return new EXBankCardInfo(source);
        }

        @Override
        public EXBankCardInfo[] newArray(int size) {
            return new EXBankCardInfo[size];
        }
    };
	
	/** @return raw text to show */
	public String getText() {
		long timeescape = timeend - timestart;
		String text = "CardNumber:" + strNumbers;
		text += "\nRecoTime=" + timeescape;
		return text;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return strBankName + "\n" + strNumbers;
	}
}
