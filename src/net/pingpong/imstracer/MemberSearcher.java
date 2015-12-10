package net.pingpong.imstracer;

import java.io.File;

import net.pingpong.imstracer.ImsReader.ImsCallback;
import net.pingpong.imstracer.ImsState.ImsMember;
import net.pingpong.imstracer.ImsState.ImsMembership;

public class MemberSearcher {

	public static void examineFileOrDir(File file, String group, String role) throws Exception {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles())
				examineFileOrDir(subFile, group, role);
		} else if (file.getName().endsWith(".xml") || file.getName().endsWith(".xml.bz2")) {
			examineFile(file, group, role);
		}
	}

	private static void examineFile(File file, final String group, final String role) throws Exception {
		ImsReader.parseFile(file, new ImsCallback() {
			@Override
			public void onMembership(ImsMembership membership) {
				if (membership.sourcedidId.equals(group)) {
					for (ImsMember member : membership.members) {
						if (member.roletype.equals(role)) {
							System.out.println(file.getName() + ":" + membership.lineNumber + " - Member " + member.sourcedidId + member.timeframeToString()
									+ ", recstatus=" + member.recstatus);
						}
					}
				}
			}
		});
	}

}
