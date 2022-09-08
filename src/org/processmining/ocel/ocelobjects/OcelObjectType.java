package org.processmining.ocel.ocelobjects;

import java.util.HashSet;
import java.util.Set;

public class OcelObjectType {
	public OcelEventLog eventLog;
	public String name;
	public Set<OcelObject> objects;
	
	public OcelObjectType(OcelEventLog eventLog, String name) {
		this.eventLog = eventLog;
		this.name = name;
		this.objects = new HashSet<OcelObject>();
	}
}
