package org.processmining.ocel.discovery;

import org.processmining.ocel.ocelobjects.OcelObjectType;

public class Endpoint {
	String activity;
	public OcelObjectType ot;
	public int numObjects;
	
	public Endpoint(String activity, OcelObjectType ot) {
		this.activity = activity;
		this.ot = ot;
		this.numObjects = 0;
	}
	
	public void increaseNumObjects() {
		this.numObjects++;
	}
	
	public int getValue() {
		return this.numObjects;
	}
	
	
	public String toString() {
		return String.format("UO=%d", this.numObjects);
	}
	
	public String toReducedString(int idx) {
		return String.format("UO=%d", this.numObjects);
	}
	
	public boolean satisfy(int idx, int count) {
		if (this.numObjects >= count) {
			return true;
		}
		return false;
	}
}
