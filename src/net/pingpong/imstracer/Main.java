package net.pingpong.imstracer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void printHelpAndExit() {
		System.err.println("Usage: imstracer <folders-to-expose>");
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		List<File> directoriesToWatch = new ArrayList<>(args.length);
		for (String arg : args) {
			File dir = new File(arg);
			if (!dir.isDirectory()) System.err.println(dir.getAbsolutePath() + " is not a folder");
			directoriesToWatch.add(dir);
		}
		if (directoriesToWatch.isEmpty()) printHelpAndExit();

		new ImsTracerServer(directoriesToWatch).start();
	}

}
