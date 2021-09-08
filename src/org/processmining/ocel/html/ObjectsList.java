package org.processmining.ocel.html;

import java.util.Date;

import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

public class ObjectsList {
	public static HTMLContainer generateTable(AnnotatedModel model, String selectedObjectType) {
		StringBuilder ret = new StringBuilder();
		ret.append("<table><thead><tr><th>Object ID</th><th>Start Timestamp</th><th>Complete Timestamp</th><th>Lifecycle Duration</th><th>Number of Rel.Ev.</th></tr></thead><tbody>");
		System.out.println(model.ocel.objectTypes);
		OcelObjectType type = model.ocel.objectTypes.get(selectedObjectType);
		for (OcelObject object : type.objects) {
			if (object.sortedRelatedEvents.size() > 0) {
				Date lastTimestamp = object.sortedRelatedEvents.get(object.sortedRelatedEvents.size()-1).timestamp;
				Date firstTimestamp = object.sortedRelatedEvents.get(0).timestamp;
				Long diff = (lastTimestamp.toInstant().toEpochMilli() - firstTimestamp.toInstant().toEpochMilli())/1000;
				ret.append("<tr><td>"+object.id+"</td><td>"+firstTimestamp+"</td><td>"+lastTimestamp+"</td><td>"+diff+"</td><td>"+object.sortedRelatedEvents.size()+"</td></tr>");
			}
		}
		ret.append("</tbody></table>");
		return new HTMLContainer(ret.toString());
	}
}
