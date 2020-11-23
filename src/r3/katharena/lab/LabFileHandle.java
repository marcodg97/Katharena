package r3.katharena.lab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import r3.katharena.command.CommandHandle;
import r3.katharena.lab.device.Device;

public class LabFileHandle {
	
	public static void saveLab(String filename) {
		String content = "";
		Lab lab = LabPool.getCurrentLab();
		for(Link link : lab.getLinks().values())
			content = content+link.getConfigRow();
		content = content+"\n";
		for(Device device : lab.getDevices().values())
			content = content+device.getConfigRow();
		
		try {
			Files.write(Paths.get(filename), content.getBytes());
		} catch (IOException e) {
			System.out.println("Error: IO Exception ("+filename+")\n");
		}
		System.out.println(LabPool.getCurrentLabName()+" saved on "+filename);
	}
	
	public static void loadLab(String filename) {
		System.out.println("loading "+filename);
		Long startTime = new Date().getTime();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			String line;
			while((line = br.readLine()) != null) {
				if(line.length() > 0 && line.charAt(0) == '$') {
					line = line.replace(" ","");
					CommandHandle.exec(line.substring(1, line.indexOf("(")), line.substring(line.indexOf("(")+1, line.indexOf(")")).split(","));
				}
			}
			br.close();
			System.out.println(filename+" successfully loaded in "+(((float)(new Date().getTime() - startTime))/1000)+"s");
		} catch (FileNotFoundException e) {
			System.out.println("Error: "+filename+" not found");
		} catch (IOException e) {
			System.out.println("Error: IO Exception ("+filename+")\n");
		}
	}
	
}