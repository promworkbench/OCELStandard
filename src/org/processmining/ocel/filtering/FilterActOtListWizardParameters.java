package org.processmining.ocel.filtering;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class FilterActOtListWizardParameters extends PluginParametersImpl {
	public String activitiesList;
	
	public FilterActOtListWizardParameters() {
		
	}
	
	public FilterActOtListWizardParameters(String activitiesList) {
		this.activitiesList = activitiesList;
	}
	
	public String getActivitiesList() {
		return this.activitiesList;
	}
	
	public void setActivitiesList(String activitiesList) {
		this.activitiesList = activitiesList;
	}
}
