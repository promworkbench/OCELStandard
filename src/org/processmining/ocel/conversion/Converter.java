package org.processmining.ocel.conversion;

import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class Converter {
	public static OcelEventLog convertBasic(XLog log) {
		OcelEventLog converted = new OcelEventLog();
		OcelObjectType caseType = new OcelObjectType(converted, "case");
		converted.objectTypes.put("case", caseType);
		int i = 0;
		int eventCount = 0;
		while (i < log.size()) {
			OcelObject thisCase = new OcelObject(converted);
			thisCase.objectType = caseType;
			thisCase.id = String.format("%d", i);
			converted.objects.put(thisCase.id, thisCase);
			for (XEvent event : log.get(i)) {
				XAttributeMap eventAttributeMap = event.getAttributes();
				XAttributeLiteral activity = (XAttributeLiteral) eventAttributeMap.get("concept:name");
				XAttributeTimestamp timestamp = (XAttributeTimestamp) eventAttributeMap.get("time:timestamp");
				OcelEvent eve = new OcelEvent(converted);
				eve.id = String.format("%d", eventCount);
				eve.activity = activity.getValue();
				eve.timestamp = timestamp.getValue();
				eve.relatedObjectsIdentifiers.put(thisCase.id, "");
				eve.relatedObjects.put(thisCase, "");
				thisCase.relatedEvents.add(eve);
				converted.events.put(eve.id, eve);
				eventCount++;
			}
			i++;
		}
		converted.register();
		return converted;
	}
}
