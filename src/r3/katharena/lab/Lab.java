package r3.katharena.lab;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import r3.katharena.lab.device.*;

public class Lab {
	
	private String name;
	
	private Map<String, Device> devices = new LinkedHashMap<>();
	private Map<String, Link> links = new HashMap<>();
	
	private Set<NetworkInterface> usedEth = new HashSet<>();
	
	public Lab(String name) {
		this.name = name;
	}
	
	/**********************************************************/

	public Map<String, Device> getDevices() {
		return devices;
	}

	public String getName() {
		return name;
	}

	public Map<String, Link> getLinks() {
		return links;
	}
	
	/**********************************************************/
	
	private boolean checkInterfaceIP(Device device) {
		for(NetworkInterface eth : device.getInterfaces()) {
			for(NetworkInterface used : usedEth) {
				if(!eth.equals(used) && eth.getIp().equals(used.getIp())) {
					System.out.println("Error: Network interface "+eth.getName()+" of "+device.getName()+" has an already used IP ("+eth.getIp()+")");
					return false;
				}
			}
			usedEth.add(eth);
		}
		return true;
	}
	
	/**********************************************************/
	
	public void addLink(Link link) {
		links.put(link.getName(), link);
	}
	
	public void addDevice(Device device, String linkName) {
		if(links.containsKey(linkName) && checkInterfaceIP(device)) {
			
			if(devices.containsKey(device.getName())) {
				devices.replace(device.getName(), device);
				if(devices.getClass() != Router.class) {
					Link link = links.get(linkName);
					for(int i=link.getConnectedInterfaces().size()-1; i>0; i--) {
						if(link.getConnectedInterfaces().get(i).getDevice().equals(device))
							link.getConnectedInterfaces().remove(i);
					}
					links.replace(linkName, link);
				}
			} else
				devices.put(device.getName(), device);
			
			links.get(linkName).addInterface(device.getInterfaces().get(device.getInterfaces().size()-1));
			if(device.getClass().equals(DNS.class))
				updateDNSZones();
			else if(device.getClass().equals(Router.class) && ((Router)device).isBgp())
				updateLoopbackBGP();
			
		} else
			System.out.println("Error: Wrong link name (link "+linkName+" for "+device.getName()+")");
	}
	
	public void addURL(String deviceName, String URL) {
		if(devices.containsKey(deviceName)) {
			DNS dns;
			String masterZone = (URL.indexOf(".") != URL.length()-1) ? URL.substring(URL.indexOf(".")+1) : ".";
			for(Device device : devices.values()) {
				if(device.getClass().equals(DNS.class)) {
					dns = (DNS)device;
					if(dns.getZone().equals(masterZone)) {
						dns.addMachineInZone(devices.get(deviceName), URL);
						devices.replace(dns.getName(), dns);
					}
				}
			}
		} else
			System.out.println("Error: "+deviceName+" not found");
	}
	
	public void updateDevice(Device device) {
		if(devices.containsKey(device.getName())) {
			devices.replace(device.getName(), device);
			if(device.getClass().equals(DNS.class))
				updateDNSZones();
			else if(device.getClass().equals(Router.class) && ((Router)device).isBgp())
				updateLoopbackBGP();
		}
	}
	
	private void updateDNSZones() {
		DNS dns1;
		DNS dns2;
		String masterZone;
		for(Device d1 : devices.values()) {
			if(d1.getClass().equals(DNS.class) && !((DNS)d1).isRoot()) {
				dns1 = (DNS)d1;
				masterZone = (dns1.getZone().indexOf(".") != dns1.getZone().length()-1) ? dns1.getZone().substring(dns1.getZone().indexOf(".")+1) : ".";
				for(Device d2 : devices.values()) {
					if(d2.getClass().equals(DNS.class)) {
						dns2 = (DNS)d2;
						if(dns2.getZone().equals(masterZone)) {
							dns2.addSubzoneNS(dns1);
							devices.replace(dns2.getName(), dns2);
						}
					}
				}
			}
		}
	}
	
	//Really really awful, must rethink this
	//TODO
	private void updateLoopbackBGP() {
		Router router = null;
		Router loopbackRouter = null;
		for(Device device : devices.values()) {
			if(device.getClass().equals(Router.class)) {
				router = (Router)device;
				if(router.isBgp()) {
					for(NetworkInterface eth : router.getInterfaces()) {
						if(!eth.getName().contains("eth")) {
							for(Device entryloop : devices.values()) {
								if(entryloop.getClass().equals(Router.class) && !entryloop.equals(router) ) {
									loopbackRouter = (Router)entryloop;
									if(loopbackRouter.isBgp() && loopbackRouter.getBgpAS() == router.getBgpAS()) {
										for(NetworkInterface loopEth : loopbackRouter.getInterfaces()) {
											if(!loopEth.getName().contains("eth")) {
												router.getBgpNeighbors().add(loopEth);
												System.out.println(router.getName()+" is an iBGP neighbor of "+loopbackRouter.getName()+" by loopback");
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**********************************************************/
	
	public void make(String directory) {
		System.out.print("\nStart creating lab \""+name+"\"\n");
		Long startTime = new Date().getTime();
		
		new File(directory).mkdir();
		String labconfContent = "";
		for(Device d : devices.values()) {
			for(NetworkInterface eth : d.getInterfaces()) {
				if(eth.getName().contains("eth"))
					labconfContent = labconfContent+d.getName()+"["+eth.getName().substring(3)+"]="+eth.getLink().getName()+"\n";
			}
			d.makeFiles(directory);
		}
		
		try {
			Files.write(Paths.get(directory+"lab.conf"), labconfContent.getBytes());
			System.out.println("lab.conf");
		} catch (IOException e) {
			//TODO
			e.printStackTrace();
		}
		
		System.out.println("Generated successfully in "+(((float)(new Date().getTime() - startTime))/1000)+"s");
	}
	
	public void printInShell() {
		System.out.print("Lab \""+name+"\"\n"+links.size()+" link(s)\n\n");
		for(Link link : links.values())
			link.printInShell();
		System.out.print("\n"+devices.size()+" device(s)\n\n");
		for(Device device : devices.values())
			device.printInShell();
	}
	
	/**********************************************************/
	
	public void wipeDevices() {
		devices.clear();
		usedEth.clear();
		for(Link l : links.values())
			l.getConnectedInterfaces().clear();
		System.out.println("All devices removed from "+name);
	}
	
	public void wipeAll() {
		devices.clear();
		usedEth.clear();
		links.clear();
		System.out.println("Lab "+name+" wiped");
	}

}
