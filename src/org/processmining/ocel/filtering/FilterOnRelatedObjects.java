package org.processmining.ocel.filtering;

import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

public class FilterOnRelatedObjects {
	public static OcelEventLog apply(OcelEventLog original, Set<OcelObject> objects) {
		OcelEventLog filtered = original.cloneEmpty();
		for (OcelEvent ev : original.events.values()) {
			boolean is_ok = false;
			for (OcelObject obj : ev.relatedObjects.keySet()) {
				if (objects.contains(obj)) {
					is_ok = true;
					break;
				}
			}
			if (is_ok) {
				filtered.cloneEvent(ev);
			}
		}
		filtered.register();
		return filtered;
	}
}
