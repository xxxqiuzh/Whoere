
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientCopy extends Socket{
	private Socket socket = null;
	private String ipAddress;
	private String longitude;
	private String latitude;
	private double bounds;
	private String tags;
	private PrintWriter printWriter;
	
	public ClientCopy(Socket socket){
		this.setSocket(socket);
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
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

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public double getBounds() {
		return bounds;
	}

	public void setBounds(Double bounds) {
		this.bounds = bounds;
	}
	
	public void setBounds(String bounds) {
		this.bounds = getFormatBounds(bounds);
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

	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	public void setPrintWriter(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}

}
