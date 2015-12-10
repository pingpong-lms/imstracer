package net.pingpong.imstracer;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;

import net.pingpong.imstracer.ImsReader.ImsCallback;
import net.pingpong.imstracer.ImsState.ImsGroup;
import net.pingpong.imstracer.ImsState.ImsMember;
import net.pingpong.imstracer.ImsState.ImsMembership;
import net.pingpong.imstracer.ImsState.ImsPerson;

public class SnapshotValidator {

	public static SortedMap<Integer, String> validateFile(File file) {

		/** Group sourcedidid:s. */
		final HashSet<String> groups = new HashSet<>();
		/** Person sourcedidid:s. */
		final HashSet<String> persons = new HashSet<>();
		// sourcedid-of-parent -> membership:
		final HashMap<String, ImsMembership> memberships = new HashMap<>();

		SortedMap<Integer, String> errors = new TreeMap<>();
		try {
			ImsReader.parseFile(file, new ImsCallback() {
				@Override
				public void onPerson(ImsPerson person) {
					persons.add(person.sourcedidId);
				}

				@Override
				public void onMembership(ImsMembership membership) {
					memberships.put(membership.sourcedidId, membership);
				}

				@Override
				public void onGroup(ImsGroup group) {
					groups.add(group.sourcedidId);
					if (group.timeframeBegin != null && group.timeframeEnd != null && group.timeframeBegin.after(group.timeframeEnd)) {
						errors.put(group.lineNumber,
								"Line: " + group.lineNumber + " - Group with timeframe/begin after timeframe/end: sourcedid/id='" + group.sourcedidId + "'");
					}
				}
			});

			for (ImsMembership membership : memberships.values()) {
				if (!groups.contains(membership.sourcedidId)) {
					errors.put(membership.lineNumber,
							"Line: " + membership.lineNumber + " - Membership with non-existing parent sourcedid/id='" + membership.sourcedidId + "'");
				}
				for (ImsMember member : membership.members) {
					if (member.timeframeBegin != null && member.timeframeEnd != null && member.timeframeBegin.after(member.timeframeEnd)) {
						errors.put(member.lineNumber, "Line: " + member.lineNumber
								+ " - Membership with timeframe/begin after timeframe/end for parent sourcedid/id='" + member.sourcedidId + "'");
					}

					if (member.isPerson()) {
						if (!persons.contains(member.sourcedidId)) {
							errors.put(member.lineNumber,
									"Line: " + member.lineNumber + " - Membership with non-existing child person sourcedid/id='" + member.sourcedidId + "'");
						}
					} else {
						if (!groups.contains(member.sourcedidId)) {
							errors.put(member.lineNumber,
									"Line: " + member.lineNumber + " - Membership with non-existing child group sourcedid/id='" + member.sourcedidId + "'");
						}
					}
				}
			}

			System.out.println("Snapshot: groups=" + groups.size() + ", persons=" + persons.size() + ", memberships=" + memberships.size());
		} catch (Exception e) {
			errors.put(-1, "Error parsing: " + e.getMessage());
			e.printStackTrace();
		}
		return errors;
	}
}
