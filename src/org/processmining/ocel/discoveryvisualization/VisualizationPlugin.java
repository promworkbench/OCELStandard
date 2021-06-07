package org.processmining.ocel.discoveryvisualization;

import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.ocel.discovery.AnnotatedModel;

@Plugin(name = "OC-DFG Visualizer", parameterLabels = { "OC-DFG Model" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class VisualizationPlugin {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JPanel visualize(PluginContext context, AnnotatedModel model) {
		return new VisualizationPanel(context, model);
	}
}
