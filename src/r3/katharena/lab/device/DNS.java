package r3.katharena.lab.device;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import r3.katharena.lab.NetworkInterface;

public class DNS extends Device {
	
	private String gateway;
	
	private String zone;
	private String rootIP;
	private List<DNS> subzonesNS = new ArrayList<>();
	private Map<String, NetworkInterface> interfacesInZone = new HashMap<>();
	
	public DNS(String name, String ip, String zone) {
		super(name);
		System.out.println("Generating DNS server "+name+" with authority zone "+zone);
		this.interfaces.add(new NetworkInterface(this, "eth0", ip));
		this.zone = zone;
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
	
	public boolean isRoot() {
		return zone.equals(".");
	}
	
	public String getRootIP() {
		return rootIP != null ? rootIP : interfaces.get(0).getIp();
	}
	
	public void setRootNS(Device rootNS) {
		if(!zone.equals(".")) {
			this.rootIP = rootNS.getInterfaces().get(0).getIp();
			System.out.println(name+" has DNS root IP set as "+rootIP+" ("+rootNS.getName()+")");
		} else 
			System.out.println("Error: Can't set a NS root to a NS root ("+name+")");
	}
	
	public void setRootIP(String rootIP) {
		if(!zone.equals(".")) {
			this.rootIP = rootIP;
			System.out.println(name+" has DNS root IP set as "+rootIP);
		} else 
			System.out.println("Error: Can't set a NS root to a NS root ("+name+")");
	}
	
	public String getZone() {
		return zone;
	}
	
	public void setZone(String zone) {
		this.zone = zone;
	}

	public List<DNS> getSubzonesNS() {
		return subzonesNS;
	}
	
	public void addSubzoneNS(DNS nameserver) {
		if(nameserver.getZone().contains(zone)) {
			subzonesNS.add(nameserver);
			System.out.println(nameserver.getZone()+" (NS "+nameserver.getName()+") is a subzone of "+zone+" (NS "+name+")");
		} else
			System.out.println("Error: Wrong DNS topology ("+nameserver.getZone()+" can't be a subzone of "+zone+")");
	}
	
	public Map<String, NetworkInterface> getInterfacesInZone() {
		return interfacesInZone;
	}
	
	public void addMachineInZone(Device machine, String name) {
		if(name.contains(zone)) {
			interfacesInZone.put(name, machine.getInterfaces().get(0));
			System.out.println(machine.getName()+" has URL name "+name+" (NS authority "+this.name+")");
		} else
			System.out.println("Error: "+name+"is not authority for "+name+" ("+machine.getName()+")");
	}
	
	/**********************************************************/

	@Override
	public String getConfigRow() {
		String config = isRoot() ? ("$root_dns("+name+", "+interfaces.get(0).getLink().getName()+", "+interfaces.get(0).getIp()+")\n") : ("$dns("+name+", "+interfaces.get(0).getLink().getName()+", "+interfaces.get(0).getIp()+", "+zone+", "+rootIP+", "+gateway+")\n");
		for(Map.Entry<String, NetworkInterface> entry : interfacesInZone.entrySet())
			config = config+"$set_url("+entry.getKey()+", "+entry.getValue().getDevice().getName()+")\n";
		return config;
	}

	@Override
	public void printInShell() {
		String show = "NAME:\t\t"+name+"\nLINK:\t\t"+interfaces.get(0).getLink().getName()+"\nIP:\t\t"+interfaces.get(0).getIp()+"\nZONE:\t\t"+zone+"\nDNS TABLE:\n";
		if(rootIP != null)
			show = show+"\t.\tIN\tNS\tROOT-SERVER.\n\tROOT-SERVER.\tIN\tA\t"+rootIP+"\n\t@\tIN\tNS\t"+name+"."+zone+"\n\t"+name+"."+zone+"\tIN\tA\t"+interfaces.get(0).getIp()+"\n";
		else
			show = show+"\t@\tIN\tNS\tROOT-SERVER.\n\tROOT-SERVER.\tIN\tA\t"+interfaces.get(0).getIp()+"\n";
		for(DNS ns : subzonesNS)
			show = show+"\t"+ns.getZone()+"\tIN\tNS\t"+ns.getName()+"."+ns.getZone()+"\n\t"+ns.getName()+"."+ns.getZone()+"\tIN\tA\t"+ns.getInterfaces().get(0).getIp()+"\n";
		for(Map.Entry<String, NetworkInterface> entry : interfacesInZone.entrySet())
			show = show+"\t"+entry.getKey()+"\tIN\tA\t"+entry.getValue().getIp()+"\n";
		
		System.out.println(show);
	}

	@Override
	public void makeFiles(String labDir) {
		makeFolder(labDir);
		new File(labDir+name+"/etc/bind").mkdirs();
		
		boolean ipv6 = false;
		for(NetworkInterface eth : interfaces) {
			if(eth.isIpv6()) {
				ipv6 = true;
				break;
			}
		}
		
		try {
			Files.write(Paths.get(labDir+name+".startup"), ((ipv6 ? "echo 1 > /proc/sys/net/ipv6/conf/all/forwarding\n\n" : "")+interfaces.get(0).getStartupLine()+"\n"+(gateway!=null?"route add default gw "+gateway+" dev eth0":"")+"\n/etc/init.d/bind start").getBytes());
			System.out.println(name+".startup");
			String time = ""+LocalDate.now().getYear()+(LocalDate.now().getMonthValue()>9?"":"0")+LocalDate.now().getMonthValue()+""+(LocalDate.now().getDayOfMonth()>9?"":"0")+LocalDate.now().getDayOfMonth()+"01";
			if(isRoot()) {
				Files.write(Paths.get(labDir+name+"/etc/bind/named.conf"), ("zone \".\" {\n\ttype master;\n\tfile \"/etc/bind/db.root\";\n};").getBytes());
				System.out.println(name+"/etc/bind/named.conf");
				
				String rootTable = "$TTL\t60000\n@\t\t\t\tIN\t\tSOA\tROOT-SERVER.\troot.ROOT-SERVER. (\n\t\t\t\t\t\t"+
							time+" ; serial\n\t\t\t\t\t\t28800 ; refresh\n\t\t\t\t\t\t14400 ; retry\n\t\t\t\t\t\t3600000 ; expire\n\t\t\t\t\t\t0 ; negative cache ttl\n\t\t\t\t\t\t)\n\n"+
							"@\t\t\tIN\t\tNS\t\tROOT-SERVER.\nROOT-SERVER.\tIN\tA\t"+interfaces.get(0).getIp()+"\n\n";
				for(DNS ns : subzonesNS)
					rootTable = rootTable+ns.getZone()+"\t\t\tIN\tNS\t"+ns.getName()+"."+ns.getZone()+"\n\t"+ns.getName()+"."+ns.getZone()+"\tIN\tA\t"+ns.getInterfaces().get(0).getIp()+"\n";
				for(Map.Entry<String, NetworkInterface> entry : interfacesInZone.entrySet())
					rootTable = rootTable+entry.getKey()+"\tIN\tA\t"+entry.getValue().getIp()+"\n";
				Files.write(Paths.get(labDir+name+"/etc/bind/db.root"),rootTable.getBytes());
				System.out.println(name+"/etc/bind/db.root");
			} else {
				String dbExt = "";
				List<String> dbSplittedExt = Arrays.asList(zone.substring(0,zone.length()-1).split("\\."));
				Collections.reverse(dbSplittedExt);
				for(String p : dbSplittedExt)
					dbExt = dbExt+p+".";
				dbExt = dbExt.substring(0, dbExt.length()-1);
				
				Files.write(Paths.get(labDir+name+"/etc/bind/named.conf"), ("zone \".\" {\n\ttype hint;\n\tfile \"/etc/bind/db.root\";\n};\n\nzone \""+zone.substring(0,zone.length()-1)+"\" {\n\ttype master;\n\tfile \"/etc/bind/db."+dbExt+"\";\n};").getBytes());
				System.out.println(name+"/etc/bind/named.conf");
				Files.write(Paths.get(labDir+name+"/etc/bind/db.root"), (".\t\t\t\t\tIN\tNS\tROOT-SERVER.\nROOT-SERVER.\t\tIN\tA\t"+rootIP).getBytes());
				System.out.println(name+"/etc/bind/db.root");
				
				String table = "$TTL\t60000\n@\t\t\t\tIN\t\tSOA\t"+zone+"\troot."+zone+"(\n\t\t\t\t\t\t"+
								time+" ; serial\n\t\t\t\t\t\t28800 ; refresh\n\t\t\t\t\t\t14400 ; retry\n\t\t\t\t\t\t3600000 ; expire\n\t\t\t\t\t\t0 ; negative cache ttl\n\t\t\t\t\t\t)\n\n"+
								"@\t\t\tIN\t\tNS\t\t"+name+"."+zone+"\n"+name+"."+zone+"\tIN\tA\t"+interfaces.get(0).getIp()+"\n\n";
				for(DNS ns : subzonesNS)
					table = table+ns.getZone()+"\t\t\tIN\tNS\t"+ns.getName()+"."+ns.getZone()+"\n"+ns.getName()+"."+ns.getZone()+"\tIN\tA\t"+ns.getInterfaces().get(0).getIp()+"\n";
				for(Map.Entry<String, NetworkInterface> entry : interfacesInZone.entrySet())
					table = table+entry.getKey()+"\tIN\tA\t"+entry.getValue().getIp()+"\n";
				
				Files.write(Paths.get(labDir+name+"/etc/bind/db."+dbExt), table.getBytes());
				System.out.println(name+"/etc/bind/db."+dbExt);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String help() {
		return "constructor (root NS):\troot_dns NAME LINK_NAME IP GATEWAY\nconstructor:\t\tdns NAME LINK_NAME IP ZONE ROOT_DNS_NAME/ROOT_DNS/IP GATEWAY\nset machine URL:\tset_url NAME URL\n";
	}

}
