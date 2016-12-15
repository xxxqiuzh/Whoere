package JavaBean.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by qiuzh on 2016/11/24.
 */

public class MyTime {
    public static String geTimeNoS(){
        Date date=new Date();
        SimpleDateFormat df=new SimpleDateFormat("MM-dd HH:mm");
        String time=df.format(date);
        return time;
    }
    public static String geTime(){
        Date date=new Date();
        SimpleDateFormat df=new SimpleDateFormat("MM-dd HH:mm:ss");
        String time=df.format(date);
        return time;
    }
}
