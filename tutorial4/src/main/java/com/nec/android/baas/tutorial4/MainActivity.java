package com.nec.android.baas.tutorial4;

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

import com.nec.baas.core.NbAcl;
import com.nec.baas.core.NbBucketMode;
import com.nec.baas.core.NbErrorInfo;
import com.nec.baas.core.NbResultCallback;
import com.nec.baas.core.NbService;
import com.nec.baas.object.NbObject;
import com.nec.baas.object.NbObjectBucket;
import com.nec.baas.object.NbObjectCallback;
import com.nec.baas.object.NbObjectSyncResultCallback;
import com.nec.baas.object.NbQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * メインアクティビティ
 */
public class MainActivity extends ListActivity implements OnItemLongClickListener {
    /**
     * NbService インスタンス
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
    
    //オンライン/オフラインモード
    private int mWhich = 0;

    /**
     * 初期化処理
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // NbService インスタンスを取得
        mService = MainApplication.getInstance().getService();

        // ListView のセットアップ
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new ArrayList<String>());
        ListView listView = this.getListView();
        listView.setAdapter(mArrayAdapter);
        listView.setOnItemLongClickListener(this);

        // レプリカモードバケットを取得
        mBucket = mService.objectBucketManager().getBucket("TodoTutorial1", NbBucketMode.REPLICA);

        // 同期スコープ(全範囲)を設定しておく
        mBucket.setSyncScope(new NbQuery());

        // TODO読み込みバケットの読み込みを実行
        loadTodos();
    }

    /**
     * Todo データをロードする(ローカル)
     */
    private void loadTodos() {
        if (mBucket == null) return;

        NbQuery query = new NbQuery();
        query.setSortOrder("updatedAt", true);
        
        mBucket.query(query, new NbObjectCallback() {
            @Override
            public void onSuccess(List<NbObject> objects, Number num) {
                mTodoList = objects;
                refresh();
            }

            @Override
            public void onFailure(int status, NbErrorInfo ei) {
                showAlert("Load todo failed : " + status);
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
     * @param s
     */
    private void addTodo(String s) {
        NbObject todo = mBucket.newObject();

        //暫定処理(#993)
        ArrayList<String> list = new ArrayList<>();
        list.add("g:anonymous");
        NbAcl acl = new NbAcl();
        acl.setAdmin(list);
        acl.setRead(list);
        acl.setWrite(list);
        todo.putAcl(acl);
        
        todo.put("description", s);
        todo.save(new NbObjectCallback() {
            @Override
            public void onSuccess(List<NbObject> todos, Number count) {
                // リロードする
                loadTodos();
            }

            @Override
            public void onFailure(int status, NbErrorInfo ei) {
                showAlert("save object failed : " + status);
            }
        });
    }
    
    /**
     * Todo の削除処理
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
    	final int position = pos;
        
    	new AlertDialog.Builder(this)
    	.setTitle("削除しますか？")
		.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        NbObject todo = mTodoList.get(position);
				deleteTodo(todo);
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//nothing to do
			}
		})
		.show();
        
        return true;
    }
    
    private void deleteTodo(NbObject todo){
        todo.deleteObject(new NbResultCallback() {
            @Override
            public void onSuccess() {
                // Todo をリロードする
                loadTodos();
            }

            @Override
            public void onFailure(int status, NbErrorInfo ei) {
                showAlert("delete failed : " + status);
            }
        });    	
    }
    
    /**
     * Alert を表示する
     * @param message
     */
    private void showAlert(final String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            ProgressDialog d = new ProgressDialog(this);
            d.setMessage(message);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            onAddTodo();
            return true;
        }
        else if (id == R.id.action_refresh) {
            loadTodos();
            return true;
        }
        else if (id == R.id.action_sync) {
        	sync();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sync(){
        showProgressDialog("同期中...");

        //競合に対応するのであれば必要
    	/*
        mService.objectBucketManager().registerSyncEventListener(new SyncEventListener(){
			@Override
			public void onResolveConflict(NbObject object, int resolve) {
				showAlert("onResolveConflict : " + resolve);
			}

			@Override
			public void onSyncCompleted(String bucket, List<String> ids) {
				showAlert("onSyncCompleted : name = " + bucket);
			}

			@Override
			public void onSyncConflicted(ResolveConflict resolveConflict, String bucket,
					NbObject client, NbObject server) {
				showAlert("onSyncConflicted : name = " + bucket);
			}

			@Override
			public void onSyncError(int code, NbObject object) {
				showAlert("onSyncError : " + code);
			}

			@Override
			public void onSyncStart(String bucket) {
				showAlert("onSyncStart : " + bucket);				
			}
        });
        */
    	
    	mBucket.sync(new NbObjectSyncResultCallback(){
			@Override
			public void onSuccess() {
                dismissProgressDialog();
                loadTodos();
			}
    		
			@Override
			public void onFailure(int status, NbErrorInfo ei) {
                dismissProgressDialog();
                showAlert("sync failed: " + status);			
			}
    	});
    }
}
