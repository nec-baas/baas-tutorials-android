package com.nec.android.baas.tutorial2;

import android.app.Application;
import android.util.Log;

import com.nec.baas.core.*;
import com.nec.baas.http.*;

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

        // サーバ自己署名証明書を許可する。
        // 呼び出しはNebulaServiceの生成前に行うこと。
        //
        // デバッグ用なので、本番時には使用しないこと!!!
        // (本番時は正規のサーバ証明書を使用すること)
        //NbHttpClient.getInstance().setAllowSelfSignedCertificate(true);

        // set debug mode
        NbSetting.setOperationMode(NbOperationMode.DEBUG);

        // BaaS 初期化処理。NebulaService を生成する。
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
     * NebulaService を取得する
     * @return NebulaService
     */
    public NbService getNebulaService() {
        return mService;
    }
}
