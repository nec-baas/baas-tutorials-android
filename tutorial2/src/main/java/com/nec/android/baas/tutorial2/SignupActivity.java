package com.nec.android.baas.tutorial2;

import com.nec.baas.core.*;
import com.nec.baas.user.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

// サインアップ画面のアクティビティ
public class SignupActivity extends Activity implements OnClickListener {
    /**
     * NebulaService インスタンス
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
     * パスワード(確認)エディットテキスト
     */
    private EditText edittext_passwordconfirm;
    
    /**
     * サインアップボタン
     */
    private Button button_signup;
    
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
        
        setContentView(R.layout.activity_signup);
        
        edittext_email = (EditText)this.findViewById(R.id.editText_emailinsignup);
        edittext_password = (EditText)this.findViewById(R.id.editText_passwordinsignup);
        edittext_passwordconfirm = (EditText)this.findViewById(R.id.editText_passwordconfirm);
        
        button_signup = (Button)this.findViewById(R.id.button_signupinsignup);
        button_signup.setOnClickListener(this);
        
        button_back = (Button)this.findViewById(R.id.button_back);
        button_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == button_signup) {
            // サインアップボタン押下時
            String email = edittext_email.getText().toString();
            String password = edittext_password.getText().toString();
            String password_confirm = edittext_passwordconfirm.getText().toString();
            
            if (!password.equals(password_confirm)) {
                // パスワード不一致
                showAlert("Password does not match.");
                return;
            }

            NbUser user = new NbUser(mService);

            // Emailを登録
            user.setEmail(email);
            
            // サインアップ処理
            user.register(password, new NbUsersCallback() {
                @Override
                public void onSuccess(List<NbUser> users) {
                    showAlert("User registered");

                    // ログイン画面のアクティビティに遷移
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                @Override
                public void onFailure(int status, NbErrorInfo errorInfo) {
                    showAlert("signup failed : " + status + " reason: " + errorInfo.getReason());
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
                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
