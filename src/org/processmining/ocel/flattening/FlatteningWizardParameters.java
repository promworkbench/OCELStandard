package org.processmining.ocel.flattening;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class FlatteningWizardParameters extends PluginParametersImpl {
	public String objectType;
	
	public FlatteningWizardParameters() {
		
	}
	
	public FlatteningWizardParameters(String objectType) {
		this.objectType = objectType;
	}
	
	public String getObjectType() {
		return this.objectType;
	}
	
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
}
