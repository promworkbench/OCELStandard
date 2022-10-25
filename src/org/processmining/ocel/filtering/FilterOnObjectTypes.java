package org.processmining.ocel.filtering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;

public class FilterOnObjectTypes {
	public static OcelEventLog apply(OcelEventLog original, Set<String> allowedObjectTypes) {
		OcelEventLog filtered = original.cloneEmpty();
		for (OcelEvent event : original.events.values()) {
			filtered.cloneEvent(event, allowedObjectTypes);
		}
		filtered.register();
		return filtered;
	}
	
	public static OcelEventLog applyFrequency(OcelEventLog original, Integer minFreq) {
		Map<String, Integer> objTypCount = objectTypesCount(original);
		Set<String> allowed = new HashSet<String>();
		for (String typ : objTypCount.keySet()) {
			if (objTypCount.get(typ) >= minFreq) {
				allowed.add(typ);
			}
		}
		return apply(original, allowed);
	}
	
	public static Map<String, Integer> objectTypesCount(OcelEventLog original) {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		for (String objId : original.objects.keySet()) {
			OcelObject obj = original.objects.get(objId);
			String objType = obj.objectType.name;
			if (!(ret.containsKey(objType))) {
				ret.put(objType, 1);
			}
			else {
				ret.put(objType, ret.get(objType) + 1);
			}
		}
		return ret;
	}
}
