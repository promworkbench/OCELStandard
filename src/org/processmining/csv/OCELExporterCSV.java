package org.processmining.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

@UIExportPlugin(description = "Export OCEL to CSV file", extension = "csvocel")
public class OCELExporterCSV {
	@Plugin(name = "Export OCEL to CSV file", parameterLabels = { "OcelEventLog", "File" }, returnLabels = { }, returnTypes = {})
	@PluginVariant(variantLabel = "Export OCEL to CSV file", requiredParameterLabels = { 0, 1 })
	public void exportFromProm(PluginContext context, OcelEventLog eventLog, File file) {
		String csvContent = OCELExporterCSV.exportCsv(eventLog, "\r\n", ',', '\"');
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.write(csvContent.getBytes());
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
				for (OcelObject obj : eve.relatedObjects.keySet()) {
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
