package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.Map;

import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class ModelEndActivities {
	OcelEventLog ocel;
	OcelObjectType ot;
	Map<String, Integer> endActivities;
	
	public ModelEndActivities(OcelEventLog ocel, OcelObjectType ot) {
		this.ocel = ocel;
		this.endActivities = new HashMap<String, Integer>();
		this.calculate();
	}
	
	public void calculate() {
		for (OcelObject obj : ot.objects) {
			String act = obj.sortedRelatedEvents.get(0).activity;
			if (!endActivities.containsKey(act)) {
				endActivities.put(act, 0);
			}
			endActivities.put(act, endActivities.get(act) + 1);
		}
	}
}
