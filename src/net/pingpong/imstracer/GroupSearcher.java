package net.pingpong.imstracer;

import java.io.File;

import net.pingpong.imstracer.ImsReader.ImsCallback;
import net.pingpong.imstracer.ImsState.ImsGroup;

public class GroupSearcher {

	public static void examineFileOrDir(File file, String sourcedidId) throws Exception {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles())
				examineFileOrDir(subFile, sourcedidId);
		} else if (file.getName().endsWith(".xml") || file.getName().endsWith(".xml.bz2")) {
			examineFile(file, sourcedidId);
		}
	}

	private static void examineFile(File file, final String sourcedidId) throws Exception {
		ImsReader.parseFile(file, new ImsCallback() {
			@Override
			public void onGroup(ImsGroup group) {
				if (group.sourcedidId.equals(sourcedidId)) {
					StringBuilder sb = new StringBuilder();
					sb.append(file.getName() + "@" + group.lineNumber + ": recstatus=" + group.recstatus);
					if (group.schoolTypes != null) sb.append(", schoolTypes=" + group.schoolTypes);
					System.out.println(sb);
				}
			}
		});
	}

}
