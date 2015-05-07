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
		INSERT, UPDATE, DELETE, UNKNOWN;
		public static RecStatus parse(String attribute) {
			if (attribute == null) attribute = "1";
			switch (attribute) {
			case "1":
				return INSERT;
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

		protected String timeframeToString() {
			if (timeframeBegin == null && timeframeEnd == null) return "";
			return ", timeframe=[" + (timeframeBegin == null ? "null" : TIMEFRAME_DATEFORMAT.format(timeframeBegin)) + ","
					+ (timeframeEnd == null ? "null" : TIMEFRAME_DATEFORMAT.format(timeframeEnd)) + "]";
		}
	}

	public static class ImsGroup extends ImsObject {
		String grouptype;
		String name; // group/description/short
		String coursecode;
		String subjectcode;

		@Override
		public String toString() {
			return recstatus + " Grupp {namn=" + name + ", typ=" + grouptype + (coursecode != null ? (", kurskod=" + coursecode) : "")
					+ (subjectcode != null ? (", ämneskod=" + subjectcode) : "") + ", guid=" + sourcedidId + timeframeToString() + "}";
		}
	}

	public static class ImsPerson extends ImsObject {
		String personnummer;
		String familyName;
		String givenName;
		String login;
		String userid;
		String email;
		String photo;

		@Override
		public String toString() {
			return recstatus + " Person {namn=" + givenName + " " + familyName + ", guid=" + sourcedidId + timeframeToString() + ", pnr=" + personnummer + "}";
		}
	}

	public static class ImsMembership extends ImsObject {
		final List<ImsMember> members = new ArrayList<>();
	}

	/** Assumes only one role per member. */
	public static class ImsMember extends ImsObject {
		ImsMembership parentMembership;
		String roletype;
		String idtype;
		String principalSchoolUnitCode;

		public ImsMember(ImsMembership parent) {
			this.parentMembership = parent;
		}

		@Override
		public String toString() {
			return recstatus
					+ " Medlemskap {roll="
					+ roletype
					+ ", föräldragrupp="
					+ parentMembership.sourcedidId
					+ ", barn"
					+ ("Person".equals(idtype) ? "person" : "grupp")
					+ "="
					+ sourcedidId
					+ (principalSchoolUnitCode == null ? "" : ("schoolunitcode=" + principalSchoolUnitCode))
					+ timeframeToString()
					+ "}"
					+ ((parentMembership.sourcedidId == null || parentMembership.sourcedidId.isEmpty()) ? " <span style='color:red'>OBS: Saknar föräldragrupp (fel i filen)</span>"
							: "");
		}
	}

	Date datetime;
	ImsPerson person;
	ImsGroup group;
	ImsMembership membership;

}
