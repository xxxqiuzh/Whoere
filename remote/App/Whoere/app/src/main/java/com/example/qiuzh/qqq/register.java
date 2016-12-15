package com.example.qiuzh.qqq;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import JavaBean.dao.User;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class register extends AppCompatActivity {

    private EditText UText;
    private EditText PText;
    private Button   btn_register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        UText = (EditText) findViewById(R.id.username);
        PText = (EditText) findViewById(R.id.password);
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = UText.getText().toString();
                String password = PText.getText().toString();
                Register(username,password);
            }
        });
    }

    private void Register(String username, String password){
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUp(new SaveListener<User>() {

            @Override
            public void done(User user, BmobException e) {
                if(e==null){
                    Toast.makeText(register.this,"注册成功",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(register.this,MainActivity.class);
                    startActivity(intent);
                }else{
                    if(e.getErrorCode() == 202){
                        Toast.makeText(register.this,"该用户名已被注册",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(register.this,"注册失败，请稍后重试",Toast.LENGTH_SHORT).show();
                    }
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

}
