package org.processmining.ocel.html;

import java.util.ArrayList;
import java.util.List;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;

@Plugin(name = "Visualize Object Lifecycle Length from Object-Centric Model",
returnLabels = { "HTML Container" },
returnTypes = { HTMLContainer.class },
parameterLabels = { "Object-Centric Model" },
help = "Visualize Object Lifecycle Length from Object-Centric Model",
userAccessible = true)
public class LifecycleLength {
	@PluginVariant(requiredParameterLabels = { 0 })
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public static HTMLContainer applyPlugin(PluginContext context, AnnotatedModel model) {
		return LifecycleLength.generateTable(model);
	}
	
	public static HTMLContainer generateTable(AnnotatedModel model) {
		StringBuilder ret = new StringBuilder();
		ret.append("<table border='1'><thead><tr><th>Object Type</th><th>Avg Length</th><th>Std Length</th><th>Num Conf. Objects (z=1)</th><th>Num Dev. Objects (z=1)</th></tr></thead><tbody>");
		for (OcelObjectType type : model.ocel.objectTypes.values()) {
			List<Double> lengths = new ArrayList<Double>();
			List<OcelObject> objects = new ArrayList<OcelObject>(type.objects);
			for (OcelObject obj : objects) {
				lengths.add(LifecycleLength.lifecycleLength(obj));
			}
			Double average = LifecycleLength.calculateAvg(lengths);
			Double std = LifecycleLength.calculateStd(lengths, average);
			Integer deviatingObjects = 0;
			Integer conformingObjects = 0;
			Double lb = average - std;
			Double ub = average + std;
			int i = 0;
			while (i < lengths.size()) {
				if (lb <= lengths.get(i) && lengths.get(i) <= ub) {
					conformingObjects += 1;
				}
				else {
					deviatingObjects += 1;
				}
				i++;
			}
			ret.append("<tr><td>"+type.name+"</td><td>"+average+"</td><td>"+std+"</td><td>"+conformingObjects+"</td><td>"+deviatingObjects+"</td></tr>");
		}
		ret.append("</tbody></table>");
		return new HTMLContainer(ret.toString());
	}
	
	public static Double lifecycleLength(OcelObject obj) {
		return new Double(obj.sortedRelatedEvents.size());
	}
	
	public static Double calculateAvg(List<Double> durations) {
		Double sum = 0.0;
		Integer count = 0;
		for (Double dur : durations) {
			sum += dur;
			count++;
		}
		if (count > 0) {
			sum = sum / count;
		}
		return sum;
	}
	
	public static Double calculateStd(List<Double> durations, Double avg) {
		Double sum = 0.0;
		Integer count = 0;
		for (Double dur : durations) {
			sum += (dur - avg) * (dur - avg);
			count++;
		}
		if (count > 0) {
			sum = sum / count;
		}
		return Math.sqrt(sum);
	}
}
