package org.processmining.ocel.filtering;

public class FilterOtListWizardParameters {
	public String objTypesList;
	
	public FilterOtListWizardParameters() {
		
	}
	
	public FilterOtListWizardParameters(String objTypesList) {
		this.objTypesList = objTypesList;
	}
	
	public String getObjTypesList() {
		return this.objTypesList;
	}
	
	public void setObjTypesList(String objTypesList) {
		this.objTypesList = objTypesList;
	}
}
