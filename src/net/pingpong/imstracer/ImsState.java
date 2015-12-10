package net.pingpong.imstracer;

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
		String coursecode;
		String subjectcode;
		List<String> schoolTypes;

		@Override
		public String toString() {
			return recstatus + " Grupp {namn=" + name + ", typ=" + grouptype + (coursecode != null ? (", kurskod=" + coursecode) : "")
					+ (subjectcode != null ? (", ämneskod=" + subjectcode) : "") + ", guid=" + sourcedidId + timeframeToString() + "}";
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
			return recstatus + " Person {namn=" + givenName + " " + familyName + ", guid=" + sourcedidId + timeframeToString() + ", pnr=" + personnummer + "}";
		}
	}

	public static final class ImsMembership extends ImsObject {
		final List<ImsMember> members = new ArrayList<>();
	}

	/** Assumes only one role per member. */
	public static final class ImsMember extends ImsObject {
		ImsMembership parentMembership;
		String roletype;
		String idtype;
		String principalSchoolUnitCode;

		public ImsMember(ImsMembership parent) {
			this.parentMembership = parent;
		}

		public boolean isPerson() {
			return "Person".equals(idtype);
		}

		@Override
		public String toString() {
			return recstatus + " Medlemskap {roll=" + roletype + ", föräldragrupp=" + parentMembership.sourcedidId + ", barn"
					+ (isPerson() ? "person" : "grupp") + "=" + sourcedidId
					+ (principalSchoolUnitCode == null ? "" : ("schoolunitcode=" + principalSchoolUnitCode)) + timeframeToString() + "}"
					+ ((parentMembership.sourcedidId == null || parentMembership.sourcedidId.isEmpty())
							? " <span style='color:red'>OBS: Saknar föräldragrupp (fel i filen)</span>" : "");
		}
	}

	Date datetime;
	ImsPerson person;
	ImsGroup group;
	ImsMembership membership;

}
