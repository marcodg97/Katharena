package r3.katharena.lab;

import java.util.ArrayList;
import java.util.List;

public class Link extends LabElement {
	
	private String name;
	private String net;
	private int subnet;
	private List<NetworkInterface> connectedInterfaces = new ArrayList<>();
	
	public Link(String name, String net) {
		this.name = name;
		this.net = net.substring(0, net.indexOf("/"));
		this.subnet = Integer.parseInt(net.substring(net.indexOf("/")+1));
		System.out.println("Generated link "+name);
	}
	
	/**********************************************************/
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getNet() {
		return net+"/"+subnet;
	}
	
	public void setNet(String net) {
		this.net = net.substring(0, net.indexOf("/"));
		this.subnet = Integer.parseInt(net.substring(net.indexOf("/")));
	}
	
	public int getSubnet() {
		return subnet;
	}
	
	public List<NetworkInterface> getConnectedInterfaces() {
		return connectedInterfaces;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || obj.getClass() != Link.class)
			return false;
		Link link = (Link) obj;
		return name.equals(link.getName());
	}
	
	/**********************************************************/
	
	@Override
	public String getConfigRow() {
		return "$link("+name+", "+net+"/"+subnet+")\n";
	}

	@Override
	public void printInShell() {
		String show = "NAME:\t"+name+"\nNET:\t"+net+"\nSUBNET:\t"+subnet+"\n\n";
		if(connectedInterfaces.isEmpty()) show = show+"No devices connected";
		else if(connectedInterfaces.size() == 1) show = show+"╠"+connectedInterfaces.get(0).getDevice().getName()+"|"+connectedInterfaces.get(0).getName()+" ("+connectedInterfaces.get(0).getIp()+")";
		else {
			show = show+"╔"+connectedInterfaces.get(0).getDevice().getName()+"|"+connectedInterfaces.get(0).getName()+" ("+connectedInterfaces.get(0).getIp()+")";
			int last = connectedInterfaces.size()-1;
			for(int i = 1; i <= last-1; i++) show = show+"\n╠"+connectedInterfaces.get(i).getDevice().getName()+"|"+connectedInterfaces.get(i).getName()+" ("+connectedInterfaces.get(i).getIp()+")";
			show = show+"\n╚"+connectedInterfaces.get(last).getDevice().getName()+"|"+connectedInterfaces.get(last).getName()+" ("+connectedInterfaces.get(last).getIp()+")";
		}
		System.out.println(show);
	}
	
	public static String help() {
		return "constructor:\tlink NAME NET/SUBNET";
	}
	
	/**********************************************************/
	
	public void addInterface(NetworkInterface eth) {
		String[] linkIP = net.split("\\.");
		String[] linkEth = eth.getIp().split("\\.");
		
		if(subnet >= 24 && (
					!linkIP[0].equals(linkEth[0]) ||
					!linkIP[1].equals(linkEth[1]) || 
					!linkIP[2].equals(linkEth[2])
				) ||
		   subnet >= 16 && (
				   !linkIP[0].equals(linkEth[0]) || 
				   !linkIP[1].equals(linkEth[1])
				) ||
		   !linkIP[0].equals(linkEth[0]))
		{	
			System.out.println("ERROR:\tWrong subnet of "+eth.getName()+"|"+eth.getDevice().getName());
			System.exit(3);
		} else {
			connectedInterfaces.add(eth);
			eth.setLink(this);
			System.out.println("Interface "+eth.getName()+" of "+eth.getDevice().getName()+" connected at link "+name);
		}
	}

}
