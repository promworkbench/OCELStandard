package org.processmining.ocel.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;

@Plugin(name = "Filter OCEL on activities occurrences",
returnLabels = { "Object-Centric Event Log" },
returnTypes = { OcelEventLog.class },
parameterLabels = { "Object-Centric Event Log" },
help = "Object-Centric Event Log",
userAccessible = true)
public class FilterActivitiesOccurrences {
	@PluginVariant(requiredParameterLabels = {0})
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static OcelEventLog applyPlugin(UIPluginContext context, OcelEventLog ocel) {
		FilterActivitiesOccWizardStep wizStep = new FilterActivitiesOccWizardStep(ocel);
		List<ProMWizardStep<FilterActivitiesOccWizardParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		ListWizard<FilterActivitiesOccWizardParameters> listWizard = new ListWizard<>(wizStepList);
		FilterActivitiesOccWizardParameters parameters = ProMWizardDisplay.show(context, listWizard, new FilterActivitiesOccWizardParameters());
		System.out.println(parameters.getMinCount());
		return apply(ocel, parameters.getMinCount());
	}
	
	public static OcelEventLog apply(OcelEventLog original, Integer minOcc) {
		OcelEventLog filtered = original.cloneEmpty();
		Map<String, Integer> actiOcc = FilterActivitiesOccurrences.getActivitiesOccurrences(original);
		for (OcelEvent event : original.events.values()) {
			if (actiOcc.get(event.activity) >= minOcc) {
				filtered.cloneEvent(event);
			}
		}
		filtered.register();
		return filtered;
	}
	
	public static Map<String, Integer> getActivitiesOccurrences(OcelEventLog ocel) {
		Map<String, Integer> activitiesOccurrences = new HashMap<String, Integer>();
		for (String evId : ocel.events.keySet()) {
			OcelEvent eve = ocel.events.get(evId);
			String acti = eve.activity;
			if (!(activitiesOccurrences.containsKey(acti))) {
				activitiesOccurrences.put(acti, 1);
			}
			else {
				activitiesOccurrences.put(acti, activitiesOccurrences.get(acti) + 1);
			}
		}
		return activitiesOccurrences;
	}

}
