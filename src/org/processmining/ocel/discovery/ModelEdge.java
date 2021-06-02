package org.processmining.ocel.discovery;

import java.util.HashSet;
import java.util.Set;

import org.processmining.ocel.ocelobjects.OcelObjectType;
import org.processmining.ocel.utils.Separator;

public class ModelEdge {
	public String sourceActivity;
	public String targetActivity;
	public OcelObjectType objectType;
	public Set<String> realizations;
	
	public ModelEdge(String sourceActivity, String targetActivity, OcelObjectType objectType) {
		this.sourceActivity = sourceActivity;
		this.targetActivity = targetActivity;
		this.objectType = objectType;
		this.realizations = new HashSet<String>();
	}
	
	public void addRealization(String e1, String e2) {
		this.realizations.add(e1 + Separator.SEPARATOR + e2);
	}
	
	public String toString() {
		return this.sourceActivity + "->" + this.targetActivity+" ("+objectType.name+"; EC="+realizations.size()+")";
	}
}
