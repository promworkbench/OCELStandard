package org.processmining.csv;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class OCELExporterCSV {
	public static String exportCsv(OcelEventLog ocel, String newline, char sep, char quotechar) {
		StringBuilder ret = new StringBuilder();
		StringBuilder header = new StringBuilder();
		header.append("ocel:eid");
		header.append(sep);
		header.append("ocel:activity");
		header.append(sep);
		header.append("ocel:timestamp");
		ret.append(header.toString());
		ret.append(newline);
		for (OcelEvent eve : ocel.events.values()) {
			StringBuilder row = new StringBuilder();
			row.append(eve.id);
			row.append(sep);
			row.append(eve.activity);
			row.append(sep);
			row.append(eve.timestamp.toInstant().toString());
			ret.append(row.toString());
			ret.append(newline);
		}
		return ret.toString();
	}
}
