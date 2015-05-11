package net.pingpong.imstracer;

import java.io.File;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import net.pingpong.imstracer.ImsReader.ImsCallback;
import net.pingpong.imstracer.ImsState.ImsGroup;
import net.pingpong.imstracer.ImsState.ImsMember;
import net.pingpong.imstracer.ImsState.ImsMembership;
import net.pingpong.imstracer.ImsState.ImsPerson;

public class SnapshotValidator {

	public static SortedMap<Integer, String> validateFile(File file) {

		// sourcedidid -> group:
		final HashMap<String, ImsGroup> groups = new HashMap<>();
		// sourcedidid -> person:
		final HashMap<String, ImsPerson> persons = new HashMap<>();
		// sourcedid-of-parent -> membership:
		final HashMap<String, ImsMembership> memberships = new HashMap<>();

		SortedMap<Integer, String> errors = new TreeMap<>();
		try {
			ImsReader.parseFile(file, new ImsCallback() {

				@Override
				public void onPerson(ImsPerson person) {
					persons.put(person.sourcedidId, person);
				}

				@Override
				public void onMembership(ImsMembership membership) {
					memberships.put(membership.sourcedidId, membership);
				}

				@Override
				public void onGroup(ImsGroup group) {
					groups.put(group.sourcedidId, group);
				}

			});

			System.out.println("Snapshot: groups=" + groups.size() + ", persons=" + persons.size() + ", memberships=" + memberships.size());

			for (ImsMembership membership : memberships.values()) {
				if (!groups.containsKey(membership.sourcedidId)) {
					errors.put(membership.lineNumber, "Line: " + membership.lineNumber + " - Membership with non-existing parent sourcedid/id='"
							+ membership.sourcedidId + "'");
				}
				for (ImsMember member : membership.members) {
					if (member.isPerson()) {
						if (!persons.containsKey(member.sourcedidId)) {
							errors.put(member.lineNumber, "Line: " + member.lineNumber + " - Membership with non-existing child person sourcedid/id='"
									+ member.sourcedidId + "'");
						}
					} else {
						if (!groups.containsKey(member.sourcedidId)) {
							errors.put(member.lineNumber, "Line: " + member.lineNumber + " - Membership with non-existing child group sourcedid/id='"
									+ member.sourcedidId + "'");
						}
					}
				}
			}
		} catch (Exception e) {
			errors.put(-1, "Error parsing: " + e.getMessage());
			e.printStackTrace();
		}
		return errors;
	}
}
