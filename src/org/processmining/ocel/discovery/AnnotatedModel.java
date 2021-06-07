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
	public int MIN_INDIPENDENT_ACT_COUNT;
	public int MAX_INDIPENDENT_ACT_COUNT;
	public int MIN_EDGE_COUNT;
	public int MAX_EDGE_COUNT;
	
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
		this.calculateExtremes(0);
	}
	
	public void calculateIndipendent() {
		this.indipendentNodeMeasures = new HashMap<String, ActivityOtIndipendent>();
		for (String activity : this.activities.activities) {
			this.indipendentNodeMeasures.put(activity, new ActivityOtIndipendent(ocel, activity));
		}
	}
	
	public void calculateDependent() {
		this.dependentNodeMeasures = new HashMap<String, Map<String, ActivityOtDependent>>();
		for (String activity : this.activityOtGroups.activityOtGroups.keySet()) {
			this.dependentNodeMeasures.put(activity, new HashMap<String, ActivityOtDependent>());
			for (String ot : this.activityOtGroups.activityOtGroups.get(activity)) {
				this.dependentNodeMeasures.get(activity).put(ot, new ActivityOtDependent(ocel, activity, ot));
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
		}
	}
	
	public void calculateStartActivities() {
		this.startActivities = new HashMap<OcelObjectType, ModelStartActivities>();
		for (OcelObjectType type : ocel.objectTypes.values()) {
			this.startActivities.put(type, new ModelStartActivities(ocel, type));
		}
	}
	
	public void calculateEndActivities() {
		this.endActivities = new HashMap<OcelObjectType, ModelEndActivities>();
		for (OcelObjectType type : ocel.objectTypes.values()) {
			this.endActivities.put(type, new ModelEndActivities(ocel, type));
		}
	}
	
	public void calculateExtremes(int idx) {
		this.MIN_INDIPENDENT_ACT_COUNT = Integer.MAX_VALUE;
		this.MAX_INDIPENDENT_ACT_COUNT = 0;
		this.MIN_EDGE_COUNT = Integer.MAX_VALUE;
		this.MAX_EDGE_COUNT = 0;
		for (ActivityOtIndipendent meas : indipendentNodeMeasures.values()) {
			int thisCount = meas.getValue(idx);
			this.MIN_INDIPENDENT_ACT_COUNT = Math.min(this.MIN_INDIPENDENT_ACT_COUNT, thisCount);
			this.MAX_INDIPENDENT_ACT_COUNT = Math.max(this.MAX_INDIPENDENT_ACT_COUNT, thisCount);
		}
		for (EdgesMeasures edge : edgesMeasures.values()) {
			int thisCount = edge.getValue(idx);
			this.MIN_EDGE_COUNT = Math.min(this.MIN_EDGE_COUNT, thisCount);
			this.MAX_EDGE_COUNT = Math.max(this.MAX_EDGE_COUNT, thisCount);
		}
	}
}
