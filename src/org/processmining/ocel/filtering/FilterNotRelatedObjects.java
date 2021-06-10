package org.processmining.ocel.filtering;

import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

public class FilterNotRelatedObjects {
	public static OcelEventLog apply(OcelEventLog original, Set<OcelObject> positive, Set<OcelObject> negative) {
		OcelEventLog filtered = original.cloneEmpty();
		for (OcelEvent ev : original.events.values()) {
			boolean is_ok1 = false;
			boolean is_ok2 = true;
			for (OcelObject obj : ev.relatedObjects) {
				if (positive.contains(obj)) {
					is_ok1 = true;
					break;
				}
			}
			for (OcelObject obj : ev.relatedObjects) {
				if (negative.contains(obj)) {
					is_ok2 = false;
					break;
				}
			}
			if (is_ok1 && is_ok2) {
				filtered.cloneEvent(ev);
			}
		}
		filtered.register();
		return filtered;
	}
}
