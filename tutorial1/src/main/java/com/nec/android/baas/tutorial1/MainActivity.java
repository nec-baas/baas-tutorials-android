package com.nec.android.baas.tutorial1;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.nec.baas.core.*;
import com.nec.baas.object.*;

/**
 * メインアクティビティ
 */
public class MainActivity extends ListActivity implements OnItemLongClickListener {
    /**
     * NebulaService インスタンス
     */
    private NbService mService;
    
    /**
     * Todo バケット
     */
    private NbObjectBucket mBucket;
    
    /**
     * Todo リスト
     */
    private List<NbObject> mTodoList;
    
    /**
     * List 表示用の adapter
     */
    private ArrayAdapter<String> mArrayAdapter;

    private ProgressDialog mProgressDialog;

    /**
     * 初期化処理
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // NebulaService インスタンスを取得
        mService = MainApplication.getInstance().getNebulaService();
        
        // ListView のセットアップ
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new ArrayList<String>());
        ListView listView = this.getListView();
        listView.setAdapter(mArrayAdapter);
        listView.setOnItemLongClickListener(this);
        
        // バケット取得
        mBucket = mService.objectBucketManager().getBucket("TodoTutorial1");

        // Todo データをサーバからロードする
        loadTodos();
    }

    /**
     * Todo データをサーバから取得する
     */
    private void loadTodos() {
        if (mBucket == null) return;

        NbQuery query = new NbQuery();
        query.setSortOrder("updatedAt", true);
        
        mBucket.query(query,  new NbObjectCallback() {
            @Override
            public void onSuccess(List<NbObject> objects, Number num) {
                mTodoList = objects;
                refresh();
            }
            
            @Override
            public void onFailure(int status, NbErrorInfo errorInfo) {
                showAlert("Load todo failed: " + status + " reason: " + errorInfo.getReason());
            }
        });
    }
    
    /**
     * 画面更新
     */
    private void refresh() {
        mArrayAdapter.clear();
        if (mTodoList == null) return; // do nothing
        
        for (NbObject todo : mTodoList) {
            String s = todo.getString("description", "-");
            mArrayAdapter.add(s);
        }
    }
    
    /**
     * Todo の追加
     */
    private void onAddTodo() {
        final EditText editView = new EditText(this);
        
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("New Todo");
        b.setView(editView);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editView.getText().toString();
                addTodo(text);
            }
        });
                
        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing;
            }
        });
        b.show();
    }
    
    /**
     * Todo 追加処理
     * @param s TODO文字列
     */
    private void addTodo(String s) {
        // オブジェクト生成
        NbObject todo = mBucket.newObject();

        // オブジェクトに値を設定
        todo.put("description", s);

        // オブジェクト保存
        todo.save(new NbObjectCallback() {
            @Override
            public void onSuccess(List<NbObject> todos, Number count) {
                // リロードする
                loadTodos();
            }

            @Override
            public void onFailure(int status, NbErrorInfo errorInfo) {
                showAlert("save object failed: " + status + " reason: " + errorInfo.getReason());
            }
        });
    }
    
    /**
     * Todo の削除処理
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        NbObject todo = mTodoList.get(position);
        
        todo.deleteObject(new NbResultCallback() {
            @Override
            public void onSuccess() {
                // Todo をリロードする
                loadTodos();
            }

            @Override
            public void onFailure(int status, NbErrorInfo errorInfo) {
                showAlert("delete failed: " + status + " reason:" + errorInfo.getReason());
            }
        });
        
        return true;
    }
    
    /**
     * Alert を表示する
     * @param message
     */
    private void showAlert(final String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            ProgressDialog d = new ProgressDialog(this);
            d.setMessage("Loading...");
            d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            d.show();

            mProgressDialog = d;
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
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
            onAddTodo();
            return true;
        }
        else if (id == R.id.action_refresh) {
            loadTodos();
            return true;
        }
        /*
        else if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }
}
