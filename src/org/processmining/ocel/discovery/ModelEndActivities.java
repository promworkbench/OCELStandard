package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.Map;

import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class ModelEndActivities {
	OcelEventLog ocel;
	OcelObjectType ot;
	Map<String, Integer> numEvents;
	
	public ModelEndActivities(OcelEventLog ocel, OcelObjectType ot) {
		this.ocel = ocel;
		this.ot = ot;
		this.numEvents = new HashMap<String, Integer>();
		this.calculate();
	}
	
	public void calculate() {
		for (OcelObject obj : ot.objects) {
			if (obj.sortedRelatedEvents.size() > 0) {
				String act = obj.sortedRelatedEvents.get(obj.sortedRelatedEvents.size() - 1).activity;
				if (!numEvents.containsKey(act)) {
					numEvents.put(act, 0);
				}
				numEvents.put(act, numEvents.get(act) + 1);
			}
		}
	}
}
