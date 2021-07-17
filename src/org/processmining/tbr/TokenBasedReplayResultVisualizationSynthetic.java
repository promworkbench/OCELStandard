package org.processmining.tbr;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Synthetic visualization of results", parameterLabels = { "TokenBasedReplayResultLog" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class TokenBasedReplayResultVisualizationSynthetic {
	@PluginVariant(requiredParameterLabels = { 0 })
	public static JPanel apply(PluginContext context, TokenBasedReplayResultLog tbrResults) {
		JPanel panel = new JPanel();
		JLabel produced = new JLabel("Total produced: "+tbrResults.totalProduced);
		JLabel consumed = new JLabel("Total consumed: "+tbrResults.totalConsumed);
		JLabel missing = new JLabel("Total missing: "+tbrResults.totalMissing);
		JLabel remaining = new JLabel("Total remaining: "+tbrResults.totalRemaining);
		JLabel fitTraces = new JLabel(String.format("Fit traces: %d out of %d", tbrResults.fitTraces, tbrResults.totalTraces));
		JLabel fitness = new JLabel(String.format("Fitness: %f", tbrResults.logFitness));
		panel.add(produced);
		panel.add(consumed);
		panel.add(missing);
		panel.add(remaining);
		panel.add(fitTraces);
		panel.add(fitness);
		return panel;
	}
}
