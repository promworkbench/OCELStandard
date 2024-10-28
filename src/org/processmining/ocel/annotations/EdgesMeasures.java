package org.processmining.ocel.annotations;

import java.util.HashSet;
import java.util.Set;

import org.processmining.ocel.discovery.ModelEdge;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.utils.Separator;

public class EdgesMeasures {
	OcelEventLog ocel;
	ModelEdge edge;
	ActivityOtDependent sourceStatistics;
	ActivityOtDependent targetStatistics;
	public int eventCouples;
	public int uniqueObjects;
	public int totalObjects;
	public double percSource;
	public double percTarget;
	
	public EdgesMeasures(OcelEventLog ocel, ModelEdge edge, ActivityOtDependent sourceStatistics, ActivityOtDependent targetStatistics) {
		this.ocel = ocel;
		this.edge = edge;
		this.sourceStatistics = sourceStatistics;
		this.targetStatistics = targetStatistics;
		this.calculateEc();
		this.calculateUniqueObjects();
		this.calculateTotalObjects();
		this.calculatePercSource();
		this.calculatePercTarget();
	}
	
	public void calculateEc() {
		this.eventCouples = this.edge.realizations.size();
	}
	
	public void calculateUniqueObjects() {
		Set<OcelObject> edgeObjects = new HashSet<OcelObject>();
		for (String rea : this.edge.realizations) {
			String[] evs = rea.split(Separator.SEPARATOR);
			OcelEvent eve1 = ocel.events.get(evs[0]);
			OcelEvent eve2 = ocel.events.get(evs[1]);
			Set<OcelObject> r1 = new HashSet<OcelObject>(eve1.relatedObjects.keySet());
			Set<OcelObject> r11 = new HashSet<OcelObject>(eve1.relatedObjects.keySet());
			Set<OcelObject> r2 = new HashSet<OcelObject>(eve2.relatedObjects.keySet());
			r11.removeAll(r2);
			r1.removeAll(r11);
			edgeObjects.addAll(r1);
		}
		this.uniqueObjects = edgeObjects.size();
	}
	
	public void calculateTotalObjects() {
		this.totalObjects = 0;
		for (String rea : this.edge.realizations) {
			String[] evs = rea.split(Separator.SEPARATOR);
			OcelEvent eve1 = ocel.events.get(evs[0]);
			OcelEvent eve2 = ocel.events.get(evs[1]);
			Set<OcelObject> r1 = new HashSet<OcelObject>(eve1.relatedObjects.keySet());
			Set<OcelObject> r11 = new HashSet<OcelObject>(eve1.relatedObjects.keySet());
			Set<OcelObject> r2 = new HashSet<OcelObject>(eve2.relatedObjects.keySet());
			r11.removeAll(r2);
			r1.removeAll(r11);
			this.totalObjects += r1.size();
		}
	}
	
	public void calculatePercSource() {
		this.percSource = (100.0 * this.uniqueObjects) / this.sourceStatistics.numUniqueObjects;
	}
	
	public void calculatePercTarget() {
		this.percTarget = (100.0 * this.uniqueObjects) / this.targetStatistics.numUniqueObjects;
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(String.format("%s ->\n%s\n", this.edge.sourceActivity, this.edge.targetActivity));
		ret.append(String.format("(%s)\n", this.edge.objectType.name));
		ret.append(String.format("event couples = %d\n", this.eventCouples));
		ret.append(String.format("unique objects = %d\n", this.uniqueObjects));
		ret.append(String.format("total objects = %d\n", this.totalObjects));
		ret.append(String.format("perc source = %.2f\n", this.percSource));
		ret.append(String.format("perc target = %.2f\n", this.percTarget));
		return ret.toString();
	}
	
	public String toIntermediateString() {
		StringBuilder ret = new StringBuilder();
		ret.append(String.format("EC=%d", this.eventCouples));
		ret.append("\n");
		ret.append(String.format("UO=%d", this.uniqueObjects));
		ret.append("\n");
		ret.append(String.format("TO=%d", this.totalObjects));
		return ret.toString();
	}
	
	public String toReducedString(int idx) {
		if (idx == 0) {
			return String.format("EC=%d", this.eventCouples);
		}
		else if (idx == 1) {
			return String.format("UO=%d", this.uniqueObjects);
		}
		else if (idx == 2) {
			return String.format("TO=%d", this.totalObjects);
		}
		return "";
	}
	
	public boolean satisfy(int idx, int count) {
		return this.getValue(idx) >= count;
	}
	
	public int getValue(int idx) {
		if (idx == 0) {
			return this.eventCouples;
		}
		else if (idx == 1) {
			return this.uniqueObjects;
		}
		else if (idx == 2) {
			return this.totalObjects;
		}
		return 0;
	}
}
