package org.processmining.ocel.flattening;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class AdvancedFlatteningWizardStep2 extends ProMPropertiesPanel implements ProMWizardStep<AdvancedFlatteningParameters> {
	OcelEventLog ocelLog;
	
	private static final String TITLE = "Choose reference document type";
		
	String currTypeAtt;
	Set<String> currTypeAttValues;
	ProMComboBox<String> currTypeAttValuesSelection;


	public AdvancedFlatteningWizardStep2(OcelEventLog ocelLog) {
		super(TITLE);
		this.ocelLog = ocelLog;
		this.currTypeAtt = "";
		this.currTypeAttValues = new HashSet<String>();
	}

	public AdvancedFlatteningParameters apply(AdvancedFlatteningParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			AdvancedFlatteningWizardStep2 step = (AdvancedFlatteningWizardStep2) component;
			model.setCurrTypeAllowedValue((String)currTypeAttValuesSelection.getSelectedItem());
		}
		return model;
	}

	public boolean canApply(AdvancedFlatteningParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return component instanceof AdvancedFlatteningWizardStep2;
	}

	public JComponent getComponent(AdvancedFlatteningParameters model) {
		// TODO Auto-generated method stub
		this.currTypeAtt = model.getCurrTypeAtt();
		for (OcelEvent eve : this.ocelLog.getEvents().values()) {
			if (eve.attributes.containsKey(this.currTypeAtt)) {
				this.currTypeAttValues.add((String)eve.attributes.get(this.currTypeAtt));
			}
		}
		this.currTypeAttValuesSelection = this.addComboBox("Ref.Type", this.currTypeAttValues);
		
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
