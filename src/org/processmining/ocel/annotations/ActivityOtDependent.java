package org.processmining.ocel.annotations;

import java.util.HashSet;
import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class ActivityOtDependent {
	public OcelEventLog ocel;
	public String activity;
	public OcelObjectType objectType;
	public int numEvents;
	public int numUniqueObjects;
	public int numTotalObjects;
	public int minRelatedObjects;
	public int maxRelatedObjects;
	
	public ActivityOtDependent(OcelEventLog ocel, String activity, String objectType) {
		this.ocel = ocel;
		this.activity = activity;
		this.objectType = ocel.objectTypes.get(objectType);
		this.calculateNumEvents();
		this.calculateUniqueObjects();
		this.calculateTotalObjects();
		this.calculateMinMaxRelatedObjects();
	}
	
	public void calculateNumEvents() {
		this.numEvents = 0;
		for (String eve : ocel.events.keySet()) {
			OcelEvent event = ocel.events.get(eve);
			if (event.activity.equals(activity)) {
				for (OcelObject obj : event.relatedObjects) {
					if (obj.objectType.equals(objectType)) {
						this.numEvents++;
						break;
					}
				}
			}
		}
	}
	
	public void calculateUniqueObjects() {
		Set<OcelObject> relatedObjects = new HashSet<OcelObject>();
		for (String eve : ocel.events.keySet()) {
			OcelEvent event = ocel.events.get(eve);
			if (event.activity.equals(activity)) {
				for (OcelObject obj : event.relatedObjects) {
					if (obj.objectType.equals(objectType)) {
						relatedObjects.add(obj);
					}
				}
			}
		}
		this.numUniqueObjects = relatedObjects.size();
	}
	
	public void calculateTotalObjects() {
		this.numTotalObjects = 0;
		for (String eve : ocel.events.keySet()) {
			OcelEvent event = ocel.events.get(eve);
			if (event.activity.equals(activity)) {
				for (OcelObject obj : event.relatedObjects) {
					if (obj.objectType.equals(objectType)) {
						this.numTotalObjects++;
					}
				}
			}
		}
	}
	
	public void calculateMinMaxRelatedObjects() {
		this.maxRelatedObjects = 0;
		this.minRelatedObjects = Integer.MAX_VALUE;
		
		for (String eve : ocel.events.keySet()) {
			OcelEvent event = ocel.events.get(eve);
			int numRelatedObjectsOt = 0;
			for (OcelObject obj : event.relatedObjects) {
				if (obj.objectType.equals(objectType)) {
					numRelatedObjectsOt++;
				}
			}
			this.maxRelatedObjects = Integer.max(numRelatedObjectsOt, this.maxRelatedObjects);
			this.minRelatedObjects = Integer.min(numRelatedObjectsOt, this.minRelatedObjects);
		}
	}
}
