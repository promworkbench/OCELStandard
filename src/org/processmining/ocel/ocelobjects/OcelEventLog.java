package org.processmining.ocel.ocelobjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	public OcelEventLog cloneEmpty() {
		OcelEventLog cloned = new OcelEventLog();
		cloned.globalEvent = new HashMap<String, Object>(this.globalEvent);
		cloned.globalObject = new HashMap<String, Object>(this.globalObject);
		cloned.globalLog = new HashMap<String, Object>(this.globalLog);
		return cloned;
	}
	
	public void cloneEvent(OcelEvent event) {
		OcelEvent newEvent = event.clone();
		for (OcelObject obj : event.relatedObjects) {
			OcelObject newObj = cloneObject(obj);
			newEvent.relatedObjects.add(newObj);
			newEvent.relatedObjectsIdentifiers.add(newObj.id);
			newObj.relatedEvents.add(newEvent);
		}
		this.events.put(newEvent.id, newEvent);
	}
	
	public void cloneEvent(OcelEvent event, Set<String> allowedObjectTypes) {
		OcelEvent newEvent = event.clone();
		for (OcelObject obj : event.relatedObjects) {
			String thisType = obj.objectType.name;
			if (allowedObjectTypes.contains(thisType)) {
				OcelObject newObj = cloneObject(obj);
				newEvent.relatedObjects.add(newObj);
				newEvent.relatedObjectsIdentifiers.add(newObj.id);
				newObj.relatedEvents.add(newEvent);
			}
		}
		if (newEvent.relatedObjects.size() > 0) {
			this.events.put(newEvent.id, newEvent);
		}
	}
	
	public OcelObject cloneObject(OcelObject original) {
		if (!this.objects.containsKey(original.id)) {
			OcelObject newObject = new OcelObject(this);
			newObject.id = original.id;
			OcelObjectType otype = original.objectType;
			if (!this.objectTypes.containsKey(otype.name)) {
				this.objectTypes.put(otype.name, new OcelObjectType(this, otype.name));
			}
			newObject.objectType = this.objectTypes.get(otype.name);
			this.objects.put(newObject.id, newObject);
		}
		else {
		}
		return this.objects.get(original.id);
	}
	
	public Map<String, OcelEvent> getEvents() {
		return this.events;
	}
	
	public Map<String, OcelObject> getObjects() {
		return this.objects;
	}
	
	public List<String> getAttributeNames() {
		return (List<String>) this.globalLog.get("ocel:attribute-names");
	}
	
	public Set<String> getObjectTypes() {
		return (Set<String>)this.globalLog.get("ocel:object-types");
	}
	
	public String getVersion() {
		return (String)this.globalLog.get("ocel:version");
	}
	
	public Map<String, Object> getGlobalEvent() {
		return this.globalEvent;
	}
	
	public Map<String, Object> getGlobalObject() {
		return this.globalObject;
	}
}
