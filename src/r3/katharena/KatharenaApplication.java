package r3.katharena;

import r3.katharena.lab.LabFileHandle;
import r3.katharena.shell.Shell;

public class KatharenaApplication {
	
	private static final String VERSION = "0.6.0";
	
	public static void main(String[] args) {
		System.out.print("╔═══════════════╗\n║   KATHARENA   ║\n╚═══════════════╝\nby Marco De Giovanni\n(v"+VERSION+")\n\n");		
		
		String fileName = null;
		String labName = "lab";
		for(int i=0; i<args.length; i++) {
			if(args[i].contains("-l") || args[i].contains("--load"))
				fileName = args[i+1];
			else if(args[i].contains("-n") || args[i].contains("--name"))
				labName = args[i+1];
		}
		LabPool.setCurrentLab(labName, false);
		if(fileName != null)
			LabFileHandle.loadLab(fileName);
		Shell.startShell();
	}
}