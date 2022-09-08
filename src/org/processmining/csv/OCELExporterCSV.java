package org.processmining.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

public class OCELExporterCSV {
	public static String exportCsv(OcelEventLog ocel, String newline, char sep, char quotechar) {
		List<String> objectTypes = new ArrayList<String>(ocel.objectTypes.keySet());
		List<String> attributeNames = new ArrayList<String>(ocel.getAttributeNames());
		StringBuilder ret = new StringBuilder();
		StringBuilder header = new StringBuilder();
		header.append("ocel:eid");
		header.append(sep);
		header.append("ocel:activity");
		header.append(sep);
		header.append("ocel:timestamp");
		for (String objType : objectTypes) {
			header.append(sep);
			header.append("ocel:type:" + objType);
		}
		for (String attName : attributeNames) {
			header.append(sep);
			header.append(attName);
		}
		ret.append(header.toString());
		ret.append(newline);
		List<String> evids = new ArrayList<String>(ocel.events.keySet());
		Collections.sort(evids);
		System.out.println(evids);
		for (String evid : evids) {
			OcelEvent eve = ocel.events.get(evid);
			StringBuilder row = new StringBuilder();
			row.append(eve.id);
			row.append(sep);
			row.append(eve.activity);
			row.append(sep);
			row.append(eve.timestamp.toInstant().toString());
			for (String objType : objectTypes) {
				row.append(sep);
				List<String> relObjs = new ArrayList<String>();
				for (OcelObject obj : eve.relatedObjects) {
					if (obj.objectType.name.equals(objType)) {
						relObjs.add(obj.id);
					}
				}
				if (relObjs.size() == 0) {
					row.append(" ");
				}
				else {
					row.append(quotechar);
					row.append("['");
					row.append(String.join("','", relObjs));
					row.append("']");
					row.append(quotechar);
				}
			}
			for (String attName : attributeNames) {
				row.append(sep);
				if (eve.attributes.containsKey(attName)) {
					row.append(eve.attributes.get(attName));
				}
				else {
					row.append(" ");
				}
			}
			ret.append(row.toString());
			ret.append(newline);
			
		}
		return ret.toString();
	}
}
