package r3.katharena.lab;

import r3.katharena.lab.device.Device;

public class NetworkInterface {
	
	protected String name;
	protected String ip;
	protected boolean ipv6;
	
	protected Device device;
	protected Link link;
	
	public NetworkInterface(Device device, String name, String ip) {
		System.out.println("Generating interface "+name+" for "+device.getName()+" ("+ip+")");
		this.device = device;
		this.name = name;
		this.ip = ip;
	}
	
	/**********************************************************/
	
	public String getStartupLine() {
		if(name.contains("eth")) {
			if(ipv6)
				return "ifconfig "+name+" up\nifconfig "+name+" add "+ip+"/"+link.getSubnet();
			return "ifconfig "+name+" "+ip+"/"+link.getSubnet()+" up";
		}
		return "ifconfig "+name+" "+ip+" up";
	}
	
	/**********************************************************/
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}
	
	public boolean isIpv6() {
		return ipv6;
	}
	
	public void setIpv6(boolean ipv6) {
		this.ipv6 = ipv6;
	}
	
	@Override
	public int hashCode() {
		return ip.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || obj.getClass() != NetworkInterface.class)
			return false;
		NetworkInterface eth = (NetworkInterface) obj;
		return ip.equals(eth.getIp());
	}

}