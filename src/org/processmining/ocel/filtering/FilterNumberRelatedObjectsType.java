package org.processmining.ocel.filtering;

import java.util.HashSet;
import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class FilterNumberRelatedObjectsType {
	public static OcelEventLog apply(OcelEventLog original, OcelObjectType objectType, int minOcc, int maxOcc) {
		OcelEventLog filtered = original.cloneEmpty();
		for (OcelEvent event : original.events.values()) {
			Set<OcelObject> relatedObjectsType = new HashSet<OcelObject>();
			for (OcelObject obj : event.relatedObjects) {
				if (obj.objectType.equals(objectType)) {
					relatedObjectsType.add(obj);
				}
			}
			if (minOcc <= relatedObjectsType.size() && relatedObjectsType.size() <= maxOcc) {
				filtered.cloneEvent(event);
			}
		}
		filtered.register();
		return filtered;
	}
}
