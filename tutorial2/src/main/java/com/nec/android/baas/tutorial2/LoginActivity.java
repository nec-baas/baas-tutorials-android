package com.nec.android.baas.tutorial2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nec.baas.core.*;
import com.nec.baas.user.*;

import java.util.List;

// ログイン画面のアクティビティ
public class LoginActivity extends Activity implements OnClickListener {
    /**
     * NbService インスタンス
     */
    private NbService mService;
    
    /**
     * E-mailエディットテキスト
     */
    private EditText edittext_email;
    
    /**
     * パスワードエディットテキスト
     */
    private EditText edittext_password;
    
    /**
     * ログインボタン
     */
    private Button button_login;
    
    /**
     * サインアップボタン
     */
    private Button button_signup;
    
    /**
     * パスワードリセットボタン
     */
    private Button button_passreset;
    
    /**
     * ProgressDialog
     */
    private ProgressDialog mProgressDialog;
    
    /**
     *  初期化処理
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // NbService インスタンスを取得
        mService = MainApplication.getInstance().getNebulaService();
        
        setContentView(R.layout.activity_login);
        
        edittext_email = (EditText)this.findViewById(R.id.editText_emailinlogin);
        edittext_password = (EditText)this.findViewById(R.id.editText_passwordinlogin);
        
        button_login = (Button)this.findViewById(R.id.button_login);
        button_login.setOnClickListener(this);
        
        button_signup = (Button)this.findViewById(R.id.button_signupinlogin);
        button_signup.setOnClickListener(this);
        
        button_passreset = (Button)this.findViewById(R.id.button_passwordresetinlogin);
        button_passreset.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // ログインボタン押下時
        if (view == button_login) {
            String email = edittext_email.getText().toString();
            String password = edittext_password.getText().toString();
            
            // ProgressDialog表示
            showProgressDialog();
            
            // ログイン処理
            NbUser.login(null, email, password, new NbUsersCallback() {
                @Override
                public void onSuccess(List<NbUser> users) {
                    // メインアクティビティに遷移
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    dismissProgressDialog();
                }

                @Override
                public void onFailure(int status, NbErrorInfo errorInfo) {
                    showAlert("login failed : " + errorInfo.getReason());
                    dismissProgressDialog();
                }
            });
        }
        // サインアップボタン押下時
        if (view == button_signup) {
            // サインアップ画面のアクティビティに遷移
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        }
        // パスワードリセットボタン押下時
        if (view == button_passreset) {
            // パスワードリセット画面のアクティビティに遷移
            Intent intent = new Intent(LoginActivity.this, PasswordresetActivity.class);
            startActivity(intent);
        }
    }
    
    /**
     * ProgressDialogを表示する
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            ProgressDialog d = new ProgressDialog(this);
            d.setMessage("ログイン中...");
            d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            d.show();

            mProgressDialog = d;
        }
    }

    /**
     * ProgressDialogを消す
     */
    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
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
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
