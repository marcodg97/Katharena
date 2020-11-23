package r3.katharena.lab;

import java.util.HashMap;
import java.util.Map;

public class LabPool {

	private static Map<String, Lab> labs = new HashMap<>();
	private static String currentLab;
	
	public static Lab getCurrentLab() {
		if(labs.containsKey(currentLab))
			return labs.get(currentLab);
		return null;
	}
	
	public static String getCurrentLabName() {
		return currentLab;
	}
	
	public static void setCurrentLab(String currentLab) {
		setCurrentLab(currentLab, true);
	}
	
	public static void setCurrentLab(String currentLab, boolean verbose) {
		if(!labs.containsKey(currentLab)) {
			if(verbose)
				System.out.println("Creating lab "+currentLab);
			labs.put(currentLab, new Lab(currentLab));
		}
		LabPool.currentLab = currentLab;
		if(verbose)
			System.out.println("switched to lab \""+currentLab+"\"");
	}
	
	public static void addLab(Lab lab) {
		labs.put(lab.getName(), lab);
	}
	
}