package org.processmining.ocel.discoveryvisualization;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.ocelobjects.OcelEventLog;

@Plugin(name = "Filtered log export", returnLabels = { "Filtered log" }, returnTypes = { OcelEventLog.class }, parameterLabels = {
		"OcelEventLog" }, help = "Filtered log export", userAccessible = false)
public class FilteredLogExporterSS {
	@UITopiaVariant(author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de", affiliation = "PADS")
	@PluginVariant(requiredParameterLabels = { 0 })
	public OcelEventLog returnFilteredLogSS(PluginContext context, OcelEventLog filteredLog) {
		return filteredLog;
	}
}
