package org.processmining.ocel.html;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class ObjectsListWizardParameters extends PluginParametersImpl {
	public String objectType;
	
	public ObjectsListWizardParameters() {
		
	}
	
	public ObjectsListWizardParameters(String objectType) {
		this.objectType = objectType;
	}
	
	public String getObjectType() {
		return this.objectType;
	}
	
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
}
