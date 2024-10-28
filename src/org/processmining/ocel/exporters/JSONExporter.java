package org.processmining.ocel.exporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

@Plugin(name = "Export OCEL to JSON file", parameterLabels = { "OcelEventLog", "File" }, returnLabels = { }, returnTypes = {})
@UIExportPlugin(description = "Export OCEL to JSON file", extension = "jsonocel")
public class JSONExporter {
	OcelEventLog eventLog;
	String filePath;
	
	public JSONExporter() {
		
	}
	
	public JSONExporter(OcelEventLog eventLog, String filePath) {
		this.eventLog = eventLog;
		this.filePath = filePath;
	}
	
	@PluginVariant(variantLabel = "Export OCEL to JSON file", requiredParameterLabels = { 0, 1 })
	public void exportFromProm(PluginContext context, OcelEventLog eventLog, File file) {
		this.eventLog = eventLog;
		this.filePath = file.getAbsolutePath();
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exportLogToStream(os);
	}
	
	public void exportLog() {
		FileOutputStream output0 = null;
		try {
			output0 = new FileOutputStream(this.filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exportLogToStream(output0);
	}
	
	public void exportLogToStream(OutputStream output0) {
		OutputStream output = null;
		if (this.filePath.endsWith("gz")) {
			try {
				output = new GZIPOutputStream(output0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			output = output0;
		}
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(output, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonEventLog = this.getJsonEventLog();
		try {
			writer.write(jsonEventLog);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getJsonEventLog() {
		JSONObject jsonEventLog = new JSONObject();
		this.setGlobalEvent(jsonEventLog);
		this.setGlobalObject(jsonEventLog);
		this.setGlobalLog(jsonEventLog);
		this.exportEvents(jsonEventLog);
		this.exportObjects(jsonEventLog);
		return jsonEventLog.toString(2);
	}
	
	public void setGlobalEvent(JSONObject jsonEventLog) {
		JSONObject globalEvent = new JSONObject();
		for (String att : this.eventLog.globalEvent.keySet()) {
			globalEvent.put(att, this.eventLog.globalEvent.get(att));
		}
		jsonEventLog.put("ocel:global-event", globalEvent);
	}
	
	public void setGlobalObject(JSONObject jsonEventLog) {
		JSONObject globalObject = new JSONObject();
		for (String att : this.eventLog.globalObject.keySet()) {
			globalObject.put(att, this.eventLog.globalObject.get(att));
		}
		jsonEventLog.put("ocel:global-object", globalObject);
	}
	
	public void setGlobalLog(JSONObject jsonEventLog) {
		JSONObject globalLog = new JSONObject();
		globalLog.put("ocel:version", this.eventLog.globalLog.get("ocel:version"));
		globalLog.put("ocel:ordering", this.eventLog.globalLog.get("ocel:ordering"));
		JSONArray attributeNames = new JSONArray();
		JSONArray objectTypes = new JSONArray();
		for (String att : ((Set<String>)this.eventLog.globalLog.get("ocel:attribute-names"))) {
			attributeNames.put(att);
		}
		for (String att : ((Set<String>)this.eventLog.globalLog.get("ocel:object-types"))) {
			objectTypes.put(att);
		}
		globalLog.put("ocel:attribute-names", attributeNames);
		globalLog.put("ocel:object-types", objectTypes);
		jsonEventLog.put("ocel:global-log", globalLog);
	}
	
	public void exportEvents(JSONObject jsonEventLog) {
		JSONObject events = new JSONObject();
		for (String eventId : this.eventLog.events.keySet()) {
			OcelEvent event = this.eventLog.events.get(eventId);
			JSONObject jsonEvent = new JSONObject();
			jsonEvent.put("ocel:activity", event.activity);
			jsonEvent.put("ocel:timestamp", event.timestamp.toInstant().toString());
			JSONArray omap = new JSONArray();
			for (OcelObject obj : event.relatedObjects.keySet()) {
				omap.put(obj.id);
			}
			JSONObject vmap = new JSONObject();
			for (String att : event.attributes.keySet()) {
				Object value = event.attributes.get(att);
				if (value.getClass() == java.util.Date.class) {
					vmap.put(att, ((Date)value).toInstant().toString());
				}
				else {
					vmap.put(att, value);
				}
			}
			jsonEvent.put("ocel:omap", omap);
			jsonEvent.put("ocel:vmap", vmap);
			events.put(eventId, jsonEvent);
		}
		jsonEventLog.put("ocel:events", events);
	}
	
	public void exportObjects(JSONObject jsonEventLog) {
		JSONObject objects = new JSONObject();
		for (String objectId : this.eventLog.objects.keySet()) {
			OcelObject object = this.eventLog.objects.get(objectId);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ocel:type", object.objectType.name);
			JSONObject ovmap = new JSONObject();
			for (String att : object.attributes.keySet()) {
				Object value = object.attributes.get(att);
				if (value.getClass() == java.util.Date.class) {
					ovmap.put(att, ((Date)value).toInstant().toString());
				}
				else {
					ovmap.put(att, value);
				}
			}
			jsonObject.put("ocel:ovmap", ovmap);
			objects.put(objectId, jsonObject);
		}
		jsonEventLog.put("ocel:objects", objects);
	}
}
