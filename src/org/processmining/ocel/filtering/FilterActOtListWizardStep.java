package org.processmining.ocel.filtering;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class FilterActOtListWizardStep extends ProMPropertiesPanel implements ProMWizardStep<FilterActOtListWizardParameters> {
	OcelEventLog ocelLog;
	
	private static final String TITLE = "Choose the activities per object type by telling YES or NO";
	
	public final JTextArea filterSelector;
	
	public FilterActOtListWizardStep(OcelEventLog ocelLog) {
		super(TITLE);
		this.ocelLog = ocelLog;
			
		filterSelector = new JTextArea();
		filterSelector.setText(FilterActOtList.getActOtStatisticsStri(ocelLog.preFilter));
		this.add(filterSelector);
	}

	public FilterActOtListWizardParameters apply(FilterActOtListWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			FilterActOtListWizardStep step = (FilterActOtListWizardStep) component;
			model.setActivitiesList(filterSelector.getText());
		}
		return model;
	}

	public boolean canApply(FilterActOtListWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof FilterActOtListWizardStep;
	}

	public JComponent getComponent(FilterActOtListWizardParameters model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
