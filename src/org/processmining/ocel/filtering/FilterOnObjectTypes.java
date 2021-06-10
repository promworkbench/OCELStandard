package org.processmining.ocel.filtering;

import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class FilterOnObjectTypes {
	public static OcelEventLog apply(OcelEventLog original, Set<String> allowedObjectTypes) {
		OcelEventLog filtered = original.cloneEmpty();
		for (OcelEvent event : original.events.values()) {
			filtered.cloneEvent(event, allowedObjectTypes);
		}
		filtered.register();
		return filtered;
	}
}
