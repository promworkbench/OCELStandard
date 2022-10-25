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
	
	public static String objTypesTextForInput(OcelEventLog original) {
		StringBuilder ret = new StringBuilder();
		Map<String, Integer> otCount = objectTypesCount(original);
		for (String objType : otCount.keySet()) {
			ret.append(objType+">>>YES");
			ret.append("\n");
		}
		return ret.toString();
	}
	
	public static OcelEventLog filterOnProvidedTextInput(OcelEventLog original, String providedInput) {
		String[] objTypesLines = providedInput.split("\n");
		Set<String> allowedObjTypes = new HashSet<String>();
		for (String line : objTypesLines) {
			String[] spli = line.split(">>>");
			if (spli.length >= 2) {
				if (spli[1].equals("YES")) {
					allowedObjTypes.add(spli[0]);
				}
			}
		}
		return FilterOnObjectTypes.apply(original, allowedObjTypes);
	}
}
