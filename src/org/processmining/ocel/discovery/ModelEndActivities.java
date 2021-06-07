package org.processmining.ocel.discovery;

import java.util.HashMap;
import java.util.Map;

import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class ModelEndActivities {
	OcelEventLog ocel;
	OcelObjectType ot;
	public Map<String, Endpoint> endpoints;
	int maxEndpoint;

	public ModelEndActivities(OcelEventLog ocel, OcelObjectType ot) {
		this.ocel = ocel;
		this.ot = ot;
		this.endpoints = new HashMap<String, Endpoint>();
		this.maxEndpoint = 0;
		this.calculateEvents();
	}
	
	public void calculateEvents() {
		for (OcelObject obj : ot.objects) {
			if (obj.sortedRelatedEvents.size() > 0) {
				String act = obj.sortedRelatedEvents.get(obj.sortedRelatedEvents.size() - 1).activity;
				if (!endpoints.containsKey(act)) {
					endpoints.put(act, new Endpoint(act, ot));
				}
				endpoints.get(act).increaseNumEvents();
			}
		}
		for (Endpoint e : endpoints.values()) {
			this.maxEndpoint = Integer.max(this.maxEndpoint, e.numEvents);
		}
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for (Endpoint e : endpoints.values()) {
			ret.append(String.format("[ %s (%s) ] ", e.activity, e.toString()));
		}
		return ret.toString();
	}
	
	public Map<String, String> getAtLeastOnce(int idx, int count) {
		Map<String, String> ret = new HashMap<String, String>();
		for (Endpoint e : endpoints.values()) {
			if (e.numEvents == maxEndpoint || e.satisfy(idx, count)) {
				ret.put(e.activity, e.toReducedString(idx));
			}
		}
		return ret;
	}
}
