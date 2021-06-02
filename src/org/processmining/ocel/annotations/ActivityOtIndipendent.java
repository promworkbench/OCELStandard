package org.processmining.ocel.annotations;

import java.util.HashSet;
import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

public class ActivityOtIndipendent {
	public OcelEventLog ocel;
	public String activity;
	public int numEvents;
	public int numUniqueObjects;
	public int numTotalObjects;
	
	public ActivityOtIndipendent(OcelEventLog ocel, String activity) {
		this.ocel = ocel;
		this.activity = activity;
		this.calculateNumEvents();
		this.calculateUniqueObjects();
		this.calculateTotalObjects();
	}
	
	public void calculateNumEvents() {
		this.numEvents = 0;
		for (String eve : ocel.events.keySet()) {
			OcelEvent event = ocel.events.get(eve);
			if (event.activity.equals(activity)) {
				this.numEvents++;
			}
		}
	}
	
	public void calculateUniqueObjects() {
		Set<OcelObject> relatedObjects = new HashSet<OcelObject>();
		for (String eve : ocel.events.keySet()) {
			OcelEvent event = ocel.events.get(eve);
			if (event.activity.equals(activity)) {
				for (OcelObject obj : event.relatedObjects) {
					relatedObjects.add(obj);
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
					this.numTotalObjects++;
				}
			}
		}
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(String.format("%s\n\n", this.activity));
		ret.append(String.format("events = %d\n", this.numEvents));
		ret.append(String.format("unique objects = %d\n", this.numUniqueObjects));
		ret.append(String.format("total objects = %d\n", this.numTotalObjects));
		return ret.toString();
	}
	
	public String toReducedString(int idx) {
		if (idx == 0) {
			return String.format("%s (E=%d)", this.activity, this.numEvents);
		}
		else if (idx == 1) {
			return String.format("%s (UO=%d)", this.activity, this.numUniqueObjects);
		}
		else if (idx == 2) {
			return String.format("%s (TO=%d)", this.activity, this.numTotalObjects);
		}
		return "";
	}
}
