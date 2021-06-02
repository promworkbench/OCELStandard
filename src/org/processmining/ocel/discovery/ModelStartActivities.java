package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.Map;

import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class ModelStartActivities {
	OcelEventLog ocel;
	OcelObjectType ot;
	Map<String, Integer> numEvents;
	
	public ModelStartActivities(OcelEventLog ocel, OcelObjectType ot) {
		this.ocel = ocel;
		this.ot = ot;
		this.numEvents = new HashMap<String, Integer>();
		this.calculateEvents();
	}
	
	public void calculateEvents() {
		for (OcelObject obj : ot.objects) {
			if (obj.sortedRelatedEvents.size() > 0) {
				String act = obj.sortedRelatedEvents.get(0).activity;
				if (!numEvents.containsKey(act)) {
					numEvents.put(act, 0);
				}
				numEvents.put(act, numEvents.get(act) + 1);
			}
		}
	}
}
