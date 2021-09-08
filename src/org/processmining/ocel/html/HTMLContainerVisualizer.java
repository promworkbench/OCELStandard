package org.processmining.ocel.html;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "HTML Container visualizer", parameterLabels = { "ProM SVN Packages overview" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class HTMLContainerVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public static JPanel overviewVisualizer(PluginContext context, HTMLContainer container) {
		JEditorPane editorPane = new JEditorPane("text/html", container.content);
		editorPane.setCaretPosition(0);
        
		JScrollPane scrollPane = new JScrollPane(editorPane);
		
		JPanel returnedPanel = new JPanel();
		returnedPanel.setLayout(new BorderLayout(0, 15));
		returnedPanel.add(scrollPane, BorderLayout.CENTER);
		
		return returnedPanel;
	}
}
