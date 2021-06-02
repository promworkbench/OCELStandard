package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.Map;

import org.processmining.ocel.annotations.ActivityOtDependent;
import org.processmining.ocel.annotations.ActivityOtIndipendent;
import org.processmining.ocel.annotations.EdgesMeasures;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class AnnotatedModel {
	public OcelEventLog ocel;
	public ModelActivities activities;
	public ModelActivityOtGroups activityOtGroups;
	public ModelEdges edges;
	public Map<String, ActivityOtIndipendent> indipendentNodeMeasures;
	public Map<String, Map<String, ActivityOtDependent>> dependentNodeMeasures;
	public Map<ModelEdge, EdgesMeasures> edgesMeasures;
	public Map<OcelObjectType, ModelStartActivities> startActivities;
	public Map<OcelObjectType, ModelEndActivities> endActivities;
	
	public AnnotatedModel() {
	}
	
	public AnnotatedModel(OcelEventLog ocel) {
		this.ocel = ocel;
		this.activities = new ModelActivities(ocel);
		this.activityOtGroups = new ModelActivityOtGroups(ocel);
		this.edges = new ModelEdges(ocel);
		this.calculateIndipendent();
		this.calculateDependent();
		this.calculateEdgesMeasures();
		this.calculateStartActivities();
		this.calculateEndActivities();
	}
	
	public void calculateIndipendent() {
		this.indipendentNodeMeasures = new HashMap<String, ActivityOtIndipendent>();
		for (String activity : this.activities.activities) {
			this.indipendentNodeMeasures.put(activity, new ActivityOtIndipendent(ocel, activity));
			//System.out.println(this.indipendentNodeMeasures.get(activity));
			//System.out.println(this.indipendentNodeMeasures.get(activity).toReducedString(0));
			//System.out.println(this.indipendentNodeMeasures.get(activity).toReducedString(1));
		}
	}
	
	public void calculateDependent() {
		this.dependentNodeMeasures = new HashMap<String, Map<String, ActivityOtDependent>>();
		for (String activity : this.activityOtGroups.activityOtGroups.keySet()) {
			this.dependentNodeMeasures.put(activity, new HashMap<String, ActivityOtDependent>());
			for (String ot : this.activityOtGroups.activityOtGroups.get(activity)) {
				this.dependentNodeMeasures.get(activity).put(ot, new ActivityOtDependent(ocel, activity, ot));
				//System.out.println(this.dependentNodeMeasures.get(activity).get(ot));
			}
		}
	}
	
	public void calculateEdgesMeasures() {
		this.edgesMeasures = new HashMap<ModelEdge, EdgesMeasures>();
		for (ModelEdge edge : this.edges.edges) {
			String source = edge.sourceActivity;
			String target = edge.targetActivity;
			OcelObjectType ot = edge.objectType;
			ActivityOtDependent sourceStatistics = this.dependentNodeMeasures.get(source).get(ot.name);
			ActivityOtDependent targetStatistics = this.dependentNodeMeasures.get(target).get(ot.name);
			this.edgesMeasures.put(edge, new EdgesMeasures(ocel, edge, sourceStatistics, targetStatistics));
			//System.out.println(this.edgesMeasures.get(edge));
			//System.out.println(this.edgesMeasures.get(edge).toReducedString(0));
			//System.out.println(this.edgesMeasures.get(edge).toReducedString(1));
		}
	}
	
	public void calculateStartActivities() {
		this.startActivities = new HashMap<OcelObjectType, ModelStartActivities>();
		for (OcelObjectType type : ocel.objectTypes.values()) {
			this.startActivities.put(type, new ModelStartActivities(ocel, type));
		}
		System.out.println(this.startActivities);
	}
	
	public void calculateEndActivities() {
		this.endActivities = new HashMap<OcelObjectType, ModelEndActivities>();
		for (OcelObjectType type : ocel.objectTypes.values()) {
			this.endActivities.put(type, new ModelEndActivities(ocel, type));
		}
		System.out.println(this.endActivities);
	}
}
