package org.processmining.ocel.conversion;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.ocelobjects.OcelEventLog;

@Plugin(name = "Classic Event Log to OCEL (simple conversion)",
returnLabels = { "Object-Centric Event Log" },
returnTypes = { OcelEventLog.class },
parameterLabels = { "Classic Event Log" },
help = "Classic Event Log to OCEL (simple conversion)",
userAccessible = true)
public class ConverterPlugin {
	@PluginVariant(requiredParameterLabels = { 0 })
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public OcelEventLog convertBasic(PluginContext context, XLog log) {
		return Converter.convertBasic(log);
	}
}
