package r3.katharena.command;

import r3.katharena.LabPool;
import r3.katharena.lab.*;
import r3.katharena.lab.device.*;

public class CommandHandle {
	
	public static void exec(String command, String[] parameters) {
		
		switch(command) {
		//HELP COMMANDS
		case "help":
			if(parameters != null) {
				String element =  parameters[0].toLowerCase();
				switch(element) {
				case "client":
					System.out.println(Client.help());
					break;
				case "server":
					System.out.println(Server.help());
					break;
				case "router":
					System.out.println(Router.help());
					break;
				case "dns":
					System.out.println(DNS.help());
					break;
				case "link":
					System.out.println(Link.help());
					break;
					
				default:
					System.out.println(parameters[0]+" not recognized");
				}
			} else
				System.out.println("Please specify an element (DNS, Router, ...)");
			break;
		
		//LAB COMMANDS
		case "print":
			LabPool.getCurrentLab().printInShell();
			break;
		case "switchto":
			if(parameters != null && parameters.length > 0)
				LabPool.setCurrentLab(parameters[0]);
			else
				System.out.println("Error: insert at least one parameter");
			break;
		case "make":
			if(parameters != null && parameters.length > 0)
				LabPool.getCurrentLab().make((parameters[0].charAt(parameters[0].length()-1) == '/' ? parameters[0] : parameters[0]+"/"));
			else
				System.out.println("Error: insert at least one parameter");
			break;
		case "save":
			if(parameters != null && parameters.length > 0) {
				if(!parameters[0].contains("."))
					parameters[0] = parameters[0]+".kat";
				LabFileHandle.saveLab(parameters[0]);
			} else
				LabFileHandle.saveLab(LabPool.getCurrentLabName()+".kat");
			break;
		case "load":
			if(parameters != null && parameters.length > 0) {
				if(parameters.length == 2)
					LabPool.setCurrentLab(parameters[1]);
				LabFileHandle.loadLab(parameters[0]);
			} else
				System.out.println("Error: insert at least one parameter");
			break;
		case "wipe":
			if(parameters != null) {
				if(parameters[0].toLowerCase().equals("devices"))
					LabPool.getCurrentLab().wipeDevices();
				else if(parameters[0].toLowerCase().equals("all"))
					LabPool.getCurrentLab().wipeAll();
				else
					System.out.println("Error: parameter not valid (devices/all)");
			}else
				System.out.println("Error: insert one parameter (devices/all)");
			break;
			
		//LINK COMMANDS
		case "link":
			if(parameters != null && parameters.length >= 2)
				LabPool.getCurrentLab().addLink(new Link(parameters[0], parameters[1]));
			else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
			
		//TUNNEL COMMANDS
		case "tunnel":
			if(parameters != null && parameters.length >= 6) {
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
					Device device = LabPool.getCurrentLab().getDevices().get(parameters[0]);
					Tunnel tunnel = new Tunnel(device, parameters[1], parameters[2], parameters[3], parameters[4], parameters[5]);
					device.addInterface(tunnel);
					LabPool.getCurrentLab().updateDevice(device);
				} else
					System.out.println("Error: device "+parameters[0]+" not found");
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
			
		//CLIENT COMMANDS
		case "client":
			if(parameters != null && parameters.length == 4)
				LabPool.getCurrentLab().addDevice(new Client(parameters[0], parameters[2], parameters[3]), parameters[1]);
			else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "client_resolv":
			if(parameters != null && parameters.length == 2) {
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
					Client client = (Client)LabPool.getCurrentLab().getDevices().get(parameters[0]);
					client.setResolv(parameters[1]);
					LabPool.getCurrentLab().getDevices().replace(parameters[0], client);
				} else
					System.out.println("Error: client "+parameters[0]+" not found");
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
			
		//SERVER COMMANDS
		case "server":
			if(parameters != null && parameters.length == 4)
				LabPool.getCurrentLab().addDevice(new Server(parameters[0], parameters[2], parameters[3]), parameters[1]);
			else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
			
		//ROUTER COMMANDS
		case "router":
			if(parameters != null && parameters.length > 0) {
				Router router;
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0]))
					router = (Router)LabPool.getCurrentLab().getDevices().get(parameters[0]);
				else
					router = new Router(parameters[0]);
				if(parameters.length == 2 && !parameters[1].contains("eth")) {
					if(parameters[1].contains("/"))
						router.addInterface(new NetworkInterface(router, parameters[1].substring(parameters[1].indexOf("|")+1), parameters[1].substring(0, parameters[1].indexOf("|"))));
					else
						System.out.println("Error: Subnet not specified on a loopback interface ("+parameters[0]+")");
				} else if(parameters.length > 2 && parameters[2].contains("eth")) {
					router.addInterface(new NetworkInterface(router, parameters[2].substring(parameters[2].indexOf("|")+1), parameters[2].substring(0, parameters[2].indexOf("|"))));
				}
				LabPool.getCurrentLab().addDevice(router, parameters[1]);
			} else
				System.out.println("Error: insert at least one parameter");
			break;
		case "static_route":
			if(parameters != null && parameters.length >= 3) {
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
					Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
					if(LabPool.getCurrentLab().getLinks().containsKey(parameters[1]))
						parameters[1] = LabPool.getCurrentLab().getLinks().get(parameters[1]).getNet();
					router.addStaticRoute(parameters[1], parameters[2]);
					LabPool.getCurrentLab().updateDevice(router);
				} else
					System.out.println("Error: router "+parameters[0]+" not found");
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "rip":
			if(parameters != null && parameters.length > 1) {
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
					Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
					router.setRip(parameters[1]);
					if(parameters.length > 2) {
						for(int i=2; i<parameters.length; i++)
							router.addRipRoute(parameters[i]);
					}
					LabPool.getCurrentLab().updateDevice(router);
				} else
					System.out.println("Error: router "+parameters[0]+" not found");
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "ospf":
			if(parameters != null && parameters.length == 2 || parameters.length == 3) {
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
					Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
					router.setOspf(parameters[1], parameters.length == 3 ? parameters[2]:"0.0.0.0");
					LabPool.getCurrentLab().updateDevice(router);
				} else
					System.out.println("Error: You're trying to set OSPF an unknow router ("+parameters[0]+")");
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "ospf_cost":
			if(parameters != null && parameters.length == 3) {
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
					Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
					router.addOspfCost(parameters[1], Integer.parseInt(parameters[2]));
					LabPool.getCurrentLab().updateDevice(router);
				} else
					System.out.println("Error: You're trying to set OSPF cost an unknow router ("+parameters[0]+")");
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "bgp":
			if(parameters != null && parameters.length >= 2) {
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
					Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
					router.setBgp(Integer.parseInt(parameters[1]));
					if(parameters.length > 2) {
						for(int i=2; i<parameters.length; i++)
							router.addBgpNetwork(parameters[i]);
					}
					LabPool.getCurrentLab().updateDevice(router);
				} else
					System.out.println("Error: You're trying to set BGP an unknow router ("+parameters[0]+")");
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "bgp_prefix":
			if(parameters != null && parameters.length == 5) {
				if(LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
					Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
					router.addBgpPrefix(parameters[1], parameters[3], parameters[2], parameters[4]);
					LabPool.getCurrentLab().updateDevice(router);
				} else
					System.out.println("Error: You're trying to set a BGP prefix to an unknow router ("+parameters[0]+")");
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "bgp_only_ebgp_reds":
			if(parameters != null && LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
				Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
				router.setEbgpOnly(true);
				LabPool.getCurrentLab().updateDevice(router);
			} else
				System.out.println("Error: You're trying to set eBGP to an unknow router ("+parameters[0]+")");
			break;
		case "bgp_preference_in":
			if(parameters != null && LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
				Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
				if(parameters.length == 2)
					router.addBgpPreference(parameters[1], "in");
				else
					router.addBgpPreference(parameters, "in");
				LabPool.getCurrentLab().updateDevice(router);
			} else
				System.out.println("Error: You're trying to set a BGP preference to an unknow router ("+parameters[0]+")");
			break;
		case "bgp_preference_out":
			if(parameters != null && LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
				Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
				if(parameters.length == 2)
					router.addBgpPreference(parameters[1], "out");
				else
					router.addBgpPreference(parameters, "out");
				LabPool.getCurrentLab().updateDevice(router);
			} else
				System.out.println("Error: You're trying to set a BGP preference to an unknow router ("+parameters[0]+")");
			break;
		case "bgp_prepending_in":
			if(parameters != null && LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
				Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
				if(parameters.length == 3)
					router.addBgpPrepend(parameters[2], parameters[1], "in");
				else
					router.addBgpPrepend(parameters[parameters.length-1], parameters, "in");
				LabPool.getCurrentLab().updateDevice(router);
			} else
				System.out.println("Error: You're trying to set a BGP prepending to an unknow router ("+parameters[0]+")");
			break;
		case "bgp_prepending_out":
			if(parameters != null && LabPool.getCurrentLab().getDevices().containsKey(parameters[0])) {
				Router router = (Router) LabPool.getCurrentLab().getDevices().get(parameters[0]);
				if(parameters.length == 3)
					router.addBgpPrepend(parameters[2], parameters[1], "out");
				else
					router.addBgpPrepend(parameters[parameters.length-1], parameters, "out");
				LabPool.getCurrentLab().updateDevice(router);
			} else
				System.out.println("Error: You're trying to set a BGP prepending to an unknow router ("+parameters[0]+")");
			break;
			
		//DNS COMMANDS
		case "dns":
			if(parameters != null && parameters.length >= 5) {
				if(parameters[3].charAt(parameters[3].length()-1) != '.')
					parameters[3] = parameters[3]+".";
				DNS dns = new DNS(parameters[0], parameters[2], parameters[3]);
				if(parameters.length == 6)
					dns.setGateway(parameters[5]);
				if(LabPool.getCurrentLab().getLinks().containsKey(parameters[1])) {
					if(LabPool.getCurrentLab().getDevices().containsKey(parameters[4]))
						dns.setRootNS(LabPool.getCurrentLab().getDevices().get(parameters[4]));
					else if(parameters[4].contains("."))
						dns.setRootIP(parameters[4]);
					else
						System.out.println("Error: Wrong root DNS name for"+parameters[0]+" ("+parameters[4]+")");
				}
				
				LabPool.getCurrentLab().addDevice(dns, parameters[1]);
			} else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "root_dns":
			if(parameters != null && parameters.length >= 3)
				LabPool.getCurrentLab().addDevice(new DNS(parameters[0], parameters[2], "."), parameters[1]);
			else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
		case "set_url":
			if(parameters != null && parameters.length >= 2)
				LabPool.getCurrentLab().addURL(parameters[0], parameters[1]);
			else
				System.out.println("Error: wrong parameters"+(parameters != null ? " for "+parameters[0] : ""));
			break;
			
		default:
			System.out.println("Error: "+command+" not recognized");
		}
		
	}

}
