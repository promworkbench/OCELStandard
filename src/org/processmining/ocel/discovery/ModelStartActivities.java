package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.Map;

import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class ModelStartActivities {
	OcelEventLog ocel;
	OcelObjectType ot;
	Map<String, Integer> startActivities;
	
	public ModelStartActivities(OcelEventLog ocel, OcelObjectType ot) {
		this.ocel = ocel;
		this.startActivities = new HashMap<String, Integer>();
		this.calculate();
	}
	
	public void calculate() {
		for (OcelObject obj : ot.objects) {
			String act = obj.sortedRelatedEvents.get(0).activity;
			if (!startActivities.containsKey(act)) {
				startActivities.put(act, 0);
			}
			startActivities.put(act, startActivities.get(act) + 1);
		}
	}
}
