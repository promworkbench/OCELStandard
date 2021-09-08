package org.processmining.ocel.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.ocelobjects.OcelEvent;
import org.processmining.ocel.ocelobjects.OcelObject;

@Plugin(name = "Visualize Events from Object-Centric Model",
returnLabels = { "HTML Container" },
returnTypes = { HTMLContainer.class },
parameterLabels = { "Object-Centric Model" },
help = "Visualize Events from Object-Centric Model",
userAccessible = true)
public class EventsList {
	@PluginVariant(requiredParameterLabels = { 0 })
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static HTMLContainer pluginTable(PluginContext context, AnnotatedModel model) {
		return EventsList.generateTable(model);
	}
	
	public static HTMLContainer generateTable(AnnotatedModel model) {
		StringBuilder ret = new StringBuilder();
		List<String> attributeNames = new ArrayList<String>((Set<String>)model.ocel.globalLog.get("ocel:attribute-names"));
		List<String> objectTypes = new ArrayList<String>((Set<String>)model.ocel.globalLog.get("ocel:object-types"));
		ret.append("<table border='1'><thead><tr><th>Event ID</th><th>Activity</th><th>Timestamp</th>");
		for (String ot : objectTypes) {
			ret.append("<th>"+ot+"</th>");
		}
		for (String an : attributeNames) {
			ret.append("<th>"+an+"</th>");
		}
		ret.append("</tr></thead><tbody>");
		for (OcelEvent eve : model.ocel.events.values()) {
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
		return new HTMLContainer(ret.toString());
	}
}
