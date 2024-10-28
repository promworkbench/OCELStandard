package org.processmining.ocel.annotations;

import java.util.HashSet;
import java.util.Set;

import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

public class ActivityOtIndipendent {
	public OcelEventLog ocel;
	public AnnotatedModel model;
	public String activity;
	public int numEvents;
	public int numUniqueObjects;
	public int numTotalObjects;
	
	public ActivityOtIndipendent(OcelEventLog ocel, AnnotatedModel model, String activity) {
		this.ocel = ocel;
		this.model = model;
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
				for (OcelObject obj : event.relatedObjects.keySet()) {
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
				for (OcelObject obj : event.relatedObjects.keySet()) {
					this.numTotalObjects++;
				}
			}
		}
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(String.format("%s\n\n", this.activity));
		ret.append(String.format("E = %d  ", this.numEvents));
		ret.append(String.format("UO = %d  ", this.numUniqueObjects));
		ret.append(String.format("TO = %d\n\n", this.numTotalObjects));
		if (model.dependentNodeMeasures.containsKey(this.activity)) {
			for (String ot : model.dependentNodeMeasures.get(activity).keySet()) {
				ActivityOtDependent zz = model.dependentNodeMeasures.get(activity).get(ot);
				ret.append(zz.toIntermediateString(true) + "\n");
			}
		}
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
	
	public boolean satisfy(int idx, int count) {
		return this.getValue(idx) >= count;
	}
	
	public int getValue(int idx) {
		if (idx == 0) {
			return this.numEvents;
		}
		else if (idx == 1) {
			return this.numUniqueObjects;
		}
		else if (idx == 2) {
			return this.numTotalObjects;
		}
		return 0;
	}
}
