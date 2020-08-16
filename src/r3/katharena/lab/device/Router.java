package r3.katharena.lab.device;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import r3.katharena.lab.NetworkInterface;

class BGPRoute {
	
	private List<String> ips = new ArrayList<>();
	private String neighbor;
	private String direction;
	private String name;
	private String command;
	
	public BGPRoute(String name, String command, String neighbor, String direction) {
		this.name = name;
		this.command = command;
		this.neighbor = neighbor;
		this.direction = direction;
	}
	
	public List<String> getIPs() {return ips;}
	public String getNeighbor() {return neighbor;}
	public String getDirection() {return direction;}
	public String getName() {return name;}
	public String getCommand() {return command;}
}

class BGPPrefix extends BGPRoute {
	private String mask;
	private String filter;
	
	public BGPPrefix(String neighbor, String mask, String filter, String direction) {
		super(null, null, neighbor, direction);
		this.filter = filter;
		this.mask = mask;
	}

	public String getMask() {return mask;}
	public String getFilter() {return filter;}
}

/**********************************************************/
/**********************************************************/

public class Router extends Device {
	
	private boolean rip;
	private boolean ripRedistribute = true;
	private String ripNetwork;
	private List<String> ripRoute;
	
	private boolean ospf;
	private String ospfNetwork;
	private String ospfArea;
	private Map<String, Integer> ospfCost;
	
	private boolean bgp;
	private int bgpAS;
	private boolean ebgpOnly;
	private List<String> bgpNetworks;
	private List<NetworkInterface> bgpNeighbors;
	private List<BGPPrefix> bgpPrefixs;
	private List<BGPRoute> bgpRoutes;
	
	private Map<String, String> staticRouting;

	public Router(String name) {
		super(name);
		System.out.println("Generating router "+name);
		this.staticRouting = new HashMap<>();
		this.ripRoute = new ArrayList<>();
		this.ospfCost = new HashMap<>();
		this.bgpNetworks = new ArrayList<>();
		this.bgpNeighbors = new ArrayList<>();
		this.bgpPrefixs = new ArrayList<>();
		this.bgpRoutes = new ArrayList<>();
	}
	
	/**********************************************************/
	
	public void addStaticRoute(String zone, String ip) {
		if(bgp)
			System.out.println("Error: You're trying to insert a static route on a BGP router ("+name+")");
		else {
			System.out.println("Adding static route to "+name+"\t ("+zone+" reachable by "+ip+")");
			staticRouting.put(zone, ip);
		}
	}
	
	/**********************************************************/
	
	public void setRip(String ripNetwork) {
		this.rip = true;
		this.ripNetwork = ripNetwork;
		System.out.println(name+" set as RIP router with network "+ripNetwork);
	}

	public void addRipRoute(String ripRoute) {
		this.ripRoute.add(ripRoute);
		System.out.println(name+" declare route "+ripRoute);
	}
	
	/**********************************************************/
	
	public void setOspf(String ospfNetwork, String ospfArea) {
		this.ospf = true;
		this.ospfNetwork = ospfNetwork;
		this.ospfArea = ospfArea;
		System.out.println(name+" set as OSPF router with network "+ospfNetwork+" and area "+ospfArea);
	}
	
	public void setOspf(String ospfNetwork) {
		setOspf(ospfNetwork, "0.0.0.0");
	}
	
	public void addOspfCost(String interfaceName, int cost) {
		if(ospf) {
			if(this.ospfCost.containsKey(interfaceName)) {
				System.out.println("Warning: OSPF cost of interface "+interfaceName+" already set to "+ospfCost.get(interfaceName)+" ("+name+")");
			} else {
				boolean present = false;
				for(NetworkInterface eth : interfaces) {
					if(eth.getName().equals(interfaceName)) {
						present = true;
						break;
					}
				}
				if(present) {
					this.ospfCost.put(interfaceName, cost);
					System.out.println("OSPF cost of interface "+interfaceName+" set to "+cost+" ("+name+")");
				} else System.out.println("Error: You're trying to set an OSPF cost of an unknow interface ("+interfaceName+" for "+name+")");
			}
		} else System.out.println("Error: You're trying to set an OSPF cost of a non-OSPF router ("+name+")");
	}
	
	/**********************************************************/
	
	public boolean isBgp() {return bgp;}
	
	public void setBgp(int AS) {
		this.bgp = true;
		this.bgpAS = AS;
		System.out.println("\n"+name+" set as BGP router with AS number "+AS);
		for(NetworkInterface eth : interfaces) {
			if(eth.getName().contains("eth")) {
				for(NetworkInterface ethLink : eth.getLink().getConnectedInterfaces()) {
					if(ethLink.getDevice().getClass().equals(Router.class)) {
						Router other = (Router)ethLink.getDevice();
						if(other.isBgp() && !other.equals(this)) {
							System.out.println(other.getName()+" is an "+((other.getBgpAS() == bgpAS) ? "i":"e"+"")+"BGP neighbor of "+name);
							if(other.getBgpAS() != bgpAS) {
								this.bgpNeighbors.add(ethLink);
								other.getBgpNeighbors().add(eth);
							}
						}
					}
				}
			}
		}
	}
	
	public int getBgpAS() {
		return bgpAS;
	}
	
	public void addBgpNetwork(String network) {
		this.bgpNetworks.add(network);
		System.out.println(name+" has network "+network+" with BGP");
	}
	
	public void setEbgpOnly(boolean ebgpOnly) {
		if(ebgpOnly) {
			if(!bgp) 
				System.out.println("Warning: You're trying to set "+name+" to redistribute eBGP, but BGP is not set");
			else if(!rip && !ospf)
				System.out.println("Warning: You're trying to set "+name+" to redistribute eBGP, but RIP or OSPF is not set");
			else {
				System.out.println(name+" redistribute only eBGP");
				this.ebgpOnly = true;
			}
		} else ebgpOnly = false;
	}
	
	public void addBgpPrepend(String prepend, String neighbor, String direction) {
		if(bgp) {
			this.bgpRoutes.add(new BGPRoute("prepend", "set as-path "+prepend, neighbor, direction.equals("out")?"out":"in"));
			System.out.println("AS "+bgpAS+" insert prepending "+prepend+" for neighbor "+neighbor+" ("+direction+")");
		} else
			System.out.println("Warning: You're trying to set prepending to a non-BGP router ("+name+")");
	}
	
	public void addBgpPrepend(String prepend, String[] values, String direction) {
		if(bgp) {
			BGPRoute prepending = new BGPRoute("prepend", "set as-path "+prepend, values[1], direction.equals("out")?"out":"in");
			for(int i=2; i<values.length-1; i++) {
				prepending.getIPs().add(values[i]);
				System.out.println("AS "+bgpAS+" insert prepending "+prepend+" for neighbor "+values[i]+" ("+direction+")");
			}
		} else System.out.println("Warning: Try to set prepending to a non-BGP router ("+name+")");
	}
	
	public void addBgpPreference(String bgpPreference, String direction) {
		if(bgp) {
			this.bgpRoutes.add(new BGPRoute("preference", "set local-preference 150", bgpPreference, direction.equals("out")?"out":"in"));
			System.out.println("AS "+bgpAS+(direction.equals("in")?"prefers":"avoid")+" sending traffic to "+bgpPreference+"with local-preference (route-map set on "+name+")");
		} else
			System.out.println("Warning: Try to set prepending to a non BGP-router ("+name+")");
	}
	
	public void addBgpPreference(String[] values, String direction) {
		if(bgp) {
			BGPRoute prefs = new BGPRoute("preference", "set local-preference 150", values[1], direction.equals("out")?"out":"in");
			for(int i=2; i<values.length; i++) {
				prefs.getIPs().add(values[i]);
				System.out.println("AS "+bgpAS+(direction.equals("in")?"prefers":"avoid")+" sending traffic for "+values[i]+" to "+values[1]+" (route-map set on "+name+")");
			}
			this.bgpRoutes.add(prefs);
		} else System.out.println("Warning: Try to set prepending to a non-BGP router ("+name+")");
	}
	
	public List<NetworkInterface> getBgpNeighbors() {
		return bgpNeighbors;
	}
	
	public void addBgpPrefix(String neighbor, String mask, String filter, String direction) {
		if(bgp) {
			if((direction.equals("in") || direction.equals("out")) && (filter.equals("permit") || filter.equals("deny"))) {
				boolean done = false;
				for(NetworkInterface eth : bgpNeighbors) {
					if(eth.getIp().equals(neighbor)) {
						bgpPrefixs.add(new BGPPrefix(neighbor, mask, filter, direction));
						System.out.println(name+" "+(direction.equals("in")?filter:"declare only")+" network "+mask+" to neighbor "+neighbor);
						done = true;
						break;
					}
				}
				if(!done)
					System.out.println("Warning: Wrong neighbor IP for prefix-list ("+name+")");
			} else System.out.println("Warning: Prefix-list not valid, check the syntax ("+name+")");
		} else System.out.println("Warning: You're trying to set prepending to a non BGP router ("+name+")");
	}
	
	/**********************************************************/

	@Override
	public String getConfigRow() {		
		boolean ipv6 = false;
		for(NetworkInterface eth : interfaces) {
			if(eth.isIpv6()) {
				ipv6 = true;
				break;
			}
		}
		
		String config = "";
		for(NetworkInterface eth : interfaces) config = config+"$router("+name+", "+(eth.getLink()!=null?(eth.getLink().getName()+", "):"")+eth.getIp()+"|"+eth.getName()+")\n";
		for(Map.Entry<String, String> entry : staticRouting.entrySet()) config = config+"$static_route("+name+", "+entry.getKey()+", "+entry.getValue()+")\n";
		
		if(rip) {
			config = config+"$rip("+name+", "+ripNetwork;
			for(String route : ripRoute) config = config+", "+route;
			config = config+")\n";
		}
		
		if(ospf) {
			config = config+"$ospf("+name+", "+ospfNetwork+", "+ospfArea+")\n";
			for(Map.Entry<String, Integer> entry : ospfCost.entrySet()) config = config+"$ospf_cost("+name+", "+entry.getKey()+", "+entry.getValue()+")\n";
		}
		
		if(bgp) {
			config = config+"$bgp("+name+", "+bgpAS;
			for(String network : bgpNetworks) config = config+", "+network;
			config = config+")\n";
			for(BGPPrefix prefix : bgpPrefixs) config = config+"$bgp_prefix("+name+", "+prefix.getNeighbor()+", "+prefix.getFilter()+", "+prefix.getMask()+", "+prefix.getDirection()+")\n";
			if(ebgpOnly) config = config+"$bgp_only_ebgp_reds("+name+")\n";
			for(BGPRoute route : bgpRoutes) {
				
				//TODO
				if(route.getName().equals("preference")) {
					config = config+"$bgp_preference_"+route.getDirection()+"("+name;
					for(String ip : route.getIPs()) config = config+", "+ip;
				} else if(route.getName().equals("prepend")) {
					config = config+"$bgp_prepending_"+route.getDirection()+"("+name;
					for(String ip : route.getIPs()) config = config+", "+ip;
					config = config+", "+route.getCommand().substring(11);
				}
				config = config+")\n";
			}
		}
		
		if(ipv6) config = config+"$ipv6("+name+")\n";
		
		return config+"\n";
	}

	@Override
	public void printInShell() {
		String show = "NAME:\t\t"+name+(rip||ospf||bgp ? "\nDAEMONS:\t"+(rip?"RIP ":"")+(ospf?"OSPF ":"")+(bgp?"BGP":"") : "")+"\nINTERFACES:";
		for(NetworkInterface eth : interfaces) show = show+"\n\t"+eth.getName()+" ("+eth.getIp()+(eth.getLink()!=null?(" on link "+eth.getLink().getName()):" loopback")+")"+(ospfCost.containsKey(eth.getName())?(" with OSPF cost "+ospfCost.get(eth.getName())):"");
		
		if(!staticRouting.isEmpty()) {
			show = show+"\nSTATIC ROUTING:\n";
			for(Map.Entry<String, String> entry : staticRouting.entrySet()) show = show+"\t"+entry.getKey()+" reachable by "+entry.getValue();
		}
		
		if(rip) {
			show = show+"\nRIP NETWORK:\t"+ripNetwork;
			if(!ripRoute.isEmpty()) {
				show = show+"\nRIP ROUTE:\t";
				for(String route : ripRoute) show = show+route+" ";
			}
		}
		
		if(ospf) show = show+"\nOSPF NETWORK:\t"+ospfNetwork+"\nOSPF AREA:\t"+ospfArea;
		
		if(bgp) {
			show = show+"\n#AS:\t\t"+bgpAS+"\nBGP NETWORKS:";
			for(String network : bgpNetworks) show = show+"\n\t"+network;
			
			show = show+"\nBGP NEIGHBORS:\n";
			for(NetworkInterface eth : bgpNeighbors) show = show+"\t"+eth.getDevice().getName()+" on link "+eth.getLink().getName()+" ("+eth.getName()+")\n";
			
			if(!bgpRoutes.isEmpty()) {
				show = show+"\nBGP ROUTE MAPS:\t";
				for(BGPRoute route : bgpRoutes) show = show+"\n"+route.getNeighbor()+" "+route.getCommand()+" "+route.getDirection()+"\n";
			}
		}
		
		System.out.println(show);
	}

	@Override
	public void makeFiles(String labDir) {		
		boolean ipv6 = false;
		for(NetworkInterface eth : interfaces) {
			if(eth.isIpv6()) {
				ipv6 = true;
				break;
			}
		}
		
		makeFolder(labDir);
		String startupContent = ipv6 ? "echo 1 > /proc/sys/net/ipv6/conf/all/forwarding\n\n" : "";
		
		for(NetworkInterface eth : interfaces)
			startupContent = startupContent+eth.getStartupLine()+"\n";
		
		if(!bgp) {
			for(Map.Entry<String, String> staticRoute : staticRouting.entrySet()) {
				String[] gw = staticRoute.getValue().split("\\.");
				startupContent = startupContent+"\n";
				for(NetworkInterface eth : interfaces) {
					int subnet = eth.getLink().getSubnet();
				
					if(subnet >= 24 && eth.getLink().getNet().contains(gw[0]+"."+gw[1]+"."+gw[2]) ||
					   subnet >= 16 && subnet < 24 && eth.getLink().getNet().contains(gw[0]+"."+gw[1]) ||
					   subnet < 16 && eth.getLink().getNet().contains(gw[0]))
					{	
						startupContent = startupContent+(ipv6 ? "route -A inet6 add ":"route add -net ")+staticRoute.getKey()+" gw "+staticRoute.getValue()+" dev "+eth.getName();
						break;
					}
				}
			}
		}
		
		if(rip || ospf || bgp) {
			startupContent = startupContent+"/etc/init.d/quagga start";
			String zebraContent = "hostname zebra\npassword zebra\nenable password zebra";
			String daemonsContent = "zebra=yes";
			String ripdContent = zebraContent;
			String ospfContent = "";
			String bgpdContent = zebraContent;
			
			String loopbackIp = "";
			for(NetworkInterface eth : interfaces) {
				if(!eth.getName().contains("eth")) {
					loopbackIp = eth.getIp().substring(0, eth.getIp().indexOf("/"));
					break;
				}
			}
			
			if(bgp) {
				daemonsContent = daemonsContent+"\nbgpd=yes";
				bgpdContent = bgpdContent+"\n\nrouter bgp "+bgpAS+"\n\n";
				if(bgpNetworks.isEmpty()) {
					for(NetworkInterface eth : interfaces)
						if(eth.getName().contains("eth")) bgpdContent = bgpdContent+"network "+eth.getLink().getNet()+"\n";
				} else {
					for(String net : bgpNetworks) bgpdContent = bgpdContent+"network "+net+"\n";
				}
				bgpdContent = bgpdContent+"\n";
				
				//neighbor settings
				for(NetworkInterface neighbor : bgpNeighbors) {
					if(!neighbor.getName().contains("eth")) {
						bgpdContent = bgpdContent+"neighbor "+neighbor.getIp().substring(0,neighbor.getIp().indexOf("/"))+" remote-as "+((Router)neighbor.getDevice()).getBgpAS()+"\n";
						bgpdContent = bgpdContent+"neighbor "+neighbor.getIp().substring(0,neighbor.getIp().indexOf("/"))+" update-source "+loopbackIp+"\n";
					} else bgpdContent = bgpdContent+"neighbor "+neighbor.getIp()+" remote-as "+((Router)neighbor.getDevice()).getBgpAS()+"\n";
				}
				
				for(BGPPrefix prefix : bgpPrefixs) bgpdContent = bgpdContent+"neighbor "+prefix.getNeighbor()+" prefix-list partial"+prefix.getDirection()+" "+prefix.getDirection()+"\n";
				
				for(int i=0; i<bgpRoutes.size(); i++) bgpdContent = bgpdContent+"neighbor "+bgpRoutes.get(i).getNeighbor()+" route-map "+bgpRoutes.get(i).getName()+i+" "+bgpRoutes.get(i).getDirection()+"\n";
				
				bgpdContent = bgpdContent+"\n";
				if(!bgpPrefixs.isEmpty()) {
					boolean denyIn = false;
					boolean denyOut = false;
					for(BGPPrefix prefix : bgpPrefixs) {
						bgpdContent = bgpdContent+"ip prefix-list partial"+prefix.getDirection()+" "+prefix.getFilter()+" "+prefix.getMask()+"\n";
						if(prefix.getFilter().equals("deny")) {
							if(prefix.getDirection().equals("in") && !denyIn) denyIn = true;
							else if(prefix.getDirection().equals("out") && !denyOut) denyOut = true;
						}
					}
					if(denyIn) bgpdContent = bgpdContent+"ip prefix-list partialin permit any\n";
					if(denyOut) bgpdContent = bgpdContent+"ip prefix-list partialout permit any\n";
				}
				
				if(bgpRoutes.size() == 1) bgpdContent = bgpdContent+"route-map "+bgpRoutes.get(0).getName()+"0 permit 10\n"+bgpRoutes.get(0).getCommand();
				else {
					boolean matchip = false;
					for(int i=0; i<bgpRoutes.size(); i++) {
						bgpdContent = bgpdContent+"route-map "+bgpRoutes.get(i).getName()+i+" permit 10\n";
						if(!bgpRoutes.get(i).getIPs().isEmpty()) {
							matchip = true;
							bgpdContent = bgpdContent+"match ip address addr"+i+"\n";
						}
						bgpdContent = bgpdContent+bgpRoutes.get(i).getCommand()+"\nroute-map "+bgpRoutes.get(i).getName()+i+" permit 20\n\n";
					}
					if(matchip) {
						for(int j=0; j<bgpRoutes.size(); j++) {
							for(String ip : bgpRoutes.get(j).getIPs()) bgpdContent = bgpdContent+"access-list addr"+j+" permit "+ip+"\n";
						}
					}
				}
				
			}
			
			if(ospf) {
				daemonsContent = daemonsContent+"\nospfd=yes";
				for(Map.Entry<String, Integer> entry : ospfCost.entrySet()) ospfContent = ospfContent+"\ninterface "+entry.getKey()+"\nospf cost "+entry.getValue();
				ospfContent = ospfContent+"\n\nrouter ospf\n\n\nnetwork "+ospfNetwork+" area "+ospfArea+"\n\nredistribute connected";
				if(!staticRouting.isEmpty()) ospfContent = ospfContent+"\nredistribute kernel";
				if(rip) ospfContent = ospfContent+"\nredistribute rip";
				if(bgp) {
					ospfContent = ospfContent+"\nredistribute bgp";
					if(ebgpOnly) {
						ospfContent = ospfContent+" route-map eBGPonly\n\nroute-map eBGPonly permit 10\nmatch ip next-hop prefix-list eBGPnexthops\n\n";
						for(NetworkInterface eth : bgpNeighbors) {
							if(!eth.getIp().contains("/")) ospfContent = ospfContent+"ip prefix-list eBGPnexthops permit "+eth.getIp()+"/32\n";
						}
					}
				}
			}
			
			if(rip) {
				daemonsContent = daemonsContent+"\nripd=yes";
				ripdContent = ripdContent+"\n\nrouter rip\nnetwork "+ripNetwork+"\n";
				if(ripRedistribute) ripdContent = ripdContent+"\nredistribute connected";
				if(ospf) ripdContent = ripdContent+"\nredistribute ospf";
				if(bgp) {
					ripdContent = ripdContent+"\nredistribute bgp";
					if(ebgpOnly) {
						ripdContent = ripdContent+" route-map eBGPonly\n\nroute-map eBGPonly permit 10\nmatch ip next-hop prefix-list eBGPnexthops\n\n";
						for(NetworkInterface eth : bgpNeighbors) {
							if(!eth.getIp().contains("/")) ripdContent = ripdContent+"ip prefix-list eBGPnexthops permit "+eth.getIp()+"/32\n";
						}
					}
				}
				for(String route : ripRoute) ripdContent = ripdContent+"\nroute "+route;
			}
			
			new File(labDir+name+"/etc/quagga").mkdirs();
			
			try {
				Files.write(Paths.get(labDir+name+"/etc/quagga/daemons"), daemonsContent.getBytes());
				System.out.println(name+"/etc/quagga/daemons");
				Files.write(Paths.get(labDir+name+"/etc/quagga/zebra.conf"), zebraContent.getBytes());
				System.out.println(name+"/etc/quagga/zebra.conf");
				if(rip) {
					Files.write(Paths.get(labDir+name+"/etc/quagga/ripd.conf"), ripdContent.getBytes());
					System.out.println(name+"/etc/quagga/ripd.conf");
				}
				if(ospf) {
					Files.write(Paths.get(labDir+name+"/etc/quagga/ospfd.conf"), ospfContent.getBytes());
					System.out.println(name+"/etc/quagga/ospfd.conf");
				}
				if(bgp) {
					Files.write(Paths.get(labDir+name+"/etc/quagga/bgpd.conf"), bgpdContent.getBytes());
					System.out.println(name+"/etc/quagga/bgpd.conf");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Files.write(Paths.get(labDir+name+".startup"), startupContent.getBytes());
			System.out.println(name+".startup");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static String help() {
		return "constructor/insert new interface:\trouter NAME LINK_NAME IP|INTERFACE_NAME\t\t/\trouter NAME IP/SUBNET|INTERFACE_NAME (if loopback)\nadd static route:\t\t\tstatic_route NAME ZONE GATEWAY\nenable RIP:\t\t\t\trip NAME NETWORK {[ROUTER]}\n"
				+ "enable OSPF:\t\t\t\tospf NAME NETWORK {AREA}\nenable BGP:\t\t\t\tbgp NAME #AS {[NETWORK]}\n"
				+ "add bgp route prepending:\t\tbpg_preference_in/bgp_preference_out NAME {NEIGHBOR_IP} [{IP MASK}] AS-PATH\nadd bgp route preference:\t\tbgp_preference_in/bgp_preference_out NAME {NEIGHBOR_IP} [{IP MASK}] \nset only eBGP redistribute:\t\tbgp_only_ebgp_reds [NAME]\n"
				+ "set raw prefix BGP:\t\t\tbgp_prefix NAME NEIGHBOR_IP permit/deny MASK in/out";
	}

}