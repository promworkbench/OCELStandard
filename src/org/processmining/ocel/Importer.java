package org.processmining.ocel;

import org.processmining.ocel.importers.JSONImporter;
import org.processmining.ocel.importers.XMLImporter;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class Importer {
	public static OcelEventLog importFromFile(String logPath) {
		if (logPath.contains("jsonocel")) {
			JSONImporter importer = new JSONImporter(logPath);
			return importer.doImport();
		}
		else if (logPath.contains("xmlocel")) {
			XMLImporter importer = new XMLImporter(logPath);
			return importer.doImport();
		}
		return null;
	}
}
