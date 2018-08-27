package com.nec.android.baas.tutorial2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nec.baas.core.*;
import com.nec.baas.user.*;

// パスワードリセット画面のアクティビティ
public class PasswordresetActivity extends Activity implements OnClickListener {
    /**
     * NebulaService インスタンス
     */
    private NbService mService;
    
    /**
     * E-mailエディットテキスト
     */
    private EditText edittext_email;
    
    /**
     * 送信ボタン
     */
    private Button button_passwordreset;
    
    /**
     * 戻るボタン
     */
    private Button button_back;
    
    /**
     *  初期化処理
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // NebulaService インスタンスを取得
        mService = MainApplication.getInstance().getNebulaService();
        
        setContentView(R.layout.activity_passwordreset);
        
        edittext_email = (EditText)this.findViewById(R.id.editText_emailinpasswordreset);
        
        button_passwordreset = (Button)this.findViewById(R.id.button_passwordreset);
        button_passwordreset.setOnClickListener(this);
        
        button_back = (Button)this.findViewById(R.id.button_backinpasswordreset);
        button_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == button_passwordreset) {
            // 送信ボタン押下時
            String username = null;
            String email = edittext_email.getText().toString();
            
            NbUser.resetPassword(username, email, new NbResultCallback() {
                @Override
                public void onSuccess() {
                    showAlert("パスワードリセットメールを送信しました");
                    // ログイン画面のアクティビティに遷移
                    Intent intent = new Intent(PasswordresetActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                @Override
                public void onFailure(int status, NbErrorInfo errorInfo) {
                    showAlert("PasswordReset failed : " + status);
                }
            });
        }
        if (view == button_back) {
            // 戻るボタン押下時
            // ログイン画面のアクティビティに遷移
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
    
    /**
     * Alert を表示する
     * @param message
     */
    private void showAlert(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PasswordresetActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
