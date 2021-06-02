package org.processmining.ocel.discovery;

import java.util.HashSet;
import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.utils.Separator;

public class ActivityOtGroups {
	OcelEventLog ocel;
	public Set<String> activityOtGroups;
	
	public ActivityOtGroups(OcelEventLog ocel) {
		this.ocel = ocel;
		this.activityOtGroups = new HashSet<String>();
		this.findGroups();
	}
	
	public void findGroups() {
		for (String eve : ocel.events.keySet()) {
			OcelEvent event = ocel.events.get(eve);
			String activity = event.activity;
			for (OcelObject obj : event.relatedObjects) {
				String otName = obj.objectType.name;
				activityOtGroups.add(activity+Separator.SEPARATOR+otName);
			}
		}
	}
}
