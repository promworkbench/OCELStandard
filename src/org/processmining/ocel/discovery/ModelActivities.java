package org.processmining.ocel.discovery;

import java.util.HashSet;
import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class ModelActivities {
	OcelEventLog ocel;
	public Set<String> activities;
	
	public ModelActivities(OcelEventLog ocel) {
		this.ocel = ocel;
		this.activities = new HashSet<String>();
		this.calculate();
	}
	
	public void calculate() {
		for (String e : ocel.events.keySet()) {
			OcelEvent eve = ocel.events.get(e);
			activities.add(eve.activity);
		}
	}
}
