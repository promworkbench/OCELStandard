package org.processmining.csv;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class OCELConverter {
	public static Map<Integer, String> getDefaultMapping(List<List<String>> parsedCsv) {
		Map<Integer, String> columns = new HashMap<Integer, String>();
		String actiColumn = "";
		String timestColumn = "";
		String eidColumn = "";
		List<String> objectTypesColumns = new ArrayList<String>();
		for (String column : parsedCsv.get(0)) {
			if (column.toLowerCase().contains("time")) {
				timestColumn = column;
			}
			else if (column.toLowerCase().contains("acti")) {
				actiColumn = column;
			}
			else if (column.toLowerCase().contains("eid")) {
				eidColumn = column;
			}
			else if (column.toLowerCase().contains("type")) {
				objectTypesColumns.add(column);
			}
		}
		int i = 0;
		for (String column : parsedCsv.get(0)) {
			if (!(column.equals(actiColumn)) && !(column.equals(timestColumn)) && !(column.equals(eidColumn)) && (!(objectTypesColumns.contains(column)))) {
				columns.put(i, "attribute");
			}
			else if (column.equals(actiColumn)) {
				columns.put(i, "activity");
			}
			else if (column.equals(timestColumn)) {
				columns.put(i, "timestamp");
			}
			else if (column.equals(eidColumn)) {
				columns.put(i, "evid");
			}
			else {
				columns.put(i, "objtype");
			}
			i++;
		}
		return columns;
	}
	
	public static OcelEventLog getOCELfromParsedCSV(List<List<String>> parsedCsv, Map<Integer, String> columns, String objSepType) {
		List<String> attributeNames = new ArrayList<String>();
		List<String> objectTypes = new ArrayList<String>();
		int i = 0;
		while (i < parsedCsv.get(0).size()) {
			String col = parsedCsv.get(0).get(i);
			if (columns.get(i).equals("objtype")) {
				String[] colspli = col.split("ocel:type:");
				objectTypes.add(colspli[colspli.length-1]);
			}
			else if (columns.get(i).equals("attribute")) {
				attributeNames.add(col);
			}
			i++;
		}
		OcelEventLog ocel = new OcelEventLog();
		ocel.globalLog.put("ocel:version", "1.0");
		ocel.globalLog.put("ocel:ordering", "timestamp");
		ocel.globalLog.put("ocel:attribute-names", attributeNames);
		ocel.globalLog.put("ocel:object-types", objectTypes);
		
		for (String col : objectTypes) {
			OcelObjectType type = new OcelObjectType(ocel, col);
			ocel.objectTypes.put(col, type);
		}
		
		i = 1;
		while (i < parsedCsv.size()) {
			OcelEvent eve = new OcelEvent(ocel);
			int j = 0;
			while (j < parsedCsv.get(0).size()) {
				if (columns.get(j).equals("evid")) {
					eve.id = parsedCsv.get(i).get(j);
				}
				else if (columns.get(j).equals("activity")) {
					eve.activity = parsedCsv.get(i).get(j);
				}
				else if (columns.get(j).equals("timestamp")) {
					eve.timestamp = Date.from(LocalDate.parse( parsedCsv.get(i).get(j) ).atStartOfDay().toInstant(ZoneOffset.UTC));
				}
				else if (columns.get(j).equals("attribute")) {
					eve.attributes.put(parsedCsv.get(0).get(j), parsedCsv.get(i).get(j));
				}
				else if (columns.get(j).equals("objtype")) {
					String col = parsedCsv.get(0).get(j);
					String[] colspli = col.split("ocel:type:");
					String currObjType = colspli[colspli.length - 1];
					OcelObjectType currObjTypeObj = ocel.objectTypes.get(currObjType);
					String theseObjects = parsedCsv.get(i).get(j);
					if (theseObjects.length() > 2) {
						theseObjects = theseObjects.substring(1, theseObjects.length() - 1);
						List<String> theseObjectsAsList = new ArrayList<String>();
						if (objSepType == "absent") {
							String[] splitResult = theseObjects.split(",");
							for (String s : splitResult) {
								theseObjectsAsList.add(s);
							}
						}
						else {
							int z = 0;
							boolean reading = false;
							String currString = "";
							while (z < theseObjects.length()) {
								if (theseObjects.charAt(z) == '\'') {
									if (reading == false) {
										reading = true;
										currString = "";
									}
									else {
										reading = false;
										theseObjectsAsList.add(currString);
									}
								}
								else {
									currString += theseObjects.charAt(z);
								}
								z++;
							}
						}
						for (String obj : theseObjectsAsList) {
							OcelObject objObj = null;
							if (!(ocel.objects.containsKey(obj))) {
								objObj = new OcelObject(ocel);
								objObj.id = obj;
								objObj.objectType = currObjTypeObj;
								ocel.objects.put(obj, objObj);
							}
							else {
								objObj = ocel.objects.get(obj);
							}
							eve.relatedObjects.add(objObj);
							eve.relatedObjectsIdentifiers.add(obj);
						}
					}
				}
				j++;
			}
			if (eve.id == null) {
				eve.id = "e" + i;
			}
			ocel.events.put(eve.id, eve);
			i++;
		}
		ocel.register();
		return ocel;
	}
	
	
}
