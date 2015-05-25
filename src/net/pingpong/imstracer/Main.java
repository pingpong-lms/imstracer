package net.pingpong.imstracer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class Main {

	public static void printHelpAndExit() {
		System.err.println("Usage: imstracer <command> <command-arguments>\n" // line
				+ "       where command is one of:\n" // line
				+ "       - web <folders-to-serve>\n" // line
				+ "       - snapshot <snapshot-to-validate>");
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		if (args.length <= 1) {
			printHelpAndExit();
		} else if ("web".equals(args[0])) {
			List<File> directoriesToWatch = new ArrayList<>(args.length);
			for (int i = 1; i < args.length; i++) {
				File dir = new File(args[i]);
				if (!dir.isDirectory()) System.err.println(dir.getAbsolutePath() + " is not a folder");
				directoriesToWatch.add(dir);
			}
			if (directoriesToWatch.isEmpty())
				printHelpAndExit();
			else
				new ImsTracerServer(directoriesToWatch).start();
			System.out.println("Serving at http://localhost:" + ImsTracerServer.PORT);
		} else if ("snapshot".equals(args[0])) {
			File file = new File(args[1]);
			if (!file.isFile()) {
				System.err.println("File " + file.getAbsolutePath() + " does not exist");
				System.exit(1);
			} else {
				SortedMap<Integer, String> errors = SnapshotValidator.validateFile(file);
				if (errors.isEmpty()) {
					System.out.println("Ok");
				} else {
					System.out.println("Errors:");
					for (Map.Entry<Integer, String> error : errors.entrySet())
						System.out.println(error.getValue());
				}
			}
		} else {
			printHelpAndExit();
		}
	}

}
