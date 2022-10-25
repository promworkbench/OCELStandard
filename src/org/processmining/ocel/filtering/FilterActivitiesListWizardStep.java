package org.processmining.ocel.filtering;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class FilterActivitiesListWizardStep extends ProMPropertiesPanel implements ProMWizardStep<FilterActivitiesListWizardParameters> {
	OcelEventLog ocelLog;
	
	private static final String TITLE = "Choose activities by telling YES or NO";
	
	public final JTextArea filterSelector;
	
	public FilterActivitiesListWizardStep(OcelEventLog ocelLog) {
		super(TITLE);
		this.ocelLog = ocelLog;
			
		filterSelector = new JTextArea();
		filterSelector.setText(FilterActivitiesList.getActiString(ocelLog.preFilter));
		this.add(filterSelector);
	}

	public FilterActivitiesListWizardParameters apply(FilterActivitiesListWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			FilterActivitiesListWizardStep step = (FilterActivitiesListWizardStep) component;
			model.setActivitiesList(filterSelector.getText());
		}
		return model;
	}

	public boolean canApply(FilterActivitiesListWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof FilterActivitiesListWizardStep;
	}

	public JComponent getComponent(FilterActivitiesListWizardParameters model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
