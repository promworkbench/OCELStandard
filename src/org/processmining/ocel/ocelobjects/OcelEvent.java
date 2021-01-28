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
	public Set<String> relatedObjectsIdentifiers;
	public Set<OcelObject> relatedObjects;
	public Map<String, Object> attributes;
	
	public OcelEvent(OcelEventLog eventLog) {
		this.eventLog = eventLog;
		this.relatedObjectsIdentifiers = new HashSet<String>();
		this.relatedObjects = new HashSet<OcelObject>();
		this.attributes = new HashMap<String, Object>();
	}
	
	public void register() {
		for (String reObj : relatedObjectsIdentifiers) {
			OcelObject obj = this.eventLog.objects.get(reObj);
			this.relatedObjects.add(obj);
			obj.relatedEvents.add(this);
		}
		for (String att : attributes.keySet()) {
			((Set<String>)this.eventLog.globalLog.get("ocel:attribute-names")).add(att);
		}
	}
}
