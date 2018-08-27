package com.nec.android.baas.tutorial3;

import com.nec.baas.core.*;
import com.nec.baas.http.NbHttpClient;

import android.app.Application;
import android.util.Log;

/**
 * アプリケーションクラス
 */
public class MainApplication extends Application {
    private NbService mService;
    
    private static final String TAG = "MainApplication";
    
    private static MainApplication sApplication;
    
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        
        sApplication = this;

        NbSetting.setOperationMode(NbOperationMode.DEBUG);
        //NbHttpClient.getInstance().setAllowSelfSignedCertificate(true);

        // BaaS 初期化処理。NbService を生成する。
        mService = new NbAndroidServiceBuilder(this)
                .tenantId(Consts.TENANT_ID)
                .appId(Consts.APP_ID)
                .appKey(Consts.APP_KEY)
                .endPointUri(Consts.ENDPOINT_URI)
                .build();
    }
    
    /**
     * アプリケーションインスタンスを取得する
     * @return
     */
    public static MainApplication getInstance() {
        return sApplication;
    }
    
    /**
     * NbService を取得する
     * @return NbService
     */
    public NbService getNebulaService() {
        return mService;
    }
}
