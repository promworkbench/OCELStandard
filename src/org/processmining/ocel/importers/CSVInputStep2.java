package org.processmining.ocel.importers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

public class CSVInputStep2 extends ProMPropertiesPanel implements ProMWizardStep<CSVInputParameters> {
	private static final String TITLE = "Choose column mappings";
	
	Map<Integer, ProMComboBox<String>> columnMappings;
	List<String> possibleValues;

	public CSVInputStep2(CSVInputParameters model) {
		super(TITLE);
		
		this.columnMappings = new HashMap<Integer, ProMComboBox<String>>();
				
		possibleValues = new ArrayList<String>();
		possibleValues.add("attribute");
		possibleValues.add("activity");
		possibleValues.add("timestamp");
		possibleValues.add("evid");
		possibleValues.add("objtype");
	}

	public CSVInputParameters apply(CSVInputParameters model, JComponent component) {
		// TODO Auto-generated method stub
		//model.newLineChars = this.newLineChars.getText();
		for (Integer i : this.columnMappings.keySet()) {
			model.columns.put(i, this.possibleValues.get(this.columnMappings.get(i).getSelectedIndex()));
		}
		return model;
	}

	public boolean canApply(CSVInputParameters model, JComponent component) {
		// TODO Auto-generated method stub
		return true;
	}

	public JComponent getComponent(CSVInputParameters model) {
		// TODO Auto-generated method stub
		
		List<String> firstRow = model.parsedCsv.get(0);

		int i = 0;
		while (i < firstRow.size()) {
			ProMComboBox<String> item = this.addComboBox(firstRow.get(i), possibleValues);
			item.setSelectedIndex(possibleValues.indexOf(model.columns.get(i)));
			this.columnMappings.put(i, item);
			i++;
		}
		
		return this;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return TITLE;
	}
}
