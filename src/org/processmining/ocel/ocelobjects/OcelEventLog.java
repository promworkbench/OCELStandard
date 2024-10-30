package org.processmining.ocel.ocelobjects;

import java.util.ArrayList;
import java.util.Date;
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
	public OcelEventLog preFilter;
	
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
		this.preFilter = this;
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
		cloned.preFilter = this;
		return cloned;
	}
	
	public void cloneEvent(OcelEvent event) {
		OcelEvent newEvent = event.clone();
		for (OcelObject obj : event.relatedObjects.keySet()) {
			OcelObject newObj = cloneObject(obj);
			newEvent.relatedObjects.put(newObj, event.relatedObjects.get(obj));
			newEvent.relatedObjectsIdentifiers.put(newObj.id, event.relatedObjects.get(obj));
			newObj.relatedEvents.add(newEvent);
		}
		this.events.put(newEvent.id, newEvent);
	}
	
	public void cloneEvent(OcelEvent event, Set<String> allowedObjectTypes) {
		OcelEvent newEvent = event.clone();
		for (OcelObject obj : event.relatedObjects.keySet()) {
			String thisType = obj.objectType.name;
			if (allowedObjectTypes.contains(thisType)) {
				OcelObject newObj = cloneObject(obj);
				newEvent.relatedObjects.put(newObj, event.relatedObjects.get(obj));
				newEvent.relatedObjectsIdentifiers.put(newObj.id, event.relatedObjects.get(obj));
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
	
	public Set<String> getAttributeNames() {
		return (Set<String>) this.globalLog.get("ocel:attribute-names");
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
	
    public Map<String, Integer> computeSummaryStatistics() {
        Map<String, Integer> summary = new HashMap<>();

        // 1. Number of events
        int numberOfEvents = this.events.size();
        summary.put("numberOfEvents", numberOfEvents);

        // 2. Number of objects
        int numberOfObjects = this.objects.size();
        summary.put("numberOfObjects", numberOfObjects);

        // 3. Number of event-to-object relationships
        int numberOfEventToObjectRelationships = 0;
        for (OcelEvent event : this.events.values()) {
            numberOfEventToObjectRelationships += event.relatedObjects.size();
        }
        summary.put("numberOfEventToObjectRelationships", numberOfEventToObjectRelationships);

        // 4. Number of object-to-object relationships
        Set<String> objectPairSet = new HashSet<>();
        for (OcelEvent event : this.events.values()) {
            List<String> objIds = new ArrayList<>(event.relatedObjectsIdentifiers.keySet());
            for (int i = 0; i < objIds.size(); i++) {
                for (int j = i + 1; j < objIds.size(); j++) {
                    String objId1 = objIds.get(i);
                    String objId2 = objIds.get(j);
                    // Create a consistent representation for the pair
                    String pairKey = objId1.compareTo(objId2) < 0 ?
                            objId1 + "," + objId2 :
                            objId2 + "," + objId1;
                    objectPairSet.add(pairKey);
                }
            }
        }
        int numberOfObjectToObjectRelationships = objectPairSet.size();
        summary.put("numberOfObjectToObjectRelationships", numberOfObjectToObjectRelationships);

        // 5. Number of changes to the object attribute values
        int numberOfObjectAttributeChanges = 0;
        for (OcelObject object : this.objects.values()) {
            for (Map<Date, Object> attributeChanges : object.timedAttributes.values()) {
                numberOfObjectAttributeChanges += attributeChanges.size();
            }
        }
        summary.put("numberOfObjectAttributeChanges", numberOfObjectAttributeChanges);

        return summary;
    }

    /**
     * Prints the summary statistics of the event log.
     */
    public void printSummaryStatistics() {
        Map<String, Integer> summary = computeSummaryStatistics();

        System.out.println("Summary Statistics:");
        System.out.println("-------------------");
        System.out.println("Number of Events: " + summary.get("numberOfEvents"));
        System.out.println("Number of Objects: " + summary.get("numberOfObjects"));
        System.out.println("Number of Event-to-Object Relationships: " + summary.get("numberOfEventToObjectRelationships"));
        System.out.println("Number of Object-to-Object Relationships: " + summary.get("numberOfObjectToObjectRelationships"));
        System.out.println("Number of Changes to Object Attribute Values: " + summary.get("numberOfObjectAttributeChanges"));
    }
}
