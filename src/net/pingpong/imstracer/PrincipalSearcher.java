package net.pingpong.imstracer;

import java.io.File;

import net.pingpong.imstracer.ImsReader.ImsCallback;
import net.pingpong.imstracer.ImsState.ImsMember;
import net.pingpong.imstracer.ImsState.ImsMembership;

public class PrincipalSearcher {

	public static void examineFileOrDir(File file) throws Exception {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles())
				examineFileOrDir(subFile);
		} else if (file.getName().endsWith(".xml") || file.getName().endsWith(".xml.bz2")) {
			examineFile(file);
		}
	}

	private static void examineFile(File file) throws Exception {
		ImsReader.parseFile(file, new ImsCallback() {
			@Override
			public void onMembership(ImsMembership membership) {
				for (ImsMember member : membership.members) {
					if (member.roletype.equals("Principal")) {
						System.out.println(file.getName() + ":" + membership.lineNumber + " - Member " + member.sourcedidId + member.timeframeToString()
								+ ", recstatus=" + member.recstatus + ", group=" + membership.sourcedidId + ", schoolunitcode=" + member.principalSchoolUnitCode);
					}
				}
			}
		});
	}

}
