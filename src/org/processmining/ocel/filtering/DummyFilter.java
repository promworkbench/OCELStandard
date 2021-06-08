package org.processmining.ocel.filtering;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class DummyFilter {
	public static OcelEventLog apply(OcelEventLog original) {
		OcelEventLog filtered = original.cloneEmpty();
		for (OcelEvent eve : original.events.values()) {
			filtered.cloneEvent(eve);
		}
		filtered.register();
		return filtered;
	}
}
