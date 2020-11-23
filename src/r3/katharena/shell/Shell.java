package r3.katharena.shell;

import java.util.Scanner;

import r3.katharena.command.CommandHandle;
import r3.katharena.lab.LabPool;

public class Shell {
	
	public static void startShell() {
		Scanner shellInput = new Scanner(System.in);
		String input;
		String command;
		String[] parameters;

		while(true) {
			System.out.print(LabPool.getCurrentLabName()+"$>");
			input = shellInput.nextLine();
			
			if(input.equals("exit")) {
				shellInput.close();
				System.exit(0);
			}
			
			if(input.contains(" ")) {
				command = input.substring(0, input.indexOf(" "));
				parameters = input.substring(input.indexOf(" ")+1).split(" ");
			} else {
				command = input;
				parameters = null;
			}
			CommandHandle.exec(command, parameters);
		}
	}

}