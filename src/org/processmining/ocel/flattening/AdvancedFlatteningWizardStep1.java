package org.processmining.ocel.flattening;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class AdvancedFlatteningWizardStep1 extends ProMPropertiesPanel implements ProMWizardStep<AdvancedFlatteningParameters> {
	OcelEventLog ocelLog;
	
	private static final String TITLE = "Choose attributes";
	
	private final ProMComboBox<String> currTypeAtt;
	private final ProMComboBox<String> prevDoc;
	private final ProMComboBox<String> currDoc;
	private final ProMComboBox<String> otherKey;


	public AdvancedFlatteningWizardStep1(OcelEventLog ocelLog) {
		super(TITLE);
		this.ocelLog = ocelLog;
		
		List<String> attributes = new ArrayList<String>(this.ocelLog.getAttributeNames());
		currTypeAtt = addComboBox("Document type", attributes);
		prevDoc = addComboBox("Prev.doc.", attributes);
		currDoc = addComboBox("Curr.doc.", attributes);
		otherKey = addComboBox("Other Curr.", attributes);
	}

	public AdvancedFlatteningParameters apply(AdvancedFlatteningParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			AdvancedFlatteningWizardStep1 step = (AdvancedFlatteningWizardStep1) component;
			model.setCurrTypeAtt((String)currTypeAtt.getSelectedItem());
			model.setCurrDoc((String)currDoc.getSelectedItem());
			model.setPrevDoc((String)prevDoc.getSelectedItem());
			model.setOtherKey((String)otherKey.getSelectedItem());
		}
		return model;
	}

	public boolean canApply(AdvancedFlatteningParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof AdvancedFlatteningWizardStep1;
	}

	public JComponent getComponent(AdvancedFlatteningParameters model) {
		// TODO Auto-generated method stub
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
