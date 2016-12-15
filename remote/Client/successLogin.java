package com.example.qiuzh.qqq;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Adapter.ChatAdapter;
import JavaBean.dao.ChatEntity;
import JavaBean.dao.MyTime;
import JavaBean.dao.User;
import cn.bmob.v3.BmobUser;

public class successLogin extends AppCompatActivity {
    private final User user = BmobUser.getCurrentUser(User.class); //获取自定义用户信息
    private boolean senderOrReceiver;
    private EditText message = null;
    private Button btn_send = null;
    private static Socket ClientSocket = null;
    private static PrintWriter os = null;
    private static BufferedReader is = null;
    private String MsgAccept;
    private Handler handler = new Handler();
    private static boolean tag = true;
    private DialogInterface exitDialog;

    private ListView chatListView;
    private ChatAdapter chatAdapter;
    public List<ChatEntity> chatEntityList = new ArrayList<ChatEntity>();//所有聊天内容
    public static int[] avatar = new int[]{R.drawable.avatar_default, R.drawable.h001, R.drawable.h002, R.drawable.h003,
            R.drawable.h004, R.drawable.h005, R.drawable.h006};

    private void initView() {
        btn_send = (Button) findViewById(R.id.button_send);
        message = (EditText) findViewById(R.id.message);

        chatListView = (ListView) findViewById(R.id.lv_chat);
        chatAdapter = new ChatAdapter(this, chatEntityList);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button_send:
                        // TODO: 15-9-4 发送数据线程
                        sendMsgThread();
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_login);

        connectThread();
    }

    private void acceptMsgThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    is = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
                    while (tag) {
                        try {
                            MsgAccept = is.readLine();
                            if (MsgAccept != null) {
                                int[] index = new int[2];
                                index[0] = MsgAccept.indexOf("username:");
                                index[1] = MsgAccept.indexOf("longitude:");
                                String username = MsgAccept.substring(index[0] + "username:".length(), index[1]);
                                int defaultIndex = MsgAccept.indexOf("ipAddress:");
                                if (defaultIndex == 1) {
                                    if (username.equals(user.getUsername())) {
                                        index[0] = MsgAccept.indexOf("counts:");
                                        index[1] = MsgAccept.indexOf("message:");
                                        String counts = MsgAccept.substring(index[0] + "counts:".length(), index[1]);
                                        if (counts.equals("1")) {
                                            Log.i("Whoere", "neighbor = " + counts);
                                            handler.post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    dialog("您的周围当前没有在线的共同好友，点击确定按钮重新设置范围","返回设置");
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    handler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            int[] index = new int[3];
                                            index[0] = MsgAccept.indexOf("username:");
                                            index[1] = MsgAccept.indexOf("longitude:");
                                            String username = MsgAccept.substring(index[0] + "username:".length(), index[1]);
                                            index[2] = MsgAccept.indexOf("message:");
                                            String message = MsgAccept.substring(index[2] + "message:".length());
                                            if (username.equals(user.getUsername()))
                                                senderOrReceiver = false;   //发送者
                                            else
                                                senderOrReceiver = true;    //接收者
                                            updateChatView(new ChatEntity(avatar[1], message, MyTime.geTime(), senderOrReceiver));
                                            chatListView.setSelection(chatAdapter.getCount() - 1);
                                        }
                                    });
                                }
                                Log.i("Whoere", "Server : " + MsgAccept);
                            } else {
                                Log.i("Whoere", "null" + "\n");
                                Toast.makeText(successLogin.this, "服务器异常", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        } catch (Exception e) {
                            Log.i("Whoere", "exception" + e.getMessage());
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendMsgThread() {

        final String MsgSend = message.getText().toString();
        final String Msg = "username:" + user.getUsername().trim() + "longitude:" + user.getLongitude().trim()
                + "latitude:" + user.getLatitude().trim() + "bounds:" + user.getBounds().trim()
                + "tags:" + user.getTags().trim() + "message:" + MsgSend;
        message.setText("");

        new Thread(new Runnable() {
            @Override
            public void run() {
                os.println(Msg);
                os.flush();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Whoere", "发送成功：" + "\n");
                    }
                });
            }
        }).start();
    }

    private String defaultSend() {
        return "$username:" + user.getUsername() + "longitude:" + user.getLongitude() +
                "latitude:" + user.getLatitude() + "bounds:" + user.getBounds()
                + "tags:" + user.getTags() + "message:";
    }

    private void exit() {
        final String message = "username:" + user.getUsername() + "longitude:" + user.getLongitude() +
                "latitude:" + user.getLatitude() + "bounds:" + user.getBounds()
                + "tags:" + user.getTags() + "message:end";
        new Thread(new Runnable() {
            @Override
            public void run() {
                os.println(message);
                os.flush();
                try {
                    is.close();
                    os.close();
                    ClientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Whoere", "发送退出信息成功：" + "\n");
                    }
                });
                exitDialog.dismiss();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }).start();
    }

    private void connectThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ClientSocket = new Socket("115.28.140.178", 5469);
                    if (ClientSocket.isConnected()) {
                        //发送默认消息
                        os = new PrintWriter(ClientSocket.getOutputStream());
                        os.println(defaultSend());
                        os.flush();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("Whoere", "连接成功！" + "\n");
                                acceptMsgThread();
                                initView();
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("Whoere", "连接失败！" + "\n");
                            }
                        });
                    }
                } catch (IOException e) {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            exceptionDialog("服务器异常，请稍后重试！", "退出");
                        }
                    });
                    Log.i("Whoere", "Catch Exception! 连接失败！");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void updateChatView(ChatEntity chatEntity) {
        chatEntityList.add(chatEntity);
        chatListView.setAdapter(chatAdapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            dialog("确定要退出吗?","提示");
            return true;
        }
        return true;
    }

    protected void dialog(String tips, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(tips);
        builder.setTitle(title);
        builder.setPositiveButton("确认",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitDialog = dialog;
                        exit(); //发送客户端退出报文
//                        dialog.dismiss();
//                        //AccoutList.this.finish();
//                        //System.exit(1);
//                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    protected void exceptionDialog(String tips, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(tips);
        builder.setTitle(title);
        builder.setPositiveButton("确认",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

}

