package org.processmining.ocel.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelObject;

public class EventsList {
	AnnotatedModel model;
	
	public EventsList(AnnotatedModel model) {
		this.model = model;
	}
	
	public String generateTable() {
		StringBuilder ret = new StringBuilder();
		List<String> attributeNames = new ArrayList<String>((Set<String>)this.model.ocel.globalLog.get("ocel:attribute-names"));
		List<String> objectTypes = new ArrayList<String>((Set<String>)this.model.ocel.globalLog.get("ocel:object-types"));
		ret.append("<table><thead><tr><th>Event ID</th><th>Activity</th><th>Timestamp</th>");
		for (String ot : objectTypes) {
			ret.append("<th>"+ot+"</th>");
		}
		for (String an : attributeNames) {
			ret.append("<th>"+an+"</th>");
		}
		ret.append("</tr></thead><tbody>");
		for (OcelEvent eve : this.model.ocel.events.values()) {
			ret.append("<tr><td>"+eve.id+"</td><td>"+eve.activity+"</td><td>"+eve.timestamp.toString()+"</td>");
			Map<String, Set<String>> objects = new HashMap<String, Set<String>>();
			for (OcelObject obj : eve.relatedObjects) {
				String otype = obj.objectType.name;
				if (!(objects.containsKey(otype))) {
					objects.put(otype, new HashSet<String>());
				}
				objects.get(otype).add(obj.id);
			}
			for (String ot : objectTypes) {
				if (objects.containsKey(ot)) {
					StringBuilder objStr = new StringBuilder();
					for (String obj : objects.get(ot)) {
						objStr.append(obj+"<br />");
					}
					ret.append("<td>"+objStr.toString()+"</td>");
				}
				else {
					ret.append("<td></td>");
				}
			}
			for (String att : attributeNames) {
				if (eve.attributes.containsKey(att)) {
					ret.append("<td>"+eve.attributes.get(att)+"</td>");
				}
				else {
					ret.append("<td></td>");
				}
			}
			ret.append("</tr>");
		}
		ret.append("</tbody>");
		return ret.toString();
	}
}
