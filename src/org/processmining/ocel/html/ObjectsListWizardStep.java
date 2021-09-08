package org.processmining.ocel.html;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class ObjectsListWizardStep extends ProMPropertiesPanel implements ProMWizardStep<ObjectsListWizardParameters> {
	OcelEventLog ocelLog;
	
	private static final String TITLE = "Choose object type";
	
	private final ProMComboBox<String> objectTypeList;
	
	public ObjectsListWizardStep(AnnotatedModel model) {
		super(TITLE);
		this.ocelLog = model.ocel;
		
		List<String> objectTypes = new ArrayList<String>(this.ocelLog.objectTypes.keySet());

		objectTypeList = addComboBox("Object Type", objectTypes);
	}

	public ObjectsListWizardParameters apply(ObjectsListWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			ObjectsListWizardStep step = (ObjectsListWizardStep) component;
			model.setObjectType((String)objectTypeList.getSelectedItem());
		}
		return model;
	}

	public boolean canApply(ObjectsListWizardParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof ObjectsListWizardStep;
	}

	public JComponent getComponent(ObjectsListWizardParameters model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}