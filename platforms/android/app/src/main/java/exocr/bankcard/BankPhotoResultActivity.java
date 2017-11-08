package exocr.bankcard;

import com.tiger.cash.R;

/* BankPhotoResultActivity.java
 * See the file "LICENSE.md" for the full license governing this code.
 */
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.DateKeyListener;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public final class BankPhotoResultActivity extends Activity implements TextWatcher {
	
    private static final String PADDING_DIP = "4dip";
    private static final String LABEL_LEFT_PADDING_DEFAULT = "2dip";
    private static final String LABEL_LEFT_PADDING_HOLO = "12dip";
    private static final String FIELD_HALF_GUTTER = PADDING_DIP;
    private int viewIdCounter = 80;
    private static final int editTextIdBase = 800;
    private int editTextIdCounter = editTextIdBase;

    private TextView activityTitleTextView;
    private EditText numberEdit;
    private ImageView cardView;
    private Button doneBtn;
    private Button cancelBtn;
    private EXBankCardInfo capture;
    private EXBankCardInfo recoResult;
    private EXBankCardInfo finalResult;
    private EditText bankNameET;
    private EditText unrecFinalResult;
    private boolean bRecoFailed = false;

    private boolean autoAcceptDone;
    private String labelLeftPadding;
        
    private int resultBeginId;//显示结果的EditText控件的起始ID，按Done时获取结果需要用到
    private int resultEndId;  //显示结果的EditText控件的终止ID，按Done时获取结果需要用到

    private final String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");

        Bundle extras = getIntent().getExtras();
        if (extras == null)
            throw new IllegalStateException("Didn't find any extras!");

        ActivityHelper.setActivityTheme(this, extras.getBoolean(CardRecoActivity.EXTRA_KEEP_APPLICATION_THEME));
        labelLeftPadding = ActivityHelper.holoSupported() ? LABEL_LEFT_PADDING_HOLO : LABEL_LEFT_PADDING_DEFAULT;
        super.onCreate(savedInstanceState);
        
        // hide titlebar of application
        // must be before setting the layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
                
        
        int paddingPx = ViewUtil.typedDimensionValueToPixelsInt(PADDING_DIP, this);
        RelativeLayout container = new RelativeLayout(this);
        container.setBackgroundColor(Appearance.DEFAULT_BACKGROUND_COLOR);
        
        ScrollView scrollView = new ScrollView(this);
        int scrollViewId = viewIdCounter++;
        scrollView.setId(scrollViewId);
        RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        scrollParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        container.addView(scrollView, scrollParams);

        LinearLayout wrapperLayout = new LinearLayout(this);
        wrapperLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(wrapperLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        RelativeLayout titleLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, ViewUtil.typedDimensionValueToPixelsInt("64dpi", this));
        
        titleLayout.setBackgroundColor(Appearance.TITLE_BACKGROUND_COLOR);
        
        TextView titleView = new TextView(this);
        RelativeLayout.LayoutParams titleViewParam = new RelativeLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        titleView.setGravity(Gravity.CENTER);
        titleView.setText("银行卡信息");
        titleView.setTextSize(Appearance.TEXT_SIZE_TITLE);
        titleView.setTextColor(Color.BLACK);
        titleView.setBackgroundColor(Appearance.TITLE_BACKGROUND_COLOR);    
        titleLayout.addView(titleView, titleViewParam);
        
        wrapperLayout.addView(titleLayout, titleParams); 
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        TextView hintText = new TextView(this);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        hintText.setText("请核对卡号信息，确认无误");
        hintText.setTextColor(Color.BLACK);
        hintText.setBackgroundColor(Color.TRANSPARENT);
        //mainLayout.addView(hintText, hintParams);
        ViewUtil.setPadding(hintText, "0dip", "5dip", "5dip", "5dip");
        ViewUtil.setMargins(hintText, "0dip", "0dip", "0dip", "0dip");
        
        initResult();
        
        capture = extras.getParcelable(CardRecoActivity.EXTRA_SCAN_RESULT);

        if (capture != null) {
        	if(capture.strBankName == null) {       		
        		bRecoFailed = true;
        	} else {
        		recoResult = capture;
        	}
        	
        	TextView bankNameTV = new TextView(this);
            LinearLayout.LayoutParams bankNameTVParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            bankNameTV.setText("银行名称：");
            bankNameTV.setTextSize(Appearance.TEXT_SIZE_BUTTON);
            bankNameTV.setTextColor(Color.BLACK);
            bankNameTV.setBackgroundColor(Color.TRANSPARENT);
            mainLayout.addView(bankNameTV, bankNameTVParams);
            ViewUtil.setPadding(bankNameTV, "0dip", "5dip", "5dip", "5dip");
            ViewUtil.setMargins(bankNameTV, "0dip", "0dip", "0dip", "0dip");
            
            bankNameET = new EditText(this);
            LinearLayout.LayoutParams bankNameETParams;
            if (capture.strBankName != null) {
            	bankNameETParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            	bankNameET.setText(capture.strBankName);
            	bankNameET.setBackgroundColor(Color.TRANSPARENT);
            } else {
            	bankNameETParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 100);
            	bankNameET.setGravity(Gravity.CENTER);
            }
            bankNameET.setTextSize(Appearance.TEXT_SIZE_BUTTON);
            bankNameET.setTextColor(Color.BLACK);         
            bankNameET.setSingleLine(true);
            if (capture.strBankName == null) {
            }
            mainLayout.addView(bankNameET, bankNameETParams);
            ViewUtil.setPadding(bankNameET, "0dip", "5dip", "5dip", "5dip");
            ViewUtil.setMargins(bankNameET, "0dip", "0dip", "0dip", "0dip");
                           
			if (!bRecoFailed) {
				if (capture.strNumbers != "") {
					LinearLayout resultLayout = new LinearLayout(this);
					LinearLayout.LayoutParams resultLayoutParams = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					resultLayout.setOrientation(LinearLayout.HORIZONTAL);
					resultLayout.setGravity(Gravity.CENTER_HORIZONTAL);

					resultBeginId = 0;
					resultEndId = 0;
					int Id;
					String[] arr = capture.strNumbers.split(" ");
					for (int i = 0; i < arr.length; i++) {
						if (arr[i] != "") {
							EditText result = new EditText(this);
							Id = viewIdCounter++;

							if (Id > resultBeginId && resultBeginId == 0)
								resultBeginId = Id;
							if (Id > resultEndId)
								resultEndId = Id;
							LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(0,
									LayoutParams.MATCH_PARENT);
							resultParams.weight = arr[i].length();
							result.setLayoutParams(resultParams);
							result.setId(Id);
							result.setMaxLines(1);
							result.setImeOptions(EditorInfo.IME_ACTION_DONE);
							result.setTextAppearance(getApplicationContext(), android.R.attr.textAppearanceLarge);
							result.setInputType(InputType.TYPE_CLASS_PHONE);
							result.setGravity(Gravity.CENTER);
							result.setText(arr[i]);
							// ViewUtil.setMargins(mainLayout, "10dip", "8dip", "10dip", "8dip");
							resultLayout.addView(result);
						}
					}
					mainLayout.addView(resultLayout, resultLayoutParams);
				}
			} else {
				TextView resultTV = new TextView(this);
	            LinearLayout.LayoutParams resultTVParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	            resultTV.setText("银行卡号：");
	            resultTV.setTextSize(Appearance.TEXT_SIZE_BUTTON);
	            resultTV.setTextColor(Color.BLACK);
	            resultTV.setBackgroundColor(Color.TRANSPARENT);
	            mainLayout.addView(resultTV, resultTVParams);
	            ViewUtil.setPadding(resultTV, "0dip", "5dip", "5dip", "5dip");
	            ViewUtil.setMargins(resultTV, "0dip", "0dip", "0dip", "0dip");
	            
            	LinearLayout resultLayout = new LinearLayout(this);
            	LinearLayout.LayoutParams resultLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 100);
            	resultLayout.setOrientation(LinearLayout.HORIZONTAL);
            	resultLayout.setGravity(Gravity.CENTER_HORIZONTAL);           
            	int Id;
            	
            	unrecFinalResult = new EditText(this); 
            	Id = viewIdCounter++;
            	LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            	unrecFinalResult.setLayoutParams(resultParams);
            	unrecFinalResult.setId(Id);
            	unrecFinalResult.setMaxLines(1);
            	unrecFinalResult.setImeOptions(EditorInfo.IME_ACTION_DONE);
            	unrecFinalResult.setTextAppearance(getApplicationContext(), android.R.attr.textAppearanceLarge);
            	unrecFinalResult.setInputType(InputType.TYPE_CLASS_PHONE);
            	unrecFinalResult.setGravity(Gravity.CENTER);
            	unrecFinalResult.setSingleLine(true);
    			resultLayout.addView(unrecFinalResult);
    			mainLayout.addView(resultLayout, resultLayoutParams);
            }
			
			cardView = new ImageView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            cardView.setPadding(0, 0, 0, paddingPx);
            cardParams.weight = 1;
            // static access is necessary, else we see weird crashes on some devices.
            if (BankPhoto.markedCardImage != null) {
            	int w = this.getBaseContext().getResources().getDisplayMetrics().widthPixels;
            	int h = (int) ((int)w * 0.63084);
            	cardView.setAdjustViewBounds(true);
            	cardView.setMaxWidth(w);
            	cardView.setMaxHeight(h);
            	cardView.setImageBitmap(BankPhoto.markedCardImage);
            }
            mainLayout.addView(cardView, cardParams);
            ViewUtil.setMargins(cardView, null, Appearance.VERTICAL_SPACING, null, Appearance.VERTICAL_SPACING);
        }
        
        wrapperLayout.addView(mainLayout, mainParams);
        //hittext, card image and text margin
        ViewUtil.setMargins(mainLayout, Appearance.CONTAINER_MARGIN_HORIZONTAL,
                Appearance.CONTAINER_MARGIN_VERTICAL, Appearance.CONTAINER_MARGIN_HORIZONTAL,
                Appearance.CONTAINER_MARGIN_VERTICAL);

        ///////////////////////////////////////////////////////////////
        //Buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setId(viewIdCounter++);
        RelativeLayout.LayoutParams buttonLayoutParam = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        buttonLayoutParam.addRule(RelativeLayout.BELOW, scrollViewId);
        //buttonLayout.setPadding(0, paddingPx, 0, 0);
        buttonLayout.setBackgroundColor(Color.TRANSPARENT);

        doneBtn = new Button(this);
        LinearLayout.LayoutParams doneParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);

        doneBtn.setText("确定");
       
        doneBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                completed();
            }
        });
        //doneBtn.setEnabled(false);
        buttonLayout.addView(doneBtn, doneParam);
        ViewUtil.styleAsButton(doneBtn, true, this);
        ViewUtil.setPadding(doneBtn, "5dip", null, "5dip", null);
        ViewUtil.setMargins(doneBtn, "8dip", "8dip", "4dip", "8dip");
        doneBtn.setTextSize(Appearance.TEXT_SIZE_MEDIUM_BUTTON);
 /*       
        Button cancelBtn = new Button(this);
        LinearLayout.LayoutParams cancelParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
        cancelBtn.setText("取消");
        cancelBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataEntryActivity.this.setResult(CardRecoActivity.RESULT_ENTRY_CANCELED);
                finish();
            }
        });

        buttonLayout.addView(cancelBtn, cancelParam);
        ViewUtil.styleAsButton(cancelBtn, false, this);
        ViewUtil.setPadding(cancelBtn, "5dip", null, "5dip", null);
        ViewUtil.setMargins(cancelBtn, "4dip", "8dip", "8dip", "8dip");
        cancelBtn.setTextSize(Appearance.TEXT_SIZE_MEDIUM_BUTTON);
  */     
        container.addView(buttonLayout, buttonLayoutParam);
        //ActivityHelper.addActionBarIfSupported(this); 
        ////////////////////////////////////////////////////////////////////////////////
        setContentView(container);
        Drawable icon = null;    
    }
    
    private void initResult() {
		recoResult = new EXBankCardInfo();
		finalResult = new EXBankCardInfo();

		recoResult.strBankName = "";
		recoResult.strNumbers = "";    
	}
    
    private String getConfirmResult() {
    	String result = "";
    	//根据resultBeginId和resultEndId获取结果
    	if(resultBeginId > 0 && resultEndId >= resultBeginId){
    		for (int i = resultBeginId; i <= resultEndId; i++){
    			EditText editText = (EditText)findViewById(i);
    			if(i > resultBeginId)
    				result += " ";
    			if(editText.length() > 0 )
    				result += editText.getText();
    		}
    	}
    	
    	return result;
    }

    private void getFinalResult() {
    	finalResult.strBankName = bankNameET.getText().toString();
    	if (bRecoFailed) {
    		finalResult.strNumbers = unrecFinalResult.getText().toString();
    	} else {
    		finalResult.strNumbers = getConfirmResult();
    	}
    }
    
    private boolean isEdited() {
		if (finalResult.strBankName.equals(recoResult.strBankName)  && finalResult.strNumbers.equals(recoResult.strNumbers)) {
			return false;
		}	
		return true;
	}
    
    private void completed() {
    	getFinalResult();
    	
    	Intent intent = new Intent(BankPhotoResultActivity.this, com.exocr.exocr.MainActivity.class);
		intent.putExtra(CardRecoActivity.BANK_RECO_RESULT, recoResult);
		intent.putExtra(CardRecoActivity.BANK_FINAL_RESULT, finalResult);
		this.setResult(CardRecoActivity.BANK_RETURN_RESULT, intent);
		
		if(isEdited()) {
			intent.putExtra(CardRecoActivity.BANK_EDITED, true);
		} else {
			intent.putExtra(CardRecoActivity.BANK_EDITED, false);
		}
		
		recoResult = null;
	    finalResult = null;
		
		this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        validateAndEnableDoneButtonIfValid();
         if (numberEdit != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        Log.i(TAG, "ready for manual entry"); // used by tests. don't delete.
    }

    private EditText advanceToNextEmptyField() {
        int viewId = editTextIdBase;
        EditText et;
        while ((et = (EditText) findViewById(viewId++)) != null) {
            if (et.getText().length() == 0) {
                if (et.requestFocus())
                    return et;
            }
        }
        // all fields have content
        return null;
    }

    private void validateAndEnableDoneButtonIfValid() {
        //doneBtn.setEnabled(numberValidator.isValid());
        //if (autoAcceptDone && numberValidator.isValid()) {
        //    completed();
        //}
    }

    @Override
    public void afterTextChanged(Editable et) {
    	/*
        if (numberEdit != null && et == numberEdit.getText()) {
            if (numberValidator.hasFullLength()) {
                if (!numberValidator.isValid())
                    numberEdit.setTextColor(Appearance.TEXT_COLOR_ERROR);
                else
                    advanceToNextEmptyField();
            } else
                numberEdit.setTextColor(Appearance.TEXT_COLOR_EDIT_TEXT);
        }

        this.validateAndEnableDoneButtonIfValid();
        */
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // leave empty
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // leave empty

    }
}
