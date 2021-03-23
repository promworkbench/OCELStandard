package org.processmining.ocel.flattening;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class AdvancedFlatteningParameters  extends PluginParametersImpl {
	public String currTypeAtt;
	public String currTypeAllowedValue;
	public String prevDoc;
	public String currDoc;
	public String otherKey;
	
	public AdvancedFlatteningParameters() {
	}
	
	public String getCurrTypeAtt() {
		return this.currTypeAtt;
	}
	
	public void setCurrTypeAtt(String currTypeAtt) {
		this.currTypeAtt = currTypeAtt;
	}
	
	public String getCurrTypeAllowedValue() {
		return this.currTypeAllowedValue;
	}
	
	public void setCurrTypeAllowedValue(String value) {
		this.currTypeAllowedValue = value;
	}
	
	public String getPrevDoc() {
		return this.prevDoc;
	}
	
	public void setPrevDoc(String prevDoc) {
		this.prevDoc = prevDoc;
	}
	
	public String getCurrDoc() {
		return this.currDoc;
	}
	
	public void setCurrDoc(String currDoc) {
		this.currDoc = currDoc;
	}
	
	public String getOtherKey() {
		return this.otherKey;
	}
	
	public void setOtherKey(String otherKey) {
		this.otherKey = otherKey;
	}
}
