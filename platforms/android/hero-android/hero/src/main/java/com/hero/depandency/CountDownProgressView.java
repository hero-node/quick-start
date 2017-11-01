package com.hero.depandency;

/**
 * Created by xincai on 16-4-19.
 */

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hero.R;

public class CountDownProgressView extends RelativeLayout {
    private long time = 10000L;
    private View view;
    private ProgressBar progressBar;
    private TextView textView;
    private ObjectAnimator animator;
    private ProgressListener listener;

    public CountDownProgressView(Context context) {
        super(context);
        init();
    }

    public CountDownProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountDownProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CountDownProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        view = inflate(getContext(), R.layout.count_down_progress, this);
        progressBar = (ProgressBar) findViewById(R.id.timeoutProgressbar);
        textView = (TextView) findViewById(R.id.countdownTextView);
    }

    public void start(long countDownMillisecond) {
        if (animator != null) {
            animator.cancel();
        }

        animator = ObjectAnimator.ofInt(progressBar, "progress", new int[] {1000, 0});
        animator.setDuration(countDownMillisecond);
        animator.setInterpolator(new LinearInterpolator());
        time = countDownMillisecond;
        animator.start();

        try {
            textView.postDelayed(new Runnable() {
                public void run() {
                    if (progressBar != null && textView != null) {
                        try {
                            update();
                            if (getVisibility() == VISIBLE) {
                                textView.postDelayed(this, 400L);
                            }
                        } catch (NullPointerException e) {
                        }
                    }
                }
            }, 200L);
        } catch (NullPointerException e1) {
        }

    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
        }
    }

    public void destory() {
        stop();
        textView = null;
        progressBar = null;
        animator = null;
        animator = null;
    }

    public void restart() {
        stop();
        start(time);
    }

    public int getProgress() {
        return progressBar.getProgress();
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }

    private void update() {
        long curTime = (long) Math.ceil((double) ((float) time / 1000.0F * (float) progressBar.getProgress() / (float) progressBar.getMax()));
        if (curTime <= 0L) {
            setVisibility(GONE);
            textView.setText("");
            if (listener != null) {
                listener.onProgressEnd();
            }
        } else {
            textView.setTextKeepState(String.format("%02d", curTime));
        }
    }

    public void setRemainTimeSecond(int remainTimeMilliSecond, int totalTimeMilliSecond) {
        time = (long) totalTimeMilliSecond;
        progressBar.setProgress(1000 * remainTimeMilliSecond / totalTimeMilliSecond);
        update();
    }

    public void setProgressListener(ProgressListener progressListener) {
        listener = progressListener;
    }

    public interface ProgressListener {
        public void onProgressEnd();
    }
}
