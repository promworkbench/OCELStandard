package org.processmining.ocel.filtering;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class FilterOtListWizardStep extends ProMPropertiesPanel implements ProMWizardStep<FilterOtListWizardParameters> {
	OcelEventLog ocelLog;
	
	private static final String TITLE = "Choose object types by telling YES or NO";
	
	public final JTextArea filterSelector;
	
	public FilterOtListWizardStep(OcelEventLog ocelLog) {
		super(TITLE);
		this.ocelLog = ocelLog;
			
		filterSelector = new JTextArea();
		filterSelector.setText(FilterOnObjectTypes.objTypesTextForInput(ocelLog.preFilter));
		this.add(filterSelector);
	}

	public FilterOtListWizardParameters apply(FilterOtListWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			FilterOtListWizardStep step = (FilterOtListWizardStep) component;
			model.setObjTypesList(filterSelector.getText());
		}
		return model;
	}

	public boolean canApply(FilterOtListWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof FilterOtListWizardStep;
	}

	public JComponent getComponent(FilterOtListWizardParameters model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
