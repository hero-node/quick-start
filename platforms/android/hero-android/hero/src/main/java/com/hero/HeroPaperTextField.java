package com.hero;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hero.depandency.CountDownProgressView;

import org.json.JSONException;
import org.json.JSONObject;

public class HeroPaperTextField extends RelativeLayout implements IHero, HeroTextField.FocusChangeListener, TextWatcher {
    private HeroTextField textField;

    private HeroButton rightButton;
    private HeroImageView rightImage;
    private TextView rightLabel;
    private ImageView deleteIcon;
    private View underline;
    private View rightElements;
    private CountDownProgressView progressView;
    private TextInputLayout textLayout;
    private TextView textTitle;
    private View textInfoContainer;
    private TextView textInfo;
    private Drawable unfocusedDrawable, focusedDrawable;

    private boolean isLocked = false;
    private boolean secureState = false;
    private boolean hasTitle = false;
    private JSONObject secureObject;

    private String theme;
    private boolean isButtonClicked = false;
    private boolean isButtonSendFirst = false;
    private String buttonRepeatTitle;
    private int buttonTime = 0;
    private int leftTime = 0;

    private static final int[] SECURE_IMAGES = {R.drawable.invisible, R.drawable.visible};

    protected final Handler mHandler = new Handler();

    public HeroPaperTextField(Context context) {
        super(context);
        init(context, null);
    }

    public HeroPaperTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HeroPaperTextField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeroPaperTextField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
//        this.setOrientation(VERTICAL);
        View view = LayoutInflater.from(context).inflate(R.layout.paper_textfield, this, true);
        textField = (HeroTextField) view.findViewById(R.id.textField);
        underline = view.findViewById(R.id.underline);
        rightButton = (HeroButton) view.findViewById(R.id.rightButton);
        rightImage = (HeroImageView) view.findViewById(R.id.rightImage);
        rightLabel = (TextView) view.findViewById(R.id.rightLabel);
        textLayout = (TextInputLayout) findViewById(R.id.textLayout);
        rightElements = view.findViewById(R.id.rightElements);
        deleteIcon = (ImageView) view.findViewById(R.id.deleteIcon);
        progressView = (CountDownProgressView) view.findViewById(R.id.progress);
        textTitle = (TextView) view.findViewById(R.id.textTitle);
        theme = "light";
        deleteIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textField != null) {
                    textField.setText("");
                }
            }
        });
        if (textLayout != null) {
            textLayout.setHintTextAppearance(R.style.PaperTextFieldFloatingText);
        }
        rightElements.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightImage != null && rightImage.getVisibility() == VISIBLE) {
                    try {
                        rightImage.performClick();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        JSONObject newJson = new JSONObject(jsonObject.toString());

        if (jsonObject.has("theme")) {
            theme = jsonObject.getString("theme");
            if (theme.equals("black")) {
                textField.setTextColor(getResources().getColor(R.color.paper_input_text_dark));
                underline.setBackgroundResource(R.drawable.textfield_underline_dark);
            }
        }

        if (jsonObject.has("title")) {
            hasTitle = true;
            setVisible(textTitle);
            textLayout.setHintEnabled(false);
            textTitle.setText(jsonObject.optString("title"));
        }

        if (jsonObject.has("drLocked")) {
            isLocked = jsonObject.getBoolean("drLocked");
            setVisible(rightImage);
            if (isLocked) {
                textField.setEnabled(false);
            }
        }

        if (jsonObject.has("drSecure")) {
            secureObject = jsonObject.getJSONObject("drSecure");
            secureState = secureObject.getBoolean("secure");
            setVisible(rightImage);
            newJson.put("secure", secureState);
            int resId = secureState ? getSecureImages()[0] : getSecureImages()[1];
            rightImage.setImageResource(resId);
            rightImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSecureState(!secureState);
                }
            });
        }

        if (jsonObject.has("rightImage")) {
            setVisible(rightImage);
            JSONObject object = jsonObject.getJSONObject("rightImage");
            if (object != null) {
                rightImage.on(object);
            }
        }

        if (jsonObject.has("rightButton")) {
            setVisible(rightButton);
            JSONObject object = jsonObject.getJSONObject("rightButton");
            if (object.has("repeatTitle")) {
                buttonRepeatTitle = object.getString("repeatTitle");
            }
            if (object.has("time")) {
                buttonTime = object.getInt("time");
            }

            if (object != null) {
                if (HeroView.getJson(rightButton) == null) {
                    rightButton.setTag(R.id.kHeroJson, object);
                }
                rightButton.on(object);
                if (object.has("isSendFirst")) {
                    isButtonSendFirst = object.getBoolean("isSendFirst");
                    if (isButtonSendFirst) {
                        try {
                            rightButton.performClick();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (jsonObject.has("startCountDown")) {
            boolean start = jsonObject.getBoolean("startCountDown");
            if (start && !TextUtils.isEmpty(buttonRepeatTitle) && buttonTime != 0) {
                rightButton.setVisibility(GONE);
                leftTime = buttonTime;
                progressView.setVisibility(VISIBLE);
                progressView.start(buttonTime * 1000);
                progressView.setProgressListener(new CountDownProgressView.ProgressListener() {
                    @Override
                    public void onProgressEnd() {
                        if (!TextUtils.isEmpty(buttonRepeatTitle)) {
                            rightButton.setText(buttonRepeatTitle);
                        }
                        rightButton.setVisibility(VISIBLE);
                    }
                });
            }
        }

        if (jsonObject.has("unitText")) {
            setVisible(rightLabel);
            rightLabel.setText(jsonObject.getString("unitText"));
        }

        if (jsonObject.has("hideBottomLine")) {
            boolean isHide = jsonObject.getBoolean("hideBottomLine");
            if (isHide) {
                underline.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        if (jsonObject.has("infoText")) {
            addInfoText(jsonObject.optString("infoText"));
        }

        if (textField != null) {
            newJson.remove("frame");
            newJson.remove("class");
            newJson.remove("borderColor");
            if (newJson.has("placeHolder") && !hasTitle) {
                textLayout.setHint(newJson.getString("placeHolder"));
                newJson.remove("placeHolder");
            }
            // put serverLog value for tracking
            JSONObject endEditObj = null;
            if (newJson.has("textFieldDidEndEditing")) {
                endEditObj = newJson.getJSONObject("textFieldDidEndEditing");
            } else {
                endEditObj = new JSONObject();
            }
            endEditObj.put("serverLog", true);
            putValueToJsonObject(newJson, "textFieldDidEndEditing", endEditObj);

            JSONObject beginEditObj;
            if (newJson.has("textFieldDidBeginEditing")) {
                beginEditObj = newJson.getJSONObject("textFieldDidBeginEditing");
            } else {
                beginEditObj = new JSONObject();
            }
            beginEditObj.put("serverLog", true);
            putValueToJsonObject(newJson, "textFieldDidBeginEditing", beginEditObj);

            if (HeroView.getJson(textField) == null) {
                textField.setTag(R.id.kHeroJson, newJson);
            }
            textField.on(newJson);
            textField.setFocusChangeListener(this);
            textField.addTextChangedListener(this);
        }

    }

    private void putValueToJsonObject(JSONObject rootObject, String key, Object value) throws JSONException {
        if (rootObject != null && value != null) {
            rootObject.put(key, value);
        }
    }

    private void setVisible(View view) {
        if (view != null) {
            view.setVisibility(VISIBLE);
        }
    }

    private void setInvisible(View view) {
        if (view != null) {
            view.setVisibility(GONE);
        }
    }

    private void toggleSecureState(boolean isSecure) {
        JSONObject click = new JSONObject();
        try {
            if (isSecure) {
                click.put("secure", true);
            } else {
                click.put("secure", false);
            }
            textField.on(click);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        secureState = isSecure;
        int resId = isSecure ? getSecureImages()[0] : getSecureImages()[1];
        rightImage.setImageResource(resId);
    }

    private void addInfoText(String text) {
        if (textInfo == null) {
            textInfoContainer = LayoutInflater.from(getContext()).inflate(R.layout.paper_textfield_info, this, false);
            textInfo = (TextView) textInfoContainer.findViewById(R.id.titleView);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            this.addView(textInfoContainer, lp);
            // set the input container above the info text
            View inputContainer = (View) textLayout.getParent();
            LayoutParams params = (LayoutParams) inputContainer.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, textInfoContainer.getId());
            inputContainer.setLayoutParams(params);
        }
        if (textInfo != null && !TextUtils.isEmpty(text)) {
            textInfo.setText(text);
        }
    }

    @Override
    public void focusChanged(boolean hasFocus) {
        underline.setSelected(hasFocus);
        if (hasFocus) {
            if (textField.isEnabled() && !TextUtils.isEmpty(textField.getText())) {
                setDeleteIconEnabled(true);
            }
        } else {
            setDeleteIconEnabled(false);
        }
        if (textInfoContainer != null) {
            textInfoContainer.setVisibility(hasFocus ? VISIBLE : INVISIBLE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (textField.isEnabled() && textField.hasFocus() && s.length() > 0) {
            setDeleteIconEnabled(true);
        } else {
            setDeleteIconEnabled(false);
        }
    }

    private void setDeleteIconEnabled(boolean enabled) {
        if (enabled) {
            deleteIcon.setAlpha(1.0f);
            deleteIcon.setClickable(true);
        } else {
            deleteIcon.setAlpha(0.0f);
            deleteIcon.setClickable(false);
        }
    }
    private String getButtonLeftTime() {
        return leftTime + "s";
    }

    private int[] getSecureImages() {
        return SECURE_IMAGES;
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            leftTime--;
            if (leftTime > 0) {
                rightButton.setText(getButtonLeftTime());
                mHandler.postDelayed(timerRunnable, 1000L);
            } else {
                rightButton.setEnabled(true);
                rightButton.setText(buttonRepeatTitle);
            }
        }
    };
}
