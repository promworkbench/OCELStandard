package org.processmining.ocel.flattening;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.utils.TypeFromValue;

@Plugin(name = "Advanced flattening of OCEL log (SAP document flow)",
returnLabels = { "Traditional Event Log" },
returnTypes = { XLog.class },
parameterLabels = { "Object-Centric Event Log" },
help = "Object-Centric Event Log",
userAccessible = true)
public class AdvancedFlattening {
	@PluginVariant(requiredParameterLabels = {0})
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public XLog advancedFlattening(UIPluginContext context, OcelEventLog ocel) {
		AdvancedFlatteningWizardStep1 wizStep1 = new AdvancedFlatteningWizardStep1(ocel);
		AdvancedFlatteningWizardStep2 wizStep2 = new AdvancedFlatteningWizardStep2(ocel);
		List<ProMWizardStep<AdvancedFlatteningParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep1);
		wizStepList.add(wizStep2);
		ListWizard<AdvancedFlatteningParameters> listWizard = new ListWizard<>(wizStepList);
		AdvancedFlatteningParameters parameters = ProMWizardDisplay.show(context, listWizard, new AdvancedFlatteningParameters());
		XLog ret = AdvancedFlattening.flatten(ocel, parameters.getCurrTypeAtt(), parameters.getCurrTypeAllowedValue(), parameters.getPrevDoc(), parameters.getCurrDoc(), parameters.getOtherKey());
		return ret;
	}
	
	public static Set<String> expandPrevious(String doc, Map<String, String> previous) {
		Set<String> visited = new HashSet<String>();
		List<String> toVisit = new ArrayList<String>();
		toVisit.add(doc);
		while (toVisit.size() > 0) {
			String curr = toVisit.get(0);
			toVisit.remove(0);
			if (visited.contains(curr)) {
				continue;
			}
			visited.add(curr);
			if (previous.containsKey(curr)) {
				toVisit.add(previous.get(curr));
			}
		}
		return visited;
	}
	
	public static Set<String> expandNext(String doc, Map<String, Set<String>> next) {
		Set<String> visited = new HashSet<String>();
		List<String> toVisit = new ArrayList<String>();
		toVisit.add(doc);
		while (toVisit.size() > 0) {
			String curr = toVisit.get(0);
			toVisit.remove(0);
			if (visited.contains(curr)) {
				continue;
			}
			visited.add(curr);
			if (next.containsKey(curr)) {
				for (String d : next.get(curr)) {
					toVisit.add(d);
				}
			}
		}
		return visited;
	}
	
	public static XLog flatten(OcelEventLog ocel, String currTypeAtt, String currTypeAllowedValue, String prevDoc, String currDoc, String otherKey) {
		XAttributeMap logAttributes = new XAttributeMapImpl();
		XLog log = new XLogImpl(logAttributes);
		
		List<String> currTypeEvents = new ArrayList<String>();
		Map<String, Set<OcelEvent>> currDocToEvents = new HashMap<String, Set<OcelEvent>>();
		for (String eveId : ocel.getEvents().keySet()) {
			OcelEvent eve = ocel.getEvents().get(eveId);
			if (eve.attributes.containsKey(currDoc)) {
				String currDocValue = (String)eve.attributes.get(currDoc);
				if (!currDocToEvents.containsKey(currDocValue)) {
					currDocToEvents.put(currDocValue, new HashSet<OcelEvent>());
				}
				currDocToEvents.get(currDocValue).add(eve);
				if (eve.attributes.containsKey(currTypeAtt)) {
					if (eve.attributes.get(currTypeAtt).equals(currTypeAllowedValue)) {
						currTypeEvents.add(currDocValue);
					}
				}
			}
			if (eve.attributes.containsKey(otherKey)) {
				String otherKeyValue = (String)eve.attributes.get(otherKey);
				if (!currDocToEvents.containsKey(otherKeyValue)) {
					currDocToEvents.put(otherKeyValue, new HashSet<OcelEvent>());
				}
				currDocToEvents.get(otherKeyValue).add(eve);
			}
		}
		
		Map<String, String> previousEvent = new HashMap<String, String>();
		Map<String, Set<String>> nextEvent = new HashMap<String, Set<String>>();
		
		for (OcelEvent eve : ocel.getEvents().values()) {
			if (eve.attributes.containsKey(prevDoc) && eve.attributes.get(prevDoc) != "" && eve.attributes.get(prevDoc) != null) {
				String currDocValue = (String)eve.attributes.get(currDoc);
				String prevDocValue = (String)eve.attributes.get(prevDoc);
				previousEvent.put(currDocValue, prevDocValue);
				if (!nextEvent.containsKey(prevDocValue)) {
					nextEvent.put(prevDocValue, new HashSet<String>());
				}
				nextEvent.get(prevDocValue).add(currDocValue);
			}
		}
		
		for (String doc : currTypeEvents) {
			Set<String> visitedPrevious = AdvancedFlattening.expandPrevious(doc, previousEvent);
			Set<String> visitedNext = AdvancedFlattening.expandNext(doc,  nextEvent);
			visitedPrevious.addAll(visitedNext);
			Set<OcelEvent> relatedEvents = new HashSet<OcelEvent>();
			for (String d2 : visitedPrevious) {
				if (currDocToEvents.containsKey(d2)) {
					for (OcelEvent eve : currDocToEvents.get(d2)) {
						relatedEvents.add(eve);
					}
				}
			}
			XAttributeMap traceAttributes = new XAttributeMapImpl();
			XAttribute caseId = new XAttributeLiteralImpl("concept:name", doc);
			traceAttributes.put("concept:name", caseId);
			XTrace trace = new XTraceImpl(traceAttributes);
			for (OcelEvent ocelEvent : relatedEvents) {
				XAttributeMap eventAttributes = new XAttributeMapImpl();
				XAttribute eventId = new XAttributeLiteralImpl("event_id", ocelEvent.id);
				eventAttributes.put("event_id", eventId);
				XAttribute conceptName = new XAttributeLiteralImpl("concept:name", ocelEvent.activity);
				eventAttributes.put("concept:name", conceptName);
				XAttribute timeTimestamp = new XAttributeTimestampImpl("time:timestamp", ocelEvent.timestamp);
				eventAttributes.put("time:timestamp", timeTimestamp);
				for (String attribute : ocelEvent.attributes.keySet()) {
					Object attributeValue = ocelEvent.attributes.get(attribute);
					XAttribute xatt = TypeFromValue.getAttributeForValue(attribute, attributeValue);
					eventAttributes.put(attribute, xatt);
				}
				XEvent event = new XEventImpl(eventAttributes);
				trace.add(event);
			}
			log.add(trace);
		}
		
		return log;
	}
}
