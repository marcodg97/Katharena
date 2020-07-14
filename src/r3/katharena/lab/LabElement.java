package r3.katharena.lab;

import r3.katharena.shell.ShellPrintable;

public abstract class LabElement implements ShellPrintable {

	public abstract String getConfigRow();
	
	public static String help() {
		return "Lab element";
	}
	
}
