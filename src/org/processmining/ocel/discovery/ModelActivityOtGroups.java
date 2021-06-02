package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

public class ModelActivityOtGroups {
	OcelEventLog ocel;
	public Map<String, Set<String>> activityOtGroups;
	
	public ModelActivityOtGroups(OcelEventLog ocel) {
		this.ocel = ocel;
		this.activityOtGroups = new HashMap<String, Set<String>>();
		this.findGroups();
	}
	
	public void findGroups() {
		for (String eve : ocel.events.keySet()) {
			OcelEvent event = ocel.events.get(eve);
			String activity = event.activity;
			if (!this.activityOtGroups.containsKey(activity)) {
				this.activityOtGroups.put(activity, new HashSet<String>());
			}
			for (OcelObject obj : event.relatedObjects) {
				String otName = obj.objectType.name;
				activityOtGroups.get(activity).add(otName);
			}
		}
	}
}
