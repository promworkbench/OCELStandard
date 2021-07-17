package org.processmining.ocel.replay;

import java.util.Set;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;

public class FilterLogOnActivities {
	public static XLog filterLogOnActivities(XLog log, Set<String> activities) {
		XAttributeMap filteredLogAttributeMap = new XAttributeMapImpl();
		XLog filteredLog = new XLogImpl(filteredLogAttributeMap);
		for (XTrace trace : log) {
			XAttributeMap filteredTraceAttributeMap = new XAttributeMapImpl();
			XTrace filteredTrace = new XTraceImpl(filteredTraceAttributeMap);
			for (XEvent eve : trace) {
				String activity = eve.getAttributes().get("concept:name").toString();
				if (activities.contains(activity)) {
					filteredTrace.add(eve);
				}
			}
			if (filteredTrace.size() > 0) {
				filteredLog.add(filteredTrace);
			}
		}
		return filteredLog;
	}
}
