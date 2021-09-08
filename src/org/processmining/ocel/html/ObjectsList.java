package org.processmining.ocel.html;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

@Plugin(name = "Visualize Objects from Object-Centric Model",
returnLabels = { "HTML Container" },
returnTypes = { HTMLContainer.class },
parameterLabels = { "Object-Centric Model" },
help = "Visualize Objects from Object-Centric Model",
userAccessible = true)
public class ObjectsList {
	@PluginVariant(requiredParameterLabels = { 0 })
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static HTMLContainer applyPlugin(UIPluginContext context, AnnotatedModel model) {
		ObjectsListWizardStep wizStep = new ObjectsListWizardStep(model);
		List<ProMWizardStep<ObjectsListWizardParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		ListWizard<ObjectsListWizardParameters> listWizard = new ListWizard<>(wizStepList);
		ObjectsListWizardParameters parameters = ProMWizardDisplay.show(context, listWizard, new ObjectsListWizardParameters());
		return ObjectsList.generateTable(model, parameters.objectType);
	}
	
	public static HTMLContainer generateTable(AnnotatedModel model, String selectedObjectType) {
		StringBuilder ret = new StringBuilder();
		ret.append("<table border='1'><thead><tr><th>Object ID</th><th>Start Timestamp</th><th>Complete Timestamp</th><th>Lifecycle Duration</th><th>Number of Rel.Ev.</th></tr></thead><tbody>");
		System.out.println(model.ocel.objectTypes);
		OcelObjectType type = model.ocel.objectTypes.get(selectedObjectType);
		for (OcelObject object : type.objects) {
			if (object.sortedRelatedEvents.size() > 0) {
				Date lastTimestamp = object.sortedRelatedEvents.get(object.sortedRelatedEvents.size()-1).timestamp;
				Date firstTimestamp = object.sortedRelatedEvents.get(0).timestamp;
				Long diff = (lastTimestamp.toInstant().toEpochMilli() - firstTimestamp.toInstant().toEpochMilli())/1000;
				ret.append("<tr><td>"+object.id+"</td><td>"+firstTimestamp+"</td><td>"+lastTimestamp+"</td><td>"+diff+"</td><td>"+object.sortedRelatedEvents.size()+"</td></tr>");
			}
		}
		ret.append("</tbody></table>");
		return new HTMLContainer(ret.toString());
	}
}
