package org.processmining.ocel.discoveryvisualization;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ocel.annotations.ActivityOtIndipendent;
import org.processmining.ocel.annotations.EdgesMeasures;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.discovery.ModelEdge;
import org.processmining.ocel.ocelobjects.OcelEventLog;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;


public class VisualizationPanel extends JPanel {
	PluginContext context;
	OcelEventLog ocel;
	AnnotatedModel model;
	public ControlTab controlTab;
	public VisualizationTab visualizationTab;
	
	public VisualizationPanel(PluginContext context, OcelEventLog ocel) {
		this.ocel = ocel;
		this.model = new AnnotatedModel(ocel);
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		
		controlTab = new ControlTab(context, ocel, model, this);
		this.add(controlTab, new Float(15));
		
		
		visualizationTab = new VisualizationTab(context, ocel, model, controlTab);
		this.add(visualizationTab, new Float(85));
	}
}

class ControlTab extends JPanel {
	PluginContext context;
	OcelEventLog ocel;
	AnnotatedModel model;
	VisualizationPanel panel;
	
	public int IDX = 0;
	public double PERC_ACT = 0.2;
	public double PERC_EDGES = 0.2;
	
	public ControlTab(PluginContext context, OcelEventLog ocel, AnnotatedModel model, VisualizationPanel panel) {
		this.context = context;
		this.ocel = ocel;
		this.model = model;
		this.panel = panel;
		
		this.add(new JLabel("ciao1"));
	}
}


class VisualizationTab extends JPanel {
	PluginContext context;
	OcelEventLog ocel;
	AnnotatedModel model;
	ControlTab controlTab;
	
	mxGraph graph;
	mxGraphComponent graphComponent;
	JScrollPane scrollPane;
	
	Map<String, Object> activityIndipendent;
	Map<String, Map<String, Object>> activityOtIndipendent;

	public VisualizationTab(PluginContext context, OcelEventLog ocel, AnnotatedModel model, ControlTab controlTab) {
		this.context = context;
		this.ocel = ocel;
		this.model = model;
		this.controlTab = controlTab;
		
		this.doRepresentationWork();
		this.addGraphToView();
	}
	
	public void doRepresentationWork() {
		beginGraph();
		drawGraph();
		doLayouting();
	}
	
	public void beginGraph() {
		if (this.graphComponent != null) {
			this.scrollPane.remove(this.graphComponent);
			//this.scrollPane.updateUI();
			this.remove(this.scrollPane);
			//this.updateUI();
		}
		this.activityIndipendent = new HashMap<String, Object>();
		this.activityOtIndipendent = new HashMap<String, Map<String, Object>>();
		
		graph = new mxGraph();
		graph.getModel().beginUpdate();
	}
	
	public void drawGraph() {
		Object parent = graph.getDefaultParent();
		
		int MIN_ALLOWED_ACT_COUNT = (int)(this.model.MAX_INDIPENDENT_ACT_COUNT * this.controlTab.PERC_ACT);
		int MIN_ALLOWED_EDGE_COUNT = (int)(this.model.MAX_EDGE_COUNT * this.controlTab.PERC_EDGES);

		for (String act : model.indipendentNodeMeasures.keySet()) {
			ActivityOtIndipendent activity = model.indipendentNodeMeasures.get(act);
			if (activity.satisfy(this.controlTab.IDX, MIN_ALLOWED_ACT_COUNT)) {
				Object activityObject = graph.insertVertex(parent, activity.toReducedString(this.controlTab.IDX), activity.toReducedString(this.controlTab.IDX), 150, 150, 275, 60, "fontSize=18");
				activityIndipendent.put(activity.activity, activityObject);
			}
		}
		
		for (ModelEdge edge : model.edgesMeasures.keySet()) {
			String act1 = edge.sourceActivity;
			String act2 = edge.targetActivity;
			
			EdgesMeasures edgeMeasure = model.edgesMeasures.get(edge);
			
			if (activityIndipendent.containsKey(act1) && activityIndipendent.containsKey(act2)) {
				if (edgeMeasure.satisfy(this.controlTab.IDX, MIN_ALLOWED_EDGE_COUNT)) {
					Object obj1 = activityIndipendent.get(act1);
					Object obj2 = activityIndipendent.get(act2);
					
					Object arc = graph.insertEdge(parent, null, edgeMeasure.toReducedString(this.controlTab.IDX), obj1, obj2, "fontSize=16");
				}
			}
		}
	}
	
	public void doLayouting() {
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);

		while (true) {
			try {
				layout.execute(graph.getDefaultParent());
				break;
			}
			catch (Exception ex) {
				System.out.println("exception");
			}
		}
		this.graph.getModel().endUpdate();
	}
	
	public void addGraphToView() {
		this.graphComponent = new mxGraphComponent(this.graph);
		
		this.scrollPane = new JScrollPane(this.graphComponent);
		this.scrollPane.setPreferredSize(new Dimension(800, 800));
		
		this.scrollPane.getViewport().setMinimumSize(new Dimension(160, 200));
		this.scrollPane.getViewport().setPreferredSize(new Dimension(160, 200));
		
		this.scrollPane.updateUI();
		this.add(this.scrollPane);
		this.updateUI();
	}
}