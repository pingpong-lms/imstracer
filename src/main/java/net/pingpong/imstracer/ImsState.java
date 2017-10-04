package net.pingpong.imstracer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImsState {

	public static final SimpleDateFormat DATETIME_DATEFORMAT = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss aa");
	public static final SimpleDateFormat TIMEFRAME_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

	static {
		DATETIME_DATEFORMAT.setLenient(false);
		TIMEFRAME_DATEFORMAT.setLenient(false);
	}

	public static enum RecStatus {
		ADD_OR_UPDATE, ADD, UPDATE, DELETE, UNKNOWN;
		public static RecStatus parse(String attribute) {
			if (attribute == null) {
				// http://www.imsglobal.org/enterprise/entv1p1/imsent_infov1p1.html
				// 4.3.5 Common Data Objects: "If this field is not present the
				// target should interpret the behavior as 'Add' or 'Update'".
				return ADD_OR_UPDATE;
			}
			switch (attribute) {
			case "1":
				return ADD;
			case "2":
				return UPDATE;
			case "3":
				return DELETE;
			default:
				return UNKNOWN;
			}
		}
	}

	public static class ImsObject {
		String sourcedidId;
		RecStatus recstatus;
		Date timeframeBegin;
		Date timeframeEnd;
		int lineNumber;

		protected String timeframeToString() {
			if (timeframeBegin == null && timeframeEnd == null) return "";
			return ", timeframe=[" + (timeframeBegin == null ? "null" : TIMEFRAME_DATEFORMAT.format(timeframeBegin)) + ","
					+ (timeframeEnd == null ? "null" : TIMEFRAME_DATEFORMAT.format(timeframeEnd)) + "]";
		}
	}

	public static final class ImsGroup extends ImsObject {
		String grouptype;
		String name; // group/description/short
		List<String> schoolTypes;

		@Override
		public String toString() {
			return recstatus + " Grupp {namn=" + name + ", typ=" + grouptype + ", guid=" + sourcedidId + timeframeToString() + "}";
		}
	}

	public static final class ImsPerson extends ImsObject {
		String personnummer;
		String familyName;
		String givenName;
		String login;
		String userid;
		String email;
		String photo;
		String enrolledSchoolUnitCode;
		String programCode;
		boolean privacyMarker;

		@Override
		public String toString() {
			return recstatus + " Person {namn=" + givenName + " " + familyName + ", guid=" + sourcedidId + timeframeToString() + ", pnr=" + personnummer + ", userid=" + userid + "}";
		}
	}

	public static final class ImsMembership extends ImsObject {
		final List<ImsMember> members = new ArrayList<>();
	}

	public static final class Responsibility {
		String schoolUnitCode;

		@Override
		public String toString() {
			return "Responsibility{code=" + schoolUnitCode + "}";
		}
	}

	public static final class Activity {
		String courseCode;
		String subjectCode;

		@Override
		public String toString() {
			return "Activity{courseCode=" + courseCode + ",subjectCode=" + subjectCode + "}";
		}
	}

	public static final class Placement {
		String schoolUnitCode;
		String schoolYear;
		String programCode;

		@Override
		public String toString() {
			return "Placement{schoolUnitCode=" + schoolUnitCode + ",schoolYear=" + schoolYear + ",programCode=" + programCode + "}";
		}
	}

	/** Assumes only one role per member. */
	public static final class ImsMember extends ImsObject {
		ImsMembership parentMembership;
		String roletype;
		String idtype;
		List<Responsibility> responsibilities;
		List<Activity> activities;
		List<Placement> placements;

		public ImsMember(ImsMembership parent) {
			this.parentMembership = parent;
		}

		public boolean isPerson() {
			return "Person".equals(idtype) || "1".equals(idtype);
		}

		public void newResponsibility() {
			if (responsibilities == null) responsibilities = new ArrayList<>();
			responsibilities.add(new Responsibility());
		}

		public void newActivity() {
			if (activities == null) activities = new ArrayList<>();
			activities.add(new Activity());
		}

		public void newPlacement() {
			if (placements == null) placements = new ArrayList<>();
			placements.add(new Placement());
		}

		public Responsibility currentResponsibility() {
			return responsibilities.get(responsibilities.size() - 1);
		}

		public Activity currentActivity() {
			return activities.get(activities.size() - 1);
		}

		public Placement currentPlacement() {
			return placements.get(placements.size() - 1);
		}

		@Override
		public String toString() {
			return recstatus + " Medlemskap {roll=" + roletype + ", föräldragrupp=" + parentMembership.sourcedidId + ", barn"
					+ (isPerson() ? "person" : "grupp") + "=" + sourcedidId + timeframeToString() + "}"
					+ ((parentMembership.sourcedidId == null || parentMembership.sourcedidId.isEmpty())
							? " <span style='color:red'>OBS: Saknar föräldragrupp (fel i filen)</span>" : "");
		}
	}

	File xmlFile;
	Date datetime;
	ImsPerson person;
	ImsGroup group;
	ImsMembership membership;

	public String getIsoDateTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(datetime);
	}

}
