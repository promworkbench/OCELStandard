package org.processmining.ocel.filtering;

import java.util.ArrayList;
import java.util.List;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class FilterOtOccurrences {
	@Plugin(name = "Filter OCEL on object types' objects",
			returnLabels = { "Object-Centric Event Log" },
			returnTypes = { OcelEventLog.class },
			parameterLabels = { "Object-Centric Event Log" },
			help = "Object-Centric Event Log",
			userAccessible = true)
	@PluginVariant(requiredParameterLabels = {0})
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static OcelEventLog applyPlugin(UIPluginContext context, OcelEventLog ocel) {
		FilterOtOccurrencesWizardStep wizStep = new FilterOtOccurrencesWizardStep(ocel);
		List<ProMWizardStep<FilterOtOccurrencesWizardParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		ListWizard<FilterOtOccurrencesWizardParameters> listWizard = new ListWizard<>(wizStepList);
		FilterOtOccurrencesWizardParameters parameters = ProMWizardDisplay.show(context, listWizard, new FilterOtOccurrencesWizardParameters());
		System.out.println(parameters.getMinCount());
		return FilterOnObjectTypes.applyFrequency(ocel, parameters.getMinCount());
	}
}
