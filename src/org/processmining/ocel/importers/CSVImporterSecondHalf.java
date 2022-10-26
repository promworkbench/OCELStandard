package org.processmining.ocel.importers;

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

@Plugin(name = "Conclude import of OCEL from CSV file",
returnLabels = { "Object-Centric Event Log" },
returnTypes = { OcelEventLog.class },
parameterLabels = { "Object-Centric Event Log" },
help = "Object-Centric Event Log",
userAccessible = true)
public class CSVImporterSecondHalf {
	@PluginVariant(requiredParameterLabels = {0})
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static OcelEventLog applyPlugin(UIPluginContext context, CSVInputParameters csvInputParameters) {
		CSVInputStep1 csvInputStep1 = new CSVInputStep1(csvInputParameters);
		CSVInputStep2 csvInputStep2 = new CSVInputStep2(csvInputParameters);
		List<ProMWizardStep<CSVInputParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(csvInputStep1);
		wizStepList.add(csvInputStep2);
		ListWizard<CSVInputParameters> listWizard = new ListWizard<>(wizStepList);
		csvInputParameters = ProMWizardDisplay.show(context, listWizard, csvInputParameters);
		return csvInputParameters.obtainEventLog();
	}
}
