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

@Plugin(name = "Filter OCEL on specified object types",
returnLabels = { "Object-Centric Event Log" },
returnTypes = { OcelEventLog.class },
parameterLabels = { "Object-Centric Event Log" },
help = "Object-Centric Event Log",
userAccessible = true)
public class FilterOnObjectTypesList {
	@PluginVariant(requiredParameterLabels = {0})
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static OcelEventLog applyPlugin(UIPluginContext context, OcelEventLog ocel) {
		FilterOtListWizardStep wizStep = new FilterOtListWizardStep(ocel);
		List<ProMWizardStep<FilterOtListWizardParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		ListWizard<FilterOtListWizardParameters> listWizard = new ListWizard<>(wizStepList);
		FilterOtListWizardParameters parameters = ProMWizardDisplay.show(context, listWizard, new FilterOtListWizardParameters());
		return FilterOnObjectTypes.filterOnProvidedTextInput(ocel, parameters.getObjTypesList());
	}
}
