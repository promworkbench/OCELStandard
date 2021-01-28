package org.processmining.ocel.flattening;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class FlatteningWizardStep extends ProMPropertiesPanel implements ProMWizardStep<FlatteningWizardParameters> {
	OcelEventLog ocelLog;
	
	private static final String TITLE = "Choose object type";
	
	private final ProMComboBox<String> objectTypeList;
	
	public FlatteningWizardStep(OcelEventLog ocelLog) {
		super(TITLE);
		this.ocelLog = ocelLog;
		
		List<String> objectTypes = new ArrayList<String>(this.ocelLog.objectTypes.keySet());

		objectTypeList = addComboBox("Object Type", objectTypes);
	}

	public FlatteningWizardParameters apply(FlatteningWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			FlatteningWizardStep step = (FlatteningWizardStep) component;
			model.setObjectType((String)objectTypeList.getSelectedItem());
		}
		return model;
	}

	public boolean canApply(FlatteningWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof FlatteningWizardStep;
	}

	public JComponent getComponent(FlatteningWizardParameters model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
