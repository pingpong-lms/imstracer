package net.pingpong.imstracer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import net.pingpong.imstracer.ImsReader.ImsCallback;
import net.pingpong.imstracer.ImsState.ImsGroup;
import net.pingpong.imstracer.ImsState.ImsMember;
import net.pingpong.imstracer.ImsState.ImsMembership;
import net.pingpong.imstracer.ImsState.ImsPerson;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

/**
 * <pre>
 * # Add this to /opt/pingpong/etc/pingpong-vhosts.conf
 * ProxyPassMatch /imstracer/(.*) http://localhost:8888/$1
 * </pre>
 */
public class ImsTracerServer extends NanoHTTPD {

	private final List<File> directories;

	public ImsTracerServer(List<File> directories) {
		super(8888);
		this.directories = directories;
	}

	@Override
	public Response serve(IHTTPSession session) {
		try {
			return doServe(session);
		} catch (Exception e) {
			e.printStackTrace();
			return new NanoHTTPD.Response(Status.INTERNAL_ERROR, "text/plain", "Internal error: " + e.getMessage());
		}
	}

	private NanoHTTPD.Response doServe(IHTTPSession session) throws Exception {
		final StringBuilder response = new StringBuilder();
		Status responseStatus = Status.OK;
		String responseMimeType = "text/html; charset=utf-8";

		Map<String, String> parms = session.getParms();
		String folder = parms.get("folder");
		String file = parms.get("file");
		boolean debugFile = parms.get("debug") != null;

		if (folder == null && file == null) {
			response.append("<html><body>\n");
			for (File dir : directories) {
				String anchor = dir.getAbsolutePath();
				response.append("<h3 id='" + anchor + "'>").append(dir.getAbsolutePath()).append(" <a href='#" + anchor + "'>#</a>").append("</h3>");
				response.append("<ul>");
				File[] xmlFiles = dir.listFiles((pathname) -> pathname.getName().contains(".xml") && !pathname.getName().startsWith("."));
				Arrays.sort(xmlFiles, (a, b) -> b.getName().compareTo(a.getName()));
				for (File xmlFile : xmlFiles) {
					String link = "?file=" + dir.getAbsolutePath() + "/" + xmlFile.getName();
					response.append("<li><a href='").append(link).append("'>").append(xmlFile.getName())
							.append("</a> <a href='" + link + "&debug=true'>?</a></li>");
				}
				response.append("</ul>");
			}
			response.append("</body></html>\n");
		} else if (file != null) {
			boolean foundDir = false;
			for (File dir : directories) {
				if (file.startsWith(dir.getCanonicalPath())) {
					foundDir = true;
					break;
				}
			}
			if (!foundDir) {
				response.append("<h2>Permission denied</h2>");
				responseStatus = Status.FORBIDDEN;
			} else {
				File requestedFile = new File(file);
				if (requestedFile.isFile()) {
					if (debugFile) {
						final Holder<Boolean> anythingFound = new Holder<>(false);
						ImsReader.parseFile(requestedFile, new ImsCallback() {

							@Override
							public void onPerson(ImsPerson person) {
								anythingFound.value = true;
								response.append("<li>").append(person).append("</li>");
							}

							@Override
							public void onGroup(ImsGroup group) {
								anythingFound.value = true;
								response.append("<li>").append(group).append("</li>");
							}

							@Override
							public void onMembership(ImsMembership membership) {
								anythingFound.value = true;
								for (ImsMember member : membership.members) {
									response.append("<li>").append(member).append("</li>");
								}
							}
						});
						if (!anythingFound.value) {
							response.append("<h1>Filen är tom från IMS-händelser</h1>");
						}
					} else {
						responseMimeType = "text/plain; charset=utf-8";
						response.append("serving file...");
						InputStream in = openFile(requestedFile);
						NanoHTTPD.Response resp = new NanoHTTPD.Response(responseStatus, responseMimeType, in);
						resp.setChunkedTransfer(true);
						return resp;
					}
				} else {
					response.append("<h2>Not a file</h2>");
					responseStatus = Status.NOT_FOUND;
				}
			}
		}

		return new NanoHTTPD.Response(responseStatus, responseMimeType, response.toString());
	}

	@SuppressWarnings("resource")
	static InputStream openFile(File f) throws IOException {
		FileInputStream in = new FileInputStream(f);
		return f.getName().endsWith(".bz2") ? new BZip2CompressorInputStream(in) : in;
	}

}
