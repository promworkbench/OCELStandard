package org.processmining.ocel.filtering;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class FilterOtOccurrencesWizardStep extends ProMPropertiesPanel implements ProMWizardStep<FilterOtOccurrencesWizardParameters>  {
	OcelEventLog ocelLog;
	
	private static final String TITLE = "Choose minimum number of occurrences";
	
	public final JTextField minCountSelector;
	
	public FilterOtOccurrencesWizardStep(OcelEventLog ocelLog) {
		super(TITLE);
		this.ocelLog = ocelLog;
			
		minCountSelector = new JTextField();
		minCountSelector.setText("2");
		this.add(minCountSelector);
	}

	public FilterOtOccurrencesWizardParameters apply(FilterOtOccurrencesWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			FilterOtOccurrencesWizardStep step = (FilterOtOccurrencesWizardStep) component;
			model.setMinCount(Integer.parseInt(step.minCountSelector.getText()));
		}
		return model;
	}

	public boolean canApply(FilterOtOccurrencesWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof FilterOtOccurrencesWizardStep;
	}

	public JComponent getComponent(FilterOtOccurrencesWizardParameters model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
