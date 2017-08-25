package net.pingpong.imstracer;

import java.io.File;
import java.util.*;

public class Main {

	public static void printHelpAndExit() {
		System.err.println("Usage: imstracer <command> <command-arguments>\n" // line
				+ "       where command is one of:\n" // line
				+ "       - web <folders-to-serve>\n" // line
				+ "       - snapshot <snapshot-to-validate>\n" // line
				+ "       - dynfields-sql <file-to-import>\n" //
				+ "       - find-members <group> <role> <files-or-dirs>\n" // line
				+ "       - log-group-membership <group> <member> <files-or-dirs>\n" // line
				+ "       - find-group <group> <files-or-dirs>\n" // Line
				+ "       - find-principals <files-or-dirs>\n"
				+ "       - visualize <file>");
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
		} else if ("dynfields-sql".equals(args[0])) {
			File file = new File(args[1]);
			if (!file.isFile()) {
				System.err.println("File " + file.getAbsolutePath() + " does not exist");
				System.exit(1);
			} else {
				DynFieldInserter.importFile(file);
			}
		} else if ("find-members".equals(args[0])) {
			if (args.length < 4) {
				System.err.println("usage: find-members <group> <role> <file-or-dirs>");
				System.exit(1);
			}
			String groupId = args[1];
			String role = args[2];
			System.out.println("Searching for members in group " + groupId + " with role " + role);
			FileReader.examineFiles(args, 3, new ImsReader.ImsCallback() {
				@Override
				public void onMembership(ImsState.ImsMembership membership) {
					if (membership.sourcedidId.equals(groupId)) {
						for (ImsState.ImsMember member : membership.members) {
							if (member.roletype.equals(role)) {
								System.out.println(imsState.xmlFile.getName() + ":" + membership.lineNumber + " - Member " + member.sourcedidId + member.timeframeToString()
										+ ", recstatus=" + member.recstatus);
							}
						}
					}
				}
			});
		} else if ("log-group-membership".equals(args[0])) {
			if (args.length < 4) {
				System.err.println("usage: log-group-membership <group> <member> <files-or-dirs>");
				System.exit(1);
			}
			final String groupId = args[1];
			final String memberId = args[2];
			System.out.println("Searching for history between group " + groupId + " and member " + memberId);
			FileReader.examineFiles(args, 3, new ImsReader.ImsCallback() {
				@Override
				public void onMembership(ImsState.ImsMembership membership) {
					if (membership.sourcedidId.equals(groupId)) {
						for (ImsState.ImsMember member : membership.members) {
							if (member.sourcedidId.equals(memberId)) {
								System.out.println(imsState.getIsoDateTime() + ": role=" + member.roletype +
										", recstatus=" + member.recstatus + " in " + imsState.xmlFile.getAbsolutePath());
							}
						}
					}
				}
			});
		} else if ("find-group".equals(args[0])) {
			if (args.length < 3) {
				System.err.println("usage: find-members <group> <file-or-dirs>");
				System.exit(1);
			}
			String groupId = args[1];
			System.out.println("Searching for group " + groupId);
			FileReader.examineFiles(args, 2, new ImsReader.ImsCallback() {
				@Override
				public void onGroup(ImsState.ImsGroup group) {
					if (group.sourcedidId.equals(groupId)) {
						StringBuilder sb = new StringBuilder();
						sb.append(imsState.xmlFile.getName() + "@" + group.lineNumber + ": recstatus=" + group.recstatus);
						if (group.schoolTypes != null) sb.append(", schoolTypes=" + group.schoolTypes);
						System.out.println(sb);
					}
				}
			});
		} else if ("find-principals".equals(args[0])) {
			if (args.length < 2) {
				System.err.println("usage: find-principals <file-or-dirs>");
				System.exit(1);
			}
			FileReader.examineFiles(args, 1, new ImsReader.ImsCallback() {
				@Override
				public void onMembership(ImsState.ImsMembership membership) {
					for (ImsState.ImsMember member : membership.members) {
						if (member.roletype.equals("Principal")) {
							System.out.println(imsState.xmlFile.getName() + ":" + membership.lineNumber + " - Member " + member.sourcedidId + member.timeframeToString()
									+ ", recstatus=" + member.recstatus + ", group=" + membership.sourcedidId + ", responsibilities=" + member.responsibilities);
						}
					}
				}
			});
		} else if ("visualize".equals(args[0])) {
			if (args.length != 2) {
				System.err.println("usage: find-principals <file>");
				System.exit(1);
			}
			File file = new File(args[1]);
			if (!file.isFile()) {
				System.err.println(args[1] + " does not exist!");
				System.exit(1);
			}
			new Visualizer().visualizeToStdout(file);
		} else {
			printHelpAndExit();
		}
	}

}
