package JavaBean.dao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {
	private static final long serialVersionUID = 1L;
	private String ip;
	private String longitude; // 经度
	private String latitude; // 纬度
	private String bounds; // 范围
	private String tags; // 标签

//	public User(){
//
//	}
//
//	public User(String username, String password, String ip, String longitude, String latitude) {
//		this.username = username;
//		this.password = password;
//		this.ip = ip;
//		this.longitude = longitude;
//		this.latitude = latitude;
//	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getBounds() {
		return bounds;
	}

	public void setBounds(String bounds) {
		this.bounds = bounds;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	private static double getFormatBounds(String bounds){//得到km为单位的bounds
		String unit;
		double value = 0.1;	//初始化为0.1km
		Pattern pa1 = Pattern.compile("[0-9]+");
		Pattern pa2 = Pattern.compile("[a-z]+");
		Matcher ma1 = pa1.matcher(bounds);
		Matcher ma2 = pa2.matcher(bounds);
		if(ma1.find()){
			value = Double.valueOf(ma1.group());
		}
		if(ma2.find()){
			unit = ma2.group();
			if(unit.equals("m")){
				value = value / 1000;
			}
		}
		return value;
	}
}
