package org.processmining.ocel.discovery;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.ocelobjects.OcelEventLog;

@Plugin(name = "OC-PM Discovery",
returnLabels = { "OC-PM Model" },
returnTypes = { AnnotatedModel.class },
parameterLabels = { "OCEL Event Log" },
help = "OC-PM Discovery",
userAccessible = true)
public class DiscoveryPlugin {
	@PluginVariant(requiredParameterLabels = { 0 })
	@UITopiaVariant(affiliation = "PADS RWTH", author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de")
	public AnnotatedModel getModel(PluginContext context, OcelEventLog log) {
		return new AnnotatedModel(log);
	}
}
