package r3.katharena.lab;

import r3.katharena.lab.device.Device;

public class Tunnel extends NetworkInterface {
	
	private String destIp;
	private String tunnelIp;
	private String tunnelDestIp;

	public Tunnel(Device device, String name, String ip, String destIp, String tunnelIp, String tunnelDestIp) {
		super(device, name, ip);
		this.destIp = destIp;
		this.tunnelIp = tunnelIp;
		this.tunnelDestIp = tunnelDestIp;
		System.out.println(name+" is a tunnel interface ("+ip+"->"+destIp+" | "+tunnelIp+"->"+tunnelDestIp+")");
	}
	
	/**********************************************************/
	
	@Override
	public String getStartupLine() {
		if(ipv6)
			return "ip tunnel add "+name+" mode sit remote "+destIp+" local "+ip+" ttl 10\nifconfig "+name+" up\nifconfig "+name+" add "+ip+"\nroute -A inet6 add default dev"+name;
		else
			return "ip tunnel add "+name+" mode ipip remote "+destIp+" local "+ip+" ttl 20\nip link set "+name+" up\nip addr add"+tunnelIp+"  peer "+tunnelDestIp+"dev "+name;
	}

	
	/**********************************************************/
	
	public String getDestIp() {
		return destIp;
	}

	public void setDestIp(String destIp) {
		this.destIp = destIp;
	}

	public String getTunnelIp() {
		return tunnelIp;
	}

	public void setTunnelIp(String tunnelIp) {
		this.tunnelIp = tunnelIp;
	}

	public String getTunnelDestIp() {
		return tunnelDestIp;
	}

	public void setTunnelDestIp(String tunnelDestIp) {
		this.tunnelDestIp = tunnelDestIp;
	}

}
