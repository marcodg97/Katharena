package r3.katharena.lab.device;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import r3.katharena.lab.LabElement;
import r3.katharena.lab.NetworkInterface;

public abstract class Device extends LabElement {

	protected String name;
	protected List<NetworkInterface> interfaces = new ArrayList<>();

	/**********************************************************/
	
	public Device(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public List<NetworkInterface> getInterfaces() {
		return interfaces;
		//return interfaces.toArray();
	}
	
	public void addInterface(NetworkInterface eth) {
		interfaces.add(eth);
	}
	
	public void removeInterface(NetworkInterface eth) {
		interfaces.remove(eth);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || obj.getClass() != Device.class)
			return false;
		Device element = (Device) obj;
		return name.equals(element.getName());
	}
	
	/**********************************************************/

	protected void makeFolder(String directory) {
		new File(directory+name).mkdir();
	}
	
	public abstract void makeFiles(String labDir);
	
}
