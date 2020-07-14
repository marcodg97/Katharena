package r3.katharena.lab.device;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import r3.katharena.lab.NetworkInterface;

public class Server extends Device {
	
	private String gateway;

	public Server(String name, String ip, String gateway) {
		super(name);
		System.out.println("Generating server "+name);
		this.interfaces.add(new NetworkInterface(this, "eth0", ip));
		this.gateway = gateway;
	}
	
	/**********************************************************/
	
	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
		System.out.println(name+" has gateway "+this.gateway);
	}
	
	/**********************************************************/

	@Override
	public void makeFiles(String labDir) {
		new File(labDir+name+"/var/www/html").mkdirs();
		
		String startupContent = "";
		for(NetworkInterface eth : interfaces) {
			if(eth.isIpv6()) {
				startupContent = "echo 1 > /proc/sys/net/ipv6/conf/all/forwarding\n\n";
				break;
			}
		}
		
		for(NetworkInterface eth : interfaces)
			startupContent = startupContent+eth.getStartupLine()+"\n";
		startupContent = startupContent+"route add default gw "+gateway+" dev eth0\n/etc/init.d/apache2 start";
		
		try {
			Files.write(Paths.get(labDir+name+".startup"), startupContent.getBytes());
			System.out.println(name+".startup");
			Files.write(Paths.get(labDir+name+"/var/www/html/index.html"), ("<html><body><p>Maked with Katharena</p></body></html>").getBytes());
			System.out.println(name+"/var/www/html/index.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void printInShell() {
		System.out.print("NAME:\t"+name+"\nLINK:\t"+interfaces.get(0).getLink().getName()+"\nIP:\t"+interfaces.get(0).getIp()+"\n\n");
	}

	@Override
	public String getConfigRow() {
		return "$server("+name+", "+interfaces.get(0).getLink().getName()+", "+interfaces.get(0).getIp()+", "+gateway+")\n\n";
	}
	
	public static String help() {
		return "constructor:\t\tserver NAME LINK_NAME IP GATEWAY";
	}

}