package org.processmining.ocel.ocelobjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class OcelEventLog {
	public Map<String, OcelEvent> events;
	public Map<String, OcelObject> objects;
	public Map<String, OcelObjectType> objectTypes;
	public Map<String, Object> globalEvent;
	public Map<String, Object> globalObject;
	public Map<String, Object> globalLog;
	
	public OcelEventLog() {
		this.events = new HashMap<String, OcelEvent>();
		this.objects = new HashMap<String, OcelObject>();
		this.objectTypes = new HashMap<String, OcelObjectType>();
		this.globalEvent = new HashMap<String, Object>();
		this.globalObject = new HashMap<String, Object>();
		this.globalLog = new HashMap<String, Object>();
		this.globalLog.put("ocel:version", "1.0");
		this.globalLog.put("ocel:ordering", "timestamp");
		this.globalLog.put("ocel:attribute-names", new HashSet<String>());
		this.globalLog.put("ocel:object-types", new HashSet<String>());
	}
	
	public void register() {
		for (String eve : this.events.keySet()) {
			this.events.get(eve).register();
		}
		for (String obj : this.objects.keySet()) {
			this.objects.get(obj).register();
		}
	}
}
