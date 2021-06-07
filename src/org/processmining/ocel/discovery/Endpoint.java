package org.processmining.ocel.discovery;

import org.processmining.ocel.ocelobjects.OcelObjectType;

public class Endpoint {
	String activity;
	public OcelObjectType ot;
	public int numEvents;
	
	public Endpoint(String activity, OcelObjectType ot) {
		this.activity = activity;
		this.ot = ot;
		this.numEvents = 0;
	}
	
	public void increaseNumEvents() {
		this.numEvents++;
	}
	
	
	public String toString() {
		return String.format("E=%d", this.numEvents);
	}
	
	public String toReducedString(int idx) {
		return String.format("E=%d", this.numEvents);
	}
	
	public boolean satisfy(int idx, int count) {
		if (this.numEvents >= count) {
			return true;
		}
		return false;
	}
}
