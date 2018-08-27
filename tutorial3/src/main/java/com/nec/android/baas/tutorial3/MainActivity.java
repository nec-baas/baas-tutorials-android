package com.nec.android.baas.tutorial3;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.nec.baas.core.*;
import com.nec.baas.file.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * メインアクティビティ
 */
public class MainActivity extends ListActivity implements OnItemLongClickListener {
    public static final boolean DEBUG = true;
    public static final String TAG = "Tutorial3";

    /**
     * NbService インスタンス
     */
    private NbService mService;
    
    /**
     * Fileバケット
     */
    private NbFileBucket mBucket;
    
    /**
     * Fileメタ情報リスト
     */
    private List<NbFileMetadata> mMetaList;
    
    /**
     * List表示用の adapter
     */
    //private ArrayAdapter<String> mArrayAdapter;
    private ImageAdapter mArrayAdapter;
    
    File mSaveFile;

    /**
     * 初期化処理
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // NbService インスタンスを取得
        mService = MainApplication.getInstance().getNebulaService();
        
        // ListView のセットアップ
        mArrayAdapter = new ImageAdapter(this, R.layout.image_item, new ArrayList<String>());
        ListView listView = this.getListView();
        listView.setAdapter(mArrayAdapter);
        listView.setOnItemLongClickListener(this);
        
        // バケット取得
        mBucket = mService.fileBucketManager().getBucket("AlbumTutorial");

        // データロード
        loadImages();
    }

    /**
     * 画像データをサーバから取得する
     */
    private void loadImages() {
        mBucket.getFileMetadataList(false, new NbFileMetadataCallback(){

            @Override
            public void onSuccess(List<NbFileMetadata> metas) {
                if(DEBUG){
                    Log.d(TAG, "getFileMetadataList success");
                    Log.d(TAG, "metadata num = " + metas.size());
                    for (NbFileMetadata meta : metas){
                        Log.d(TAG, "name = " + meta.getFileName());
                    }
                }
                mMetaList = metas;
                refresh();
            }
            
            @Override
            public void onFailure(int status, NbErrorInfo ei) {
                showAlert("getFileMetadataList failed : " + status);
            }
        });
    }
    
    /**
     * 画面更新
     */
    private void refresh() {
        mArrayAdapter.clear();
        if (mMetaList == null) return; // do nothing
        
        for (NbFileMetadata meta : mMetaList) {
            mArrayAdapter.add(meta.getFileName());
        }
    }
    
    /**
     * 画像の追加
     */
    private void onAddImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }
    
    //カメラからの戻りチェック
    protected void onActivityResult(int requestCode, int resultCode, Intent data){        
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                //撮影した画像を取得
                Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                setTitle(bitmap);
           }
            else{
                showAlert("get image failed. result = " + resultCode);
            }
        }
    }
    
    private void setTitle(Bitmap bmp){
        final Bitmap bitmap = bmp;
        final EditText text = new EditText(this);
        
        new AlertDialog.Builder(this)
        .setTitle("タイトルを入力")
        .setView(text)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addImage(bitmap, text.getText().toString());
            }
        })
        .show();
    }
    
    /**
     * 画像追加処理
     * @param s
     */
    private void addImage(Bitmap bitmap, String s) {
        if (mBucket == null) return;

        final String title = s;

        //サーバへの登録用にInputStreamに変換
        ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
        bitmap.compress(CompressFormat.PNG, 0, bos); 
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(bitmapdata);

        //ACL設定
        ArrayList<String> list = new ArrayList<String>();
        list.add("g:anonymous");
        NbAcl acl = new NbAcl();
        acl.setAdmin(list);
        acl.setRead(list);
        acl.setDelete(list);
        
        mBucket.uploadNewFile(title, bis, acl, "image/png", new NbFileMetadataCallback(){
            @Override
            public void onSuccess(List<NbFileMetadata> metas) {
                if(DEBUG){
                    Log.d(TAG, "uploadNewFile success");
                }
                loadImages();
            }
            
            @Override
            public void onFailure(int status, NbErrorInfo ei) {
                showAlert("upload failed : " + status);              
            }
        });
    }
    
    /**
     * 画像の削除処理
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        NbFileMetadata meta = mMetaList.get(position);
        mBucket.deleteFile(meta.getFileName(), new NbResultCallback(){

            @Override
            public void onSuccess() {
                if(DEBUG){
                    Log.d(TAG, "deleteFile success");
                }
                loadImages();
                
            }

            @Override
            public void onFailure(int status, NbErrorInfo ei) {
                showAlert("delete failed : " + status);              
            }
        });
        
        return true;
    }
    
    /**
     * Alert を表示する
     * @param message
     */
    public void showAlert(final String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
            onAddImage();
            return true;
        }
        /*
        else if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }
    
    public NbFileBucket getFileBucket(){
        return mBucket;
    }
}

