package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.ocel.annotations.ActivityOtDependent;
import org.processmining.ocel.annotations.ActivityOtIndipendent;
import org.processmining.ocel.annotations.EdgesMeasures;
import org.processmining.ocel.filtering.FilterNotRelatedObjects;
import org.processmining.ocel.filtering.FilterNumberRelatedObjectsType;
import org.processmining.ocel.filtering.FilterOnObjectTypes;
import org.processmining.ocel.filtering.FilterOnRelatedObjects;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;
import org.processmining.ocel.utils.Separator;

public class AnnotatedModel {
	public AnnotatedModel original;
	public AnnotatedModel parent;
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
		this.original = this;
		this.parent = this;
	}
	
	public void calculateIndipendent() {
		this.indipendentNodeMeasures = new HashMap<String, ActivityOtIndipendent>();
		for (String activity : this.activities.activities) {
			this.indipendentNodeMeasures.put(activity, new ActivityOtIndipendent(ocel, this, activity));
		}
	}
	
	public void calculateDependent() {
		this.dependentNodeMeasures = new HashMap<String, Map<String, ActivityOtDependent>>();
		for (String activity : this.activityOtGroups.activityOtGroups.keySet()) {
			this.dependentNodeMeasures.put(activity, new HashMap<String, ActivityOtDependent>());
			for (String ot : this.activityOtGroups.activityOtGroups.get(activity)) {
				this.dependentNodeMeasures.get(activity).put(ot, new ActivityOtDependent(ocel, this, activity, ot));
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
	
	public Set<OcelObject> relatedObjectsEdge(ModelEdge edge) {
		Set<OcelObject> relatedObjects = new HashSet<OcelObject>();
		if (edge != null) {
			for (String rea : edge.realizations) {
				String[] events = rea.split(Separator.SEPARATOR);
				if (this.ocel.events.containsKey(events[0]) && this.ocel.events.containsKey(events[1])) {
					OcelEvent eve1 = this.ocel.events.get(events[0]);
					OcelEvent eve2 = this.ocel.events.get(events[1]);
					for (OcelObject obj : eve1.relatedObjects) {
						if (eve2.relatedObjects.contains(obj)) {
							relatedObjects.add(obj);
						}
					}
				}
			}
		}
		return relatedObjects;
	}
	
	public Set<OcelObject> notRelatedObjectsEdge(ModelEdge edge) {
		OcelObjectType otype = edge.objectType;
		Set<OcelObject> allObjects = new HashSet<OcelObject>(otype.objects);
		Set<OcelObject> relatedObjects = this.relatedObjectsEdge(edge);
		allObjects.removeAll(relatedObjects);
		return allObjects;
	}
	
	public Set<OcelObject> relatedObjectsActivity(String activity) {
		Set<OcelObject> relatedObjects = new HashSet<OcelObject>();
		for (OcelEvent eve : this.ocel.events.values()) {
			if (eve.activity.equals(activity)) {
				for (OcelObject obj : eve.relatedObjects) {
					relatedObjects.add(obj);
				}
			}
		}
		return relatedObjects;
	}
	
	public Set<OcelObject> relatedObjectsActivityOt(String activity, OcelObjectType ot, boolean positive) {
		Set<OcelObject> allObjects = new HashSet<OcelObject>();
		Set<OcelObject> relatedObjects = new HashSet<OcelObject>();
		for (OcelEvent eve : this.ocel.events.values()) {
			for (OcelObject obj : eve.relatedObjects) {
				if (obj.objectType.equals(ot)) {
					allObjects.add(obj);
					if (eve.activity.equals(activity)) {
						relatedObjects.add(obj);
					}
				}
			}
		}
		if (positive) {
			return relatedObjects;
		}
		allObjects.removeAll(relatedObjects);
		return allObjects;
	}
	
	public Set<OcelObject> objectsHavingStartActivity(String activity, OcelObjectType ot, boolean positive) {
		Set<OcelObject> satisfyingObjects = new HashSet<OcelObject>();
		for (OcelObject obj : this.ocel.objects.values()) {
			List<OcelEvent> evs = obj.sortedRelatedEvents;
			if (evs.size() > 0 && ((positive && evs.get(0).activity.equals(activity) || (!positive && !evs.get(0).activity.equals(activity))))) {
				satisfyingObjects.add(obj);
			}
		}
		return satisfyingObjects;
	}
	
	public Set<OcelObject> objectsHavingEndActivity(String activity, OcelObjectType ot, boolean positive) {
		Set<OcelObject> satisfyingObjects = new HashSet<OcelObject>();
		for (OcelObject obj : this.ocel.objects.values()) {
			List<OcelEvent> evs = obj.sortedRelatedEvents;
			if (evs.size() > 0 && ((positive && evs.get(evs.size()-1).activity.equals(activity) || (!positive && !evs.get(evs.size()-1).activity.equals(activity))))) {
				satisfyingObjects.add(obj);
			}
		}
		return satisfyingObjects;
	}
	
	public AnnotatedModel filterOnRelatedObjects(Set<OcelObject> objects) {
		OcelEventLog filtered = FilterOnRelatedObjects.apply(this.ocel, objects);
		AnnotatedModel ret = new AnnotatedModel(filtered);
		ret.original = this.original;
		ret.parent = this;
		return ret;
	}
	
	public AnnotatedModel filterOnNotRelatedObjects(Set<OcelObject> positive, Set<OcelObject> negative) {
		OcelEventLog filtered = FilterNotRelatedObjects.apply(this.ocel, positive, negative);
		AnnotatedModel ret = new AnnotatedModel(filtered);
		ret.original = this.original;
		ret.parent = this;
		return ret;
	}
	
	public AnnotatedModel filterOnObjectTypes(Set<String> allowedObjectTypes) {
		OcelEventLog filtered = FilterOnObjectTypes.apply(this.ocel, allowedObjectTypes);
		AnnotatedModel ret = new AnnotatedModel(filtered);
		ret.original = this.original;
		ret.parent = this;
		return ret;
	}
	
	public AnnotatedModel filterOnNumberRelatedObjects(OcelObjectType type, int minOcc, int maxOcc) {
		OcelEventLog filtered = FilterNumberRelatedObjectsType.apply(this.ocel, type, minOcc, maxOcc);
		AnnotatedModel ret = new AnnotatedModel(filtered);
		ret.original = this.original;
		ret.parent = this;
		return ret;
	}
	
	public String getStringRelatedObjectsTypeActivity(String activity, OcelObjectType objectType) {
		StringBuilder ret = new StringBuilder();
		Set<OcelObject> consObjects = new HashSet<OcelObject>();
		for (OcelEvent eve : this.ocel.events.values()) {
			if (eve.activity.equals(activity)) {
				for (OcelObject obj : eve.relatedObjects) {
					if (obj.objectType.equals(objectType)) {
						consObjects.add(obj);
					}
				}
			}
		}
		for (OcelObject obj : consObjects) {
			ret.append(" "+obj.id);
		}
		return ret.toString();
	}
}
