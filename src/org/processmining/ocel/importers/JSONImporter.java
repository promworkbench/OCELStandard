package org.processmining.ocel.importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@Plugin(name = "Import OCEL from JSON", parameterLabels = { "Filename" }, returnLabels = {
"Object-Centric Event Log" }, returnTypes = { OcelEventLog.class })
@UIImportPlugin(description = "Import OCEL from JSON", extensions = { "jsonocel", "gz" })
public class JSONImporter extends AbstractImportPlugin {
	String logPath;
	
	public JSONImporter() {
		
	}
	
	public JSONImporter(String logPath) {
		this.logPath = logPath;
	}
	
	protected OcelEventLog importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		this.logPath = filename;
		return doImportFromStream(input);
	}
	
	public OcelEventLog doImport() {
		File file = new File(this.logPath);
		InputStream is0 = null;
		try {
			is0 = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doImportFromStream(is0);
	}
	
	public OcelEventLog doImportFromStream(InputStream is0) {
		InputStream is = null;
		if (this.logPath.endsWith(".gz")) {
			try {
				is = new GZIPInputStream(is0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			is = is0;
		}
		OcelEventLog eventLog = new OcelEventLog();
		JSONTokener tokener = new JSONTokener(is);
		JSONObject object = new JSONObject(tokener);
		importGlobalEvent(eventLog, object.getJSONObject("ocel:global-event"));
		importGlobalObject(eventLog, object.getJSONObject("ocel:global-object"));
		importGlobalLog(eventLog, object.getJSONObject("ocel:global-log"));
		importEvents(eventLog, object.getJSONObject("ocel:events"));
		importObjects(eventLog, object.getJSONObject("ocel:objects"));
		eventLog.register();
		return eventLog;
	}
	
	public void importGlobalEvent(OcelEventLog eventLog, JSONObject globalEvent) {
		for (String key : globalEvent.keySet()) {
			eventLog.globalEvent.put(key, (Object)globalEvent.getString(key));
		}
	}
	
	public void importGlobalObject(OcelEventLog eventLog, JSONObject globalObject) {
		for (String key : globalObject.keySet()) {
			eventLog.globalObject.put(key, (Object)globalObject.getString(key));
		}
	}
	
	public void importGlobalLog(OcelEventLog eventLog, JSONObject globalLog) {
		eventLog.globalLog.put("ocel:version", globalLog.get("ocel:version"));
		eventLog.globalLog.put("ocel:ordering", globalLog.get("ocel:ordering"));
		JSONArray attributeNames = (JSONArray)globalLog.get("ocel:attribute-names");
		Integer i = 0;
		while (i < attributeNames.length()) {
			((Set<String>)eventLog.globalLog.get("ocel:attribute-names")).add((String)attributeNames.get(i));
			i = i + 1;
		}
		JSONArray objectTypes = (JSONArray)globalLog.get("ocel:object-types");
		i = 0;
		while (i < objectTypes.length()) {
			((Set<String>)eventLog.globalLog.get("ocel:object-types")).add((String)objectTypes.get(i));
			i = i + 1;
		}
	}
	
	public void importEvents(OcelEventLog eventLog, JSONObject events) {
		for (String eventId : events.keySet()) {
			JSONObject jsonEvent = events.getJSONObject(eventId);
			OcelEvent event = new OcelEvent(eventLog);
			event.id = eventId;
			event.activity = jsonEvent.getString("ocel:activity");
			String timestampStr = jsonEvent.getString("ocel:timestamp");
			timestampStr = timestampStr.replaceAll(" ", "T");

			DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

			TemporalAccessor temporalAccessor = formatter.parseBest(
			    timestampStr, 
			    OffsetDateTime::from, 
			    LocalDateTime::from
			);

			Instant instant;
			if (temporalAccessor instanceof OffsetDateTime) {
			    instant = ((OffsetDateTime) temporalAccessor).toInstant();
			} else {
			    instant = ((LocalDateTime) temporalAccessor).atZone(ZoneOffset.UTC).toInstant();
			}

			event.timestamp = Date.from(instant);
			
			JSONArray jsonRelatedObjects = jsonEvent.getJSONArray("ocel:omap");
			int i = 0;
			while (i < jsonRelatedObjects.length()) {
				event.relatedObjectsIdentifiers.put(jsonRelatedObjects.getString(i), "");
				i++;
			}
			JSONObject jsonVmap = jsonEvent.getJSONObject("ocel:vmap");
			for (String att_key : jsonVmap.keySet()) {
				event.attributes.put(att_key, jsonVmap.get(att_key));
			}
			eventLog.events.put(eventId, event);
		}
	}
	
	public void importObjects(OcelEventLog eventLog, JSONObject objects) {
		for (String objectId : objects.keySet()) {
			JSONObject jsonObject = objects.getJSONObject(objectId);
			OcelObject object = new OcelObject(eventLog);
			object.id = objectId;
			String objectTypeName = jsonObject.getString("ocel:type");
			if (!(eventLog.objectTypes.containsKey(objectTypeName))) {
				OcelObjectType objectType = new OcelObjectType(eventLog, objectTypeName);
				eventLog.objectTypes.put(objectTypeName, objectType);
			}
			OcelObjectType objectType = eventLog.objectTypes.get(objectTypeName);
			object.objectType = objectType;
			JSONObject jsonVmap = jsonObject.getJSONObject("ocel:ovmap");
			for (String att_key : jsonVmap.keySet()) {
				object.attributes.put(att_key, jsonVmap.get(att_key));
			}
			eventLog.objects.put(objectId, object);
		}
	}
}
