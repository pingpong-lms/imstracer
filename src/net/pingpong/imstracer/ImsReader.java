package net.pingpong.imstracer;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import net.pingpong.imstracer.ImsState.ImsGroup;
import net.pingpong.imstracer.ImsState.ImsMember;
import net.pingpong.imstracer.ImsState.ImsMembership;
import net.pingpong.imstracer.ImsState.ImsPerson;
import net.pingpong.imstracer.ImsState.RecStatus;

public class ImsReader {

	private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

	public static abstract class ImsCallback {
		public void onPerson(@SuppressWarnings("unused") ImsPerson person) {
			// Do nothing by default.
		}

		public void onGroup(@SuppressWarnings("unused") ImsGroup group) {
			// Do nothing by default.
		}

		public void onMembership(@SuppressWarnings("unused") ImsMembership membership) {
			// Do nothing by default.
		}
	}

	public static void parseFile(File file, ImsCallback callback) throws Exception {
		try (InputStream in = ImsTracerServer.openFile(file)) {
			XMLStreamReader r = XML_INPUT_FACTORY.createXMLStreamReader(in);

			XmlState xml = new XmlState();
			ImsState ims = new ImsState();

			while (r.hasNext()) {
				switch (r.next()) {
				case XMLStreamReader.START_ELEMENT:
					String elementName = r.getLocalName();
					xml.startElement(elementName);

					switch (xml.path) {
					case "/enterprise/properties/datetime":
						String datetimeString = xml.readElement(r);
						try {
							ims.datetime = ImsState.DATETIME_DATEFORMAT.parse(datetimeString);
						} catch (ParseException pe) {
							// throw new
							// ImsException("Unable to parse datetime '" +
							// datetimeString + "'");
						}
						break;
					case "/enterprise/group":
						ims.group = new ImsGroup();
						ims.group.lineNumber = r.getLocation().getLineNumber();
						ims.group.recstatus = RecStatus.parse(r.getAttributeValue(null, "recstatus"));
						break;
					case "/enterprise/group/description/short":
						ims.group.name = xml.readElement(r);
						break;
					case "/enterprise/group/grouptype/typevalue":
						ims.group.grouptype = xml.readElement(r);
						break;
					case "/enterprise/group/sourcedid/id":
						ims.group.sourcedidId = xml.readElement(r);
						break;
					case "/enterprise/group/timeframe/begin": {
						String dateString = xml.readElement(r).trim();
						if (!dateString.isEmpty()) {
							ims.group.timeframeBegin = ImsState.TIMEFRAME_DATEFORMAT.parse(dateString);
						}
					}
						break;
					case "/enterprise/group/timeframe/end": {
						String dateString = xml.readElement(r).trim();
						if (!dateString.isEmpty()) {
							ims.group.timeframeEnd = ImsState.TIMEFRAME_DATEFORMAT.parse(dateString);
						}
					}
						break;
					case "/enterprise/group/extension/coursecode":
						ims.group.coursecode = xml.readElement(r).trim();
						break;
					case "/enterprise/group/extension/schooltype":
						if (ims.group.schoolTypes == null) ims.group.schoolTypes = new ArrayList<>();
						ims.group.schoolTypes.add(xml.readElement(r).trim());
						break;
					case "/enterprise/group/extension/subjectcode":
						if (ims.group.coursecode == null) {
							// Only note subject code when necessary.
							ims.group.subjectcode = xml.readElement(r).trim();
						}
						break;
					case "/enterprise/person":
						ims.person = new ImsPerson();
						ims.person.lineNumber = r.getLocation().getLineNumber();
						ims.person.recstatus = RecStatus.parse(r.getAttributeValue(null, "recstatus"));

						break;
					case "/enterprise/person/extension/privacy":
						ims.person.privacyMarker = "Level1".equals(xml.readElement(r));
						break;
					case "/enterprise/person/extension/programcode":
						ims.person.programCode = xml.readElement(r);
						break;
					case "/enterprise/person/extension/schoolunitcode":
						ims.person.enrolledSchoolUnitCode = xml.readElement(r);
						break;
					case "/enterprise/person/sourcedid/id":
						ims.person.sourcedidId = xml.readElement(r);
						break;
					case "/enterprise/person/userid":
						if ("PID".equals(r.getAttributeValue(null, "useridtype"))) ims.person.personnummer = xml.readElement(r);
						break;
					case "/enterprise/person/name/n/family":
						ims.person.familyName = xml.readElement(r);
						break;
					case "/enterprise/person/name/n/given":
						ims.person.givenName = xml.readElement(r);
						break;
					case "/enterprise/membership":
						ims.membership = new ImsMembership();
						ims.membership.lineNumber = r.getLocation().getLineNumber();
						break;
					case "/enterprise/membership/sourcedid/id":
						ims.membership.sourcedidId = xml.readElement(r);
						break;
					case "/enterprise/membership/member":
						ImsMember member = new ImsMember(ims.membership);
						member.lineNumber = r.getLocation().getLineNumber();
						ims.membership.members.add(member);
						break;
					case "/enterprise/membership/member/sourcedid/id": {
						ImsMember lastMember = ims.membership.members.get(ims.membership.members.size() - 1);
						lastMember.sourcedidId = xml.readElement(r);
					}
						break;
					case "/enterprise/membership/member/idtype": {
						ImsMember lastMember = ims.membership.members.get(ims.membership.members.size() - 1);
						lastMember.idtype = xml.readElement(r);
					}
						break;
					case "/enterprise/membership/member/role": {
						ImsMember lastMember = ims.membership.members.get(ims.membership.members.size() - 1);
						lastMember.roletype = r.getAttributeValue(null, "roletype");
						lastMember.recstatus = RecStatus.parse(r.getAttributeValue(null, "recstatus"));
					}
						break;
					case "/enterprise/membership/member/role/extension/schoolunitcode": {
						ImsMember lastMember = ims.membership.members.get(ims.membership.members.size() - 1);
						lastMember.principalSchoolUnitCode = xml.readElement(r);
					}
						break;
					case "/enterprise/membership/member/role/timeframe/begin": {
						String dateString = xml.readElement(r).trim();
						if (!dateString.isEmpty()) {
							ImsMember lastMember = ims.membership.members.get(ims.membership.members.size() - 1);
							lastMember.timeframeBegin = ImsState.TIMEFRAME_DATEFORMAT.parse(dateString);
						}
					}
						break;
					case "/enterprise/membership/member/role/timeframe/end": {
						String dateString = xml.readElement(r).trim();
						if (!dateString.isEmpty()) {
							ImsMember lastMember = ims.membership.members.get(ims.membership.members.size() - 1);
							lastMember.timeframeEnd = ImsState.TIMEFRAME_DATEFORMAT.parse(dateString);
						}
					}
						break;
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					switch (xml.path) {
					case "/enterprise/group":
						callback.onGroup(ims.group);
						break;
					case "/enterprise/person":
						callback.onPerson(ims.person);
						break;
					case "/enterprise/membership":
						callback.onMembership(ims.membership);
						break;
					}
					xml.endElement();
					break;
				}
			}
		}
	}
}
