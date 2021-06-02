package org.processmining.ocel.flattening;

import java.util.ArrayList;
import java.util.List;

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
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.utils.TypeFromValue;

@Plugin(name = "Flatten OCEL to traditional event log",
returnLabels = { "Traditional Event Log" },
returnTypes = { XLog.class },
parameterLabels = { "Object-Centric Event Log" },
help = "Object-Centric Event Log",
userAccessible = true)
public class Flattening {
	@PluginVariant(requiredParameterLabels = {0})
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public XLog flattenToTraditionalEventLog(UIPluginContext context, OcelEventLog ocel) {
		FlatteningWizardStep wizStep = new FlatteningWizardStep(ocel);
		List<ProMWizardStep<FlatteningWizardParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		ListWizard<FlatteningWizardParameters> listWizard = new ListWizard<>(wizStepList);
		FlatteningWizardParameters parameters = ProMWizardDisplay.show(context, listWizard, new FlatteningWizardParameters());
		return flatten(ocel, parameters.objectType);
	}
	
	public static XLog flatten(OcelEventLog ocel, String objectType) {
		XAttributeMap logAttributes = new XAttributeMapImpl();
		XLog log = new XLogImpl(logAttributes);
		for (OcelObject ocelObject : ocel.objectTypes.get(objectType).objects) {
			XAttributeMap traceAttributes = new XAttributeMapImpl();
			XAttribute caseId = new XAttributeLiteralImpl("concept:name", ocelObject.id);
			traceAttributes.put("concept:name", caseId);
			XTrace trace = new XTraceImpl(traceAttributes);
			for (OcelEvent ocelEvent : ocelObject.sortedRelatedEvents) {
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
