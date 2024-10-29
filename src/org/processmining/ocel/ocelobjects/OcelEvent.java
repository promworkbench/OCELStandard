package org.processmining.ocel.ocelobjects;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OcelEvent {
	public OcelEventLog eventLog;
	public String id;
	public String activity;
	public Date timestamp;
	public Map<String, String> relatedObjectsIdentifiers;
	public Map<OcelObject, String> relatedObjects;
	public Map<String, Object> attributes;
	
	public OcelEvent(OcelEventLog eventLog) {
		this.eventLog = eventLog;
		this.relatedObjectsIdentifiers = new HashMap<String, String>();
		this.relatedObjects = new HashMap<OcelObject, String>();
		this.attributes = new HashMap<String, Object>();
	}
	
	public void register() {
		for (String reObj : relatedObjectsIdentifiers.keySet()) {
			OcelObject obj = this.eventLog.objects.get(reObj);
			if (obj != null) {
				this.relatedObjects.put(obj, relatedObjectsIdentifiers.get(reObj));
				obj.relatedEvents.add(this);
			}
		}
		for (String att : attributes.keySet()) {
			((Set<String>)this.eventLog.globalLog.get("ocel:attribute-names")).add(att);
		}
	}
	
	public OcelEvent clone() {
		OcelEvent newEvent = new OcelEvent(this.eventLog);
		newEvent.id = this.id;
		newEvent.activity = this.activity;
		newEvent.timestamp = this.timestamp;
		newEvent.attributes = new HashMap<String, Object>(this.attributes);
		return newEvent;
	}
}
