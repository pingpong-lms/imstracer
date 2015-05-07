package net.pingpong.imstracer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XmlState {

	String path = "";

	public void startElement(String tagName) {
		path = path + "/" + tagName;
	}

	public void endElement() {
		int lastSlashIndex = path.lastIndexOf('/');
		if (lastSlashIndex == -1) throw new IllegalArgumentException("endElement() called with empty state");
		path = path.substring(0, lastSlashIndex);
	}

	public String readElement(XMLStreamReader r) throws XMLStreamException {
		endElement(); // Since the below getElementText() call will advance:
		return r.getElementText();
	}

	@Override
	public String toString() {
		return path;
	}

}
