package org.processmining.ocel;

import org.processmining.ocel.exporters.JSONExporter;
import org.processmining.ocel.exporters.XMLExporter;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class Exporter {
	public static void exportToFile(OcelEventLog eventLog, String destinationPath) {
		if (destinationPath.contains("jsonocel")) {
			JSONExporter exporter = new JSONExporter(eventLog, destinationPath);
			exporter.exportLog();
		}
		else if (destinationPath.contains("xmlocel")) {
			XMLExporter exporter = new XMLExporter(eventLog, destinationPath);
			exporter.exportLog();
		}
	}
}
