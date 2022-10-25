package org.processmining.ocel.filtering;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class FilterOtOccurrencesWizardParameters extends PluginParametersImpl {
	public Integer minCount;
	
	public FilterOtOccurrencesWizardParameters() {
		
	}
	
	public FilterOtOccurrencesWizardParameters(Integer minCount) {
		this.minCount = minCount;
	}
	
	public Integer getMinCount() {
		return this.minCount;
	}
	
	public void setMinCount(Integer minCount) {
		this.minCount = minCount;
	}
}
