package org.processmining.ocel.filtering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;

@Plugin(name = "Filter OCEL on specified activities",
returnLabels = { "Object-Centric Event Log" },
returnTypes = { OcelEventLog.class },
parameterLabels = { "Object-Centric Event Log" },
help = "Object-Centric Event Log",
userAccessible = true)
public class FilterActivitiesList {
	@PluginVariant(requiredParameterLabels = {0})
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static OcelEventLog applyPlugin(UIPluginContext context, OcelEventLog ocel) {
		FilterActivitiesListWizardStep wizStep = new FilterActivitiesListWizardStep(ocel);
		List<ProMWizardStep<FilterActivitiesListWizardParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		ListWizard<FilterActivitiesListWizardParameters> listWizard = new ListWizard<>(wizStepList);
		FilterActivitiesListWizardParameters parameters = ProMWizardDisplay.show(context, listWizard, new FilterActivitiesListWizardParameters());
		
		return FilterActivitiesList.filterActivitiesList(ocel.preFilter, parameters.getActivitiesList());
	}
	
	public static String getActiString(OcelEventLog ocel) {
		StringBuilder ret = new StringBuilder();
		Map<String, Integer> actOcc = FilterActivitiesOccurrences.getActivitiesOccurrences(ocel);
		for (String act: actOcc.keySet()) {
			ret.append(act+">>>YES\n");
		}
		return ret.toString();
	}
	
	public static OcelEventLog filterActivitiesList(OcelEventLog ocel, String actiList) {
		Set<String> allowedActivities = new HashSet<String>();
		String[] lines = actiList.split("\n");
		for (String line : lines) {
			String[] spli = line.split(">>>");
			if (spli.length >= 2) {
				if (spli[1].equals("YES")) {
					allowedActivities.add(spli[0]);
				}
			}
		}
		OcelEventLog filtered = ocel.cloneEmpty();
		for (OcelEvent eve : ocel.events.values()) {
			if (allowedActivities.contains(eve.activity)) {
				filtered.cloneEvent(eve);
			}
		}
		filtered.register();
		return filtered;
	}
}
