package net.pingpong.imstracer;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.pingpong.imstracer.ImsReader.ImsCallback;
import net.pingpong.imstracer.ImsState.Activity;
import net.pingpong.imstracer.ImsState.ImsGroup;
import net.pingpong.imstracer.ImsState.ImsMember;
import net.pingpong.imstracer.ImsState.ImsMembership;
import net.pingpong.imstracer.ImsState.ImsPerson;
import net.pingpong.imstracer.ImsState.Placement;
import net.pingpong.imstracer.ImsState.Responsibility;

public class Visualizer {

	final static String PREFIX = "    ";

	final Map<String, ImsMembership> memberships = new HashMap<>();
	final Map<String, ImsPerson> persons = new HashMap<>();
	final Map<String, ImsGroup> groups = new HashMap<>();
	final Set<String> groupsWithParents = new HashSet<>();

	private static void prefixed(int depth, String content) {
		for (int i = 0; i < depth; i++) {
			System.out.print(PREFIX);
		}
		System.out.println(content);
	}

	private static String red(String content) {
		return "\u001B[31;1m" + content + "\u001B[39m";
	}

	private static String green(String content) {
		return "\u001B[32;1m" + content + "\u001B[39m";
	}

	private static String yellow(String content) {
		return "\u001B[33;1m" + content + "\u001B[39m";
	}

	private static String cyan(String content) {
		return "\u001B[36;1m" + content + "\u001B[39m";
	}

	public void visualizeToStdout(File file) throws Exception {

		ImsReader.parseFile(file, new ImsCallback() {
			@Override
			public void onPerson(ImsPerson person) {
				persons.put(person.sourcedidId, person);
			}

			@Override
			public void onGroup(ImsGroup group) {
				groups.put(group.sourcedidId, group);
			}

			@Override
			public void onMembership(ImsMembership membership) {
				memberships.put(membership.sourcedidId, membership);
				for (ImsMember member : membership.members) {
					if (!member.isPerson()) {
						groupsWithParents.add(member.sourcedidId);
					}
				}
			}
		});

		List<ImsGroup> topGroups = groups.values().stream().filter(g -> !groupsWithParents.contains(g)).sorted((g1, g2) -> g1.name.compareTo(g2.name))
				.collect(Collectors.toList());
		for (ImsGroup topGroup : topGroups) {
			System.out.println("Group {name=" + topGroup.name + ", type=" + topGroup.grouptype + ", id=" + topGroup.sourcedidId + "}");
			describe(1, topGroup);
		}
		System.out.println("SUMMARY: Top groups: " + topGroups.size());
	}

	private void describe(int depth, ImsGroup group) {
		ImsMembership membership = memberships.get(group.sourcedidId);
		if (membership == null) return;
		for (ImsMember member : membership.members) {
			if (member.isPerson()) {
				ImsPerson childPerson = persons.get(member.sourcedidId);
				if (childPerson == null) {
					prefixed(depth, "ERROR: Non-existing person '" + member.sourcedidId + "'");
				} else {
					StringBuilder description = new StringBuilder();
					description.append(red(member.roletype));
					description.append(": ");
					description.append(childPerson.givenName + " " + childPerson.familyName);
					if (member.placements != null) {
						for (Placement placement : member.placements) {
							StringBuilder desc = new StringBuilder();
							desc.append(" placement{");
							String separator = "";
							if (placement.schoolUnitCode != null) {
								desc.append("code=" + placement.schoolUnitCode);
								separator = ",";
							}
							if (placement.schoolYear != null) {
								desc.append(separator + "year=" + placement.schoolYear);
								separator = ",";
							}
							if (placement.programCode != null) desc.append(separator + "program=" + placement.programCode);
							desc.append("}");
							description.append(green(desc.toString()));
						}
					}
					if (member.activities != null) {
						for (Activity activity : member.activities) {
							StringBuilder desc = new StringBuilder();
							desc.append(" activity{");
							String separator = "";
							if (activity.subjectCode != null) {
								desc.append("subject=" + activity.subjectCode);
								separator = ",";
							}
							if (activity.courseCode != null) desc.append(separator + "course=" + activity.courseCode);
							desc.append("}");
							description.append(yellow(desc.toString()));
						}
					}
					if (member.responsibilities != null) {
						for (Responsibility responsibility : member.responsibilities) {
							description.append(cyan(" responsibility{code=" + responsibility.schoolUnitCode + "}"));
						}
					}
					prefixed(depth, description.toString());
				}
			}
		}
		for (ImsMember member : membership.members) {
			if (!member.isPerson()) {
				ImsGroup childGroup = groups.get(member.sourcedidId);
				if (childGroup == null) {
					prefixed(depth, "ERROR: Non-existing group '" + member.sourcedidId + "'");
				} else {
					prefixed(depth, "Group {name=" + childGroup.name + ", type=" + childGroup.grouptype + ", id=" + childGroup.sourcedidId + "}");
					describe(depth + 1, childGroup);
				}
			}
		}
	}

}
