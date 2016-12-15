package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qiuzh.qqq.R;
import com.example.qiuzh.qqq.successLogin;

import java.util.List;

import JavaBean.dao.ChatEntity;

/**
 * Created by qiuzh on 2016/11/24.
 */

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private List<ChatEntity> list;
    LayoutInflater inflater;
    private int[] avatar=new int[]{0, R.drawable.h001,R.drawable.h002,R.drawable.h003,
            R.drawable.h004,R.drawable.h005,R.drawable.h006};

    public ChatAdapter(Context context,List<ChatEntity> list){
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup root) {
        ImageView avatar;
        TextView content;
        TextView time;
        ChatEntity ce=list.get(position);
        if(ce.isLeft()){
            ce.setAvatar(1);
            convertView = inflater.inflate(R.layout.chat_listview_item_left, null);

            avatar=(ImageView) convertView.findViewById(R.id.avatar_chat_left);
            content=(TextView) convertView.findViewById(R.id.message_chat_left);
            time=(TextView) convertView.findViewById(R.id.sendtime_chat_left);
            int id=ce.getAvatar();
            avatar.setImageResource(successLogin.avatar[id]);
            content.setText(ce.getContent());
            time.setText(ce.getTime());
        }else{
            ce.setAvatar(2);
            convertView=inflater.inflate(R.layout.chat_listview_item_right, null);

            avatar=(ImageView) convertView.findViewById(R.id.avatar_chat_right);
            content=(TextView) convertView.findViewById(R.id.message_chat_right);
            time=(TextView) convertView.findViewById(R.id.sendtime_chat_right);
            int id=ce.getAvatar();
            avatar.setImageResource(successLogin.avatar[id]);
            content.setText(ce.getContent());
            time.setText(ce.getTime());
        }

        return convertView;
    }
    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
}

