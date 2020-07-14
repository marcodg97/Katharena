package r3.katharena.lab.device;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import r3.katharena.lab.NetworkInterface;

public class Client extends Device {

	private String gateway;
	private String resolv;

	public Client(String name, String ip, String gateway) {
		super(name);
		System.out.println("Generating client "+name);
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

	public String getResolv() {
		return resolv;
	}

	public void setResolv(String resolv) {
		this.resolv = resolv;
		System.out.println(name+" has resolv "+this.resolv);
	}
	
	/**********************************************************/
	
	@Override
	public void makeFiles(String labDir) {
		String startupContent = interfaces.get(0).getStartupLine()+"\nroute add default gw "+gateway+" dev eth0";
		for(NetworkInterface eth : interfaces) {
			if(eth.isIpv6()) {
				startupContent = "echo 1 > /proc/sys/net/ipv6/conf/all/forwarding\n\n"+startupContent;
				break;
			}
		}
		
		makeFolder(labDir);
		try {
			Files.write(Paths.get(labDir+name+".startup"), startupContent.getBytes());
			System.out.println(name+".startup");
			if(resolv != null) {
				new File(labDir+name+"/etc").mkdirs();
				Files.write(Paths.get(labDir+name+"/etc/resolv.conf"), ("nameserver "+resolv).getBytes());
				System.out.println(name+"/etc/resolv.conf");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void printInShell() {
		System.out.print("NAME:\t\t"+name+"\nLINK:\t\t"+interfaces.get(0).getLink().getName()+"\nIP:\t\t"+interfaces.get(0).getIp()+"\nGATEWAY:\t"+gateway+(resolv == null ? "\n" : "\nRESOLV:\t\t"+resolv+"\n\n"));
	}
	
	@Override
	public String getConfigRow() {
		String config = "$client("+name+", "+interfaces.get(0).getLink().getName()+", "+interfaces.get(0).getIp()+", "+gateway+")\n";
		if(resolv != null)
			config = config+"$client_resolv("+name+", "+resolv+")\n";
		return config+"\n";
	}

	public static String help() {
		return "constructor:\t\tclient NAME LINK_NAME IP GATEWAY\nset resolv nameserver:\tclient_resolv NAME IP_NS";
	}

}
