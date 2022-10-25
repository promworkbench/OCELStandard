package org.processmining.ocel.filtering;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class FilterActivitiesOccWizardParameters extends PluginParametersImpl {
	public Integer minCount;
	
	public FilterActivitiesOccWizardParameters() {
		
	}
	
	public FilterActivitiesOccWizardParameters(Integer minCount) {
		this.minCount = minCount;
	}
	
	public Integer getMinCount() {
		return this.minCount;
	}
	
	public void setMinCount(Integer minCount) {
		this.minCount = minCount;
	}
}