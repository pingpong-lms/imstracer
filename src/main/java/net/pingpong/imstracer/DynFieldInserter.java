package net.pingpong.imstracer;

import java.io.File;

import net.pingpong.imstracer.ImsReader.ImsCallback;
import net.pingpong.imstracer.ImsState.ImsGroup;
import net.pingpong.imstracer.ImsState.ImsMembership;
import net.pingpong.imstracer.ImsState.ImsPerson;

public class DynFieldInserter {

	private static void generateSQL(int dynFieldId, String sourcedidId, String content) {
		System.out.println("INSERT INTO dyn_field_content_person (dyn_field_id, userid, content, visible) VALUES (" + dynFieldId
				+ ", (SELECT userid FROM ims_person_source WHERE sourcedid_id = '" + sourcedidId + "'), '" + content + "', true);");
	}

	public static void importFile(File file) throws Exception {
		System.out.println("BEGIN;");
		ImsReader.parseFile(file, new ImsCallback() {
			@Override
			public void onPerson(ImsPerson person) {
				if (person.privacyMarker) {
					final int DYN_FIELD = 36; // select id from dyn_field_person
												// where extern_item_id = 34
												// (PRIVACY);
					generateSQL(DYN_FIELD, person.sourcedidId, "true");
				}
			}

			@Override
			public void onMembership(ImsMembership membership) {
				// Ignore.
			}

			@Override
			public void onGroup(ImsGroup group) {
				// Ignore.
			}
		});
		System.out.println("ABORT;");
	}

}
