package org.processmining.ocel.importers;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

public class CSVInputStep1 extends ProMPropertiesPanel implements ProMWizardStep<CSVInputParameters> {
	private static final String TITLE = "Choose import parameters";
	
	ProMTextField newLineChars;
	ProMTextField separator;
	ProMTextField quotechars;
	
	public CSVInputStep1(CSVInputParameters model) {
		super(TITLE);
		
		//this.newLineChars = this.addTextField("New line characters", model.newLineChars);
		this.separator = this.addTextField("Separator", ""+model.separator);
		this.quotechars = this.addTextField("Quotechar", ""+model.quotechar);
	}

	public CSVInputParameters apply(CSVInputParameters model, JComponent component) {
		// TODO Auto-generated method stub
		//model.newLineChars = this.newLineChars.getText();
		model.separator = this.separator.getText().charAt(0);
		model.quotechar = this.quotechars.getText().charAt(0);
		model.compute();
		return model;
	}

	public boolean canApply(CSVInputParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return true;
	}

	public JComponent getComponent(CSVInputParameters model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
