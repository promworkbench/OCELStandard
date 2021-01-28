package org.processmining.ocel.ocelobjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OcelObject {
	public OcelEventLog eventLog;
	public String id;
	public OcelObjectType objectType;
	public Set<OcelEvent> relatedEvents;
	public Map<String, Object> attributes;
	
	public OcelObject(OcelEventLog eventLog) {
		this.eventLog = eventLog;
		this.relatedEvents = new HashSet<OcelEvent>();
		this.attributes = new HashMap<String, Object>();
	}
	
	public void register() {
		this.objectType.objects.add(this);
	}
}
