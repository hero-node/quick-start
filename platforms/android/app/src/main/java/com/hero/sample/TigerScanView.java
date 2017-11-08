/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name Hero nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hero.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.hero.HeroActivity;
import com.hero.HeroFragmentActivity;
import com.hero.HeroView;
import com.hero.IHero;
import com.hero.IHeroContext;
import com.hero.depandency.MPermissionUtils;
import com.megvii.licensemanager.Manager;
import com.megvii.livenessdetection.LivenessLicenseManager;
import com.megvii.livenesslib.LivenessActivity;
import com.megvii.livenesslib.util.ConUtil;
import com.megvii.livenesslib.util.SharedUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import exocr.bankcard.CardRecoActivity;
import exocr.bankcard.EXBankCardInfo;
import exocr.exocrengine.EXIDCardResult;
import exocr.idcard.CaptureActivity;
import exocr.idcard.IDCardEditActivity;
import exocr.idcard.IDPhoto;
import io.reactivex.functions.Consumer;

import static android.os.Build.VERSION_CODES.M;


/**
 * Created by R9L7NGH on 2015/12/22.
 */
public class TigerScanView extends View implements IHero,HeroFragmentActivity.IRequestView {

    private String uuid;
    private SharedUtil mSharedUtil;
    IDPhoto idPhoto;

    public TigerScanView(Context c) {
        super(c);
    }

    @Override
    public void on(final JSONObject object) throws JSONException {
        HeroView.on(this,object);
        if (object.has("live")) {
            netWorkWarranty();
        }
        if (object.has("idcard_front")){
            MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.CAMERA, new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) {
                    if (aBoolean) {
                        Intent scanIntent = new Intent(getContext(), exocr.idcard.CaptureActivity.class);
                        scanIntent.putExtra("ShouldFront", true);
                        ((HeroFragmentActivity)TigerScanView.this.getContext()).startActivityForResult(TigerScanView.this,scanIntent, 1);
                    } else {
                        ConUtil.showToast(TigerScanView.this.getContext(), "获取相机权限失败");
                    }
                }
            });
        }
        if (object.has("idcard_back")){
            MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.CAMERA, new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) {
                    if (aBoolean) {
                        Intent scanIntent = new Intent(getContext(), exocr.idcard.CaptureActivity.class);
                        scanIntent.putExtra("ShouldFront", false);
                        ((HeroFragmentActivity)TigerScanView.this.getContext()).startActivityForResult(TigerScanView.this,scanIntent, 2);
                    } else {
                        ConUtil.showToast(TigerScanView.this.getContext(), "获取相机权限失败");
                    }
                }
            });
        }
        if (object.has("bankcard")){
            MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.CAMERA, new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) {
                    if (aBoolean) {
                        Intent scanIntent = new Intent(getContext(), exocr.bankcard.CardRecoActivity.class);
                        ((HeroFragmentActivity)TigerScanView.this.getContext()).startActivityForResult(TigerScanView.this,scanIntent, 3);
                    } else {
                        ConUtil.showToast(TigerScanView.this.getContext(), "获取相机权限失败");
                    }
                }
            });
        }
    }
    private void fail(){
        JSONObject objecct = new JSONObject();
        try {
            objecct.put("name",HeroView.getName(this));
            objecct.put("value","fail");
            ((IHeroContext) this.getContext()).on(objecct);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void sucess(JSONObject value){
        JSONObject objecct = new JSONObject();
        try {
            objecct.put("name",HeroView.getName(this));
            objecct.put("value",value);
            ((IHeroContext) this.getContext()).on(objecct);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("error","requestCode"+requestCode+"resultCode"+requestCode);
        if (requestCode == 0){
            if (data != null && data.getExtras() !=null) {
                Bundle extras = data.getExtras();
                String delta = extras.getString("delta");
                Serializable images = extras.getSerializable("images");
                byte[] best_image = ((Map<String,byte[]>)images).get("image_best");
                File cache = new File(this.getContext().getCacheDir(), "faceplusplus");
                BufferedOutputStream stream = null;
                FileOutputStream fstream = null;
                try {
                    fstream = new FileOutputStream(cache);
                    stream = new BufferedOutputStream(fstream);
                    stream.write(best_image);
                    JSONObject value = new JSONObject();
                    value.put("delta",delta);
                    value.put("filename",cache.getAbsolutePath());
                    sucess(value);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                        if (null != fstream) {
                            fstream.close();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }else {
                fail();
            }
        }else if (requestCode == 1){
            if (data != null && data.getExtras() !=null) {
                Bundle extras = data.getExtras();
                EXIDCardResult recoResult = extras.getParcelable(exocr.idcard.CaptureActivity.EXTRA_SCAN_RESULT);
                //获取照片
                Bitmap IDCardFrontFullImage = CaptureActivity.IDCardFrontFullImage;
                try {
                    JSONObject value = new JSONObject();
                    value.put("code",recoResult.cardnum);
                    value.put("name",recoResult.name);
                    value.put("nation",recoResult.nation);
                    value.put("filename",saveBitmap(this.getContext(),IDCardFrontFullImage));
                    sucess(value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }else{
                fail();
            }

        }else if (requestCode == 2){
            if (data != null && data.getExtras() !=null) {
                Bundle extras = data.getExtras();
                EXIDCardResult recoResult = extras.getParcelable(exocr.idcard.CaptureActivity.EXTRA_SCAN_RESULT);
                //获取照片
                Bitmap IDCardBackFullImage = CaptureActivity.IDCardBackFullImage;
                try {
                    JSONObject value = new JSONObject();
                    value.put("valid",recoResult.validdate);
                    value.put("issue",recoResult.office);
                    value.put("filename",saveBitmap(this.getContext(),IDCardBackFullImage));
                    sucess(value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                fail();
            }
        }else if (requestCode == 3) {
            if (data != null && data.getExtras() !=null){
                Bundle extras = data.getExtras();
                EXBankCardInfo bankCardInfo = extras.getParcelable("bankcard");
                Bitmap cardFullImage = CardRecoActivity.cardFullImage;
                try {
                    JSONObject value = new JSONObject();
                    value.put("bankcard",bankCardInfo.strNumbers);
                    value.put("bankname",bankCardInfo.strBankName);
                    value.put("filename",saveBitmap(this.getContext(),cardFullImage));
                    sucess(value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                fail();
            }
        }

    }

    /**
     * 联网授权
     */
    private void netWorkWarranty() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager manager = new Manager(TigerScanView.this.getContext());
                LivenessLicenseManager licenseManager = new LivenessLicenseManager(TigerScanView.this.getContext());
                manager.registerLicenseManager(licenseManager);

                manager.takeLicenseFromNetwork(uuid);
                if (licenseManager.checkCachedLicense() > 0)
                    mHandler.sendEmptyMessage(1);
                else
                    mHandler.sendEmptyMessage(2);
            }
        }).start();
    }

    private void requestCameraPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this.getContext(),
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                MPermissionUtils.requestPermissionAndCall(getContext(), Manifest.permission.CAMERA, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        if (aBoolean) {
                            ((HeroFragmentActivity)TigerScanView.this.getContext()).startActivityForResult(TigerScanView.this,new Intent(TigerScanView.this.getContext(), LivenessActivity.class), 0);
                        } else {
                            ConUtil.showToast(TigerScanView.this.getContext(), "获取相机权限失败");
                        }
                    }
                });
            } else {
                ((HeroFragmentActivity)this.getContext()).startActivityForResult(this,new Intent(this.getContext(), LivenessActivity.class), 0);
            }
        } else {
            ((HeroFragmentActivity)this.getContext()).startActivityForResult(this,new Intent(this.getContext(), LivenessActivity.class), 0);
        }
    }
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                requestCameraPerm();
                break;
            case 2:
                ConUtil.showToast(TigerScanView.this.getContext(), "授权失败");
                break;
        }
        }
    };
    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }
    public String saveBitmap(Context context, Bitmap mBitmap) {
        String savePath = this.getContext().getCacheDir().getAbsolutePath();
        File filePic;

        try {
            filePic = new File(savePath +'/'+ generateFileName() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return filePic.getAbsolutePath();
    }

}
