package org.processmining.ocel.discoveryvisualization;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ocel.annotations.ActivityOtIndipendent;
import org.processmining.ocel.annotations.EdgesMeasures;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.discovery.Endpoint;
import org.processmining.ocel.discovery.ModelEdge;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObjectType;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;


public class VisualizationPanel extends JPanel {
	PluginContext context;
	AnnotatedModel model;
	public ControlTab controlTab;
	public VisualizationTab visualizationTab;
	
	public VisualizationPanel(PluginContext context, AnnotatedModel model) {
		this.model = model;
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		
		controlTab = new ControlTab(context, model, this);
		this.add(controlTab, new Float(15));
		
		visualizationTab = new VisualizationTab(context, model, controlTab);
		this.add(visualizationTab, new Float(85));
	}
}

class ControlTab extends JPanel {
	PluginContext context;
	OcelEventLog ocel;
	AnnotatedModel model;
	VisualizationPanel panel;
	SliderChange sliderChange;
	
	public int IDX = 0;
	public double PERC_ACT = 0.2;
	public double PERC_EDGES = 0.2;
	public NiceDoubleSlider actSlider;
	public NiceDoubleSlider edgesSlider;
	
	public Double getPercAct() {
		return this.actSlider.getValue();
	}
	
	public Double getPercEdges() {
		return this.edgesSlider.getValue();
	}
	
	public ControlTab(PluginContext context, AnnotatedModel model, VisualizationPanel panel) {
		this.context = context;
		this.ocel = ocel;
		this.model = model;
		this.panel = panel;
		this.sliderChange = new SliderChange(context, model, panel);
		
		this.actSlider = SlickerFactory.instance().createNiceDoubleSlider("% Activities", 0.0, 1.0, 0.2, Orientation.HORIZONTAL);
		this.edgesSlider = SlickerFactory.instance().createNiceDoubleSlider("% Paths", 0.0, 1.0, 0.2, Orientation.HORIZONTAL);
		
		this.actSlider.addChangeListener(this.sliderChange);
		this.edgesSlider.addChangeListener(this.sliderChange);
		
		this.add(this.actSlider);
		this.add(this.edgesSlider);
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

	public VisualizationTab(PluginContext context, AnnotatedModel model, ControlTab controlTab) {
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
	
	public String getColorFromString(String orig) {
		return String.format("#%X", orig.hashCode());
	}
	
	public String getOppositeColorFromString(String orig) {
		return String.format("#%X", -orig.hashCode());
	}
	
	public void drawGraph() {
		Object parent = graph.getDefaultParent();
		
		int MIN_ALLOWED_ACT_COUNT = (int)(this.model.MAX_INDIPENDENT_ACT_COUNT * (1.0 - this.controlTab.getPercAct()));
		int MIN_ALLOWED_EDGE_COUNT = (int)(this.model.MAX_EDGE_COUNT * (1.0 - this.controlTab.getPercEdges()));

		for (String act : model.indipendentNodeMeasures.keySet()) {
			ActivityOtIndipendent activity = model.indipendentNodeMeasures.get(act);
			if (activity.satisfy(this.controlTab.IDX, MIN_ALLOWED_ACT_COUNT)) {
				Object activityObject = graph.insertVertex(parent, activity.activity, activity.toReducedString(this.controlTab.IDX), 150, 150, 275, 60, "fontSize=18");
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
					String this_color = getColorFromString(edge.objectType.name);
					
					Object arc = graph.insertEdge(parent, null, edgeMeasure.toReducedString(this.controlTab.IDX), obj1, obj2, "fontSize=16;strokeColor="+this_color+";fontColor="+this_color);
				}
			}
		}
		
		for (OcelObjectType ot : model.startActivities.keySet()) {
			boolean is_ok = false;
			for (String act : model.startActivities.get(ot).endpoints.keySet()) {
				if (activityIndipendent.containsKey(act)) {
					is_ok = true;
				}
			}
			if (is_ok) {
				String this_color = getColorFromString(ot.name);
				Object saNode = graph.insertVertex(parent, "", ot.name, 150, 150, 275, 60, "shape=ellipse;fillColor="+this_color+";fontColor=white");
				for (String act : model.startActivities.get(ot).endpoints.keySet()) {
					if (activityIndipendent.containsKey(act)) {
						Endpoint activity = model.startActivities.get(ot).endpoints.get(act);
						Object arc = graph.insertEdge(parent, null, activity.toReducedString(this.controlTab.IDX), saNode, activityIndipendent.get(act), "fontSize=16;strokeColor="+this_color+";fontColor="+this_color);
					}
				}
			}
		}
		
		for (OcelObjectType ot : model.endActivities.keySet()) {
			boolean is_ok = false;
			for (String act : model.endActivities.get(ot).endpoints.keySet()) {
				if (activityIndipendent.containsKey(act)) {
					Endpoint activity = model.startActivities.get(ot).endpoints.get(act);
					if (activity.satisfy(this.controlTab.IDX, MIN_ALLOWED_EDGE_COUNT)) {
						is_ok = true;
					}
				}
			}
			if (is_ok) {
				String this_color = getColorFromString(ot.name);
				Object eaNode = graph.insertVertex(parent, "", "", 150, 150, 60, 60, "shape=ellipse;fillColor="+this_color);
				for (String act : model.endActivities.get(ot).endpoints.keySet()) {
					if (activityIndipendent.containsKey(act)) {
						Endpoint activity = model.startActivities.get(ot).endpoints.get(act);
						if (activity.satisfy(this.controlTab.IDX, MIN_ALLOWED_EDGE_COUNT)) {
							Object arc = graph.insertEdge(parent, null, activity.toReducedString(this.controlTab.IDX), activityIndipendent.get(act), eaNode, "fontSize=16;strokeColor="+this_color+";fontColor="+this_color);
						}
					}
				}
			}
		}
	}
	
	public void doLayouting() {
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);

		layout.execute(graph.getDefaultParent());

		this.graph.getModel().endUpdate();
	}
	
	public void addGraphToView() {
		this.graphComponent = new mxGraphComponent(this.graph);
		
		this.scrollPane = new JScrollPane(this.graphComponent);
		this.scrollPane.setPreferredSize(new Dimension(1824, 826));
		
		//this.scrollPane.getViewport().setMinimumSize(new Dimension(160, 200));
		//this.scrollPane.getViewport().setPreferredSize(new Dimension(160, 200));
		
		this.scrollPane.updateUI();
		this.add(this.scrollPane);
		this.updateUI();
	}
}

class SliderChange implements ChangeListener {
	PluginContext context;
	VisualizationPanel panel;
	AnnotatedModel model;
	
	public SliderChange(PluginContext context, AnnotatedModel model, VisualizationPanel panel) {
		this.context = context;
		this.model = model;
		this.panel = panel;
	}
	
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		this.panel.visualizationTab.doRepresentationWork();
		this.panel.visualizationTab.addGraphToView();
	}
}