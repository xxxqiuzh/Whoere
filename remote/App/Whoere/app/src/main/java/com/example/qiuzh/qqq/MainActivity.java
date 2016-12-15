package com.example.qiuzh.qqq;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import JavaBean.dao.User;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class MainActivity extends AppCompatActivity {

    private EditText UText;
    private EditText PText;
    private Button btn_login;
    private Button btn_register;
    private TextView text_gps;
    private LocationManager manager;
    private Location location;
    private String ip = "0.0.0.0";
    private String longitude = "117.130345";
    private String latitude = "36.668129";
//    private String longitude, latitude; //经度、纬度、范围、标签
    private String bounds = "0.1";       //范围100m
    private String tags = "学习";       //标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bmob.initialize(this, "1f74200b695d3dca1c4a95f07889f1da");

        ensureMessage();
        UText = (EditText) findViewById(R.id.username);
        PText = (EditText) findViewById(R.id.password);
        text_gps = (TextView) findViewById(R.id.text_gps);
        GetGps();
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = UText.getText().toString();
                String password = PText.getText().toString();
                Login(username,password);
            }
        });

        btn_register = (Button) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,register.class);
                startActivity(intent);
            }
        });
    }

    private void Login(final String username, final String password){
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);
        user.login(new SaveListener<User>() {

            @Override
            public void done(User user, BmobException e) {
                if(e==null){
                    updateUser(username);
                    //通过BmobUser user = BmobUser.getCurrentUser()获取登录成功后的本地用户信息
                    //如果是自定义用户对象MyUser，可通过MyUser user = BmobUser.getCurrentUser(MyUser.class)获取自定义用户信息
                }else{
                    if(e.getErrorCode() == 101){
                        Toast.makeText(MainActivity.this,"用户名或密码错误",Toast.LENGTH_SHORT).show();
                    }else{
                        if(e.getErrorCode() == 9016){
                            Toast.makeText(MainActivity.this,"请检查网络",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this,"登录失败，请稍后重试",Toast.LENGTH_SHORT).show();
                        }
                    }
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    private void GetGps() {
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }else{
            location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 8, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                showLocation(location);
                updateLocation(location);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }//获得权限
                updateLocation(manager.getLastKnownLocation(provider));
            }
            @Override
            public void onProviderDisabled(String provider) {
                updateLocation(null);
            }
        });
    }
    private void showLocation(Location location) {
        String currentPosition=+location.getLatitude()+"    "+location.getLongitude();
        text_gps.setText(currentPosition);
    }

    private void updateLocation(Location location){
        if(location!=null){
            longitude = Double.toString(location.getLongitude());
            latitude = Double.toString(location.getLatitude());
        }
        else{
            longitude = "117.130345";
            latitude = "36.668129";
            Toast.makeText(MainActivity.this, "定位失败，使用上次定位数据", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUser(String username){
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setIp(ip);
        newUser.setLongitude(longitude);
        newUser.setLatitude(latitude);
        newUser.setBounds(bounds);
        newUser.setTags(tags);
        BmobUser bmobUser = BmobUser.getCurrentUser(User.class);
        newUser.update(bmobUser.getObjectId(),new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Intent intent=new Intent(MainActivity.this,successLogin.class);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
//                    finish();
                    Log.i("bmob","更新用户信息成功");
                }else{
                    Log.i("bmob","更新用户信息失败:" + e.getMessage());
                }
            }
        });
    }

    private void Exit(){
        finish();
    }

    private void ensureMessage(){
        Spinner s1 = (Spinner) findViewById(R.id.spinnerbounds);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.bounds, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        s1.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        bounds = parent.getSelectedItem().toString();
//                        showToast("Spinner1: position=" + position + " id=" + id + " value=" + bounds);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
//                        showToast("Spinner1: unselected");
                    }
                });

        Spinner s2 = (Spinner) findViewById(R.id.spinnertags);
        adapter = ArrayAdapter.createFromResource(this, R.array.tags,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s2.setAdapter(adapter);
        s2.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        tags = parent.getSelectedItem().toString();
//                        showToast("Spinner2: position=" + position + " id=" + id + " value=" + tags);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
//                        showToast("Spinner2: unselected");
                    }
                });
    }

    private void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
