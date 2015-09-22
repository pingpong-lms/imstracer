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
				if (person.programCode != null) {
					final int DYN_FIELD = 35; // select id from dyn_field_person
												// where extern_item_id = 32
												// (SCHOOL_PROGRAM_CODE).
					generateSQL(DYN_FIELD, person.sourcedidId, person.programCode);
				}

				if (person.enrolledSchoolUnitCode != null) {
					final int DYN_FIELD = 28; // select id from dyn_field_person
												// where extern_item_id = 33
												// (ENROLLED_SCHOOL_UNIT_CODE).
					generateSQL(DYN_FIELD, person.sourcedidId, person.enrolledSchoolUnitCode);
				}

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
