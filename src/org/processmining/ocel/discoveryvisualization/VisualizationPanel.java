package org.processmining.ocel.discoveryvisualization;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.MenuElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageDescriptor.OS;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.ocel.annotations.ActivityOtDependent;
import org.processmining.ocel.annotations.ActivityOtIndipendent;
import org.processmining.ocel.annotations.EdgesMeasures;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.discovery.Endpoint;
import org.processmining.ocel.discovery.ModelEdge;
import org.processmining.ocel.discovery.ModelWithTbrResults;
import org.processmining.ocel.ocelobjects.OcelEventLog;
import org.processmining.ocel.ocelobjects.OcelObject;
import org.processmining.ocel.ocelobjects.OcelObjectType;
import org.processmining.ocel.replay.ReplayView;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;


public class VisualizationPanel extends JPanel {
	PluginContext context;
	AnnotatedModel model;
	public ControlTab controlTab;
	public StatisticsTab statisticsTab;
	public ActivityFilteringTab activityFilteringTab;
	public EdgeFilteringTab edgeFilteringTab;
	public VisualizationTab visualizationTab;
	
	public VisualizationPanel(PluginContext context, AnnotatedModel model) {
		this.model = model;
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		
		controlTab = new ControlTab(context, model, this);
		this.add(controlTab, new Float(5));
		
		statisticsTab = new StatisticsTab(context, model, this);
		this.add(statisticsTab, new Float(5));
		
		activityFilteringTab = new ActivityFilteringTab(context, model, this);
		this.add(activityFilteringTab, new Float(5));
		
		edgeFilteringTab = new EdgeFilteringTab(context, model, this);
		this.add(edgeFilteringTab, new Float(5));
		
		visualizationTab = new VisualizationTab(context, model, this);
		this.add(visualizationTab, new Float(80));
	}
	
	public void changeModel(AnnotatedModel model) {
		this.model = model;
		this.controlTab.changeModel(model);
		this.statisticsTab.changeModel(model);
		this.activityFilteringTab.changeModel(model);
		this.edgeFilteringTab.changeModel(model);
		this.visualizationTab.changeModel(model);
		this.visualizationTab.doRepresentationWork();
		this.visualizationTab.addGraphToView();
	}
}

class ControlTab extends JPanel {
	PluginContext context;
	AnnotatedModel model;
	VisualizationPanel panel;
	SliderChange sliderChange;
	
	public double PERC_ACT = 0.2;
	public double PERC_EDGES = 0.2;
	public NiceDoubleSlider actSlider;
	public NiceDoubleSlider edgesSlider;
	JButton resetFilters;
	ResetFiltersMouseListener resetFiltersMouseListener;
	
	String[] modelTypes = { "OC-DFG", "OC-Petri Nets" };
	JComboBox modelTypeSelection;
	JLabel modelTypeSelectionLabel;
	
	String[] syntheticMetrics = { "Events", "Unique Objects", "Total Objects" };
	JComboBox syntheticMetricsSelection;
	JLabel syntheticMetricsSelectionLabel;
	MetricsActionListener metricsActionListener;
	
	JPopupMenu menu;
	JButton menuShow;
	MenuShowMouseListener menuShowListener;
	
	JButton filterObjectTypes;
	FilterObjectTypesButtonMouseListener filterObjectTypesButtonListener;
	
	JButton exportFilteredButton;
	ExportFilteredLogMouseListener filteredLogMouseListener;
	
	public Double getPercAct() {
		return this.actSlider.getValue();
	}
	
	public Double getPercEdges() {
		return this.edgesSlider.getValue();
	}
	
	public int getSelectedIndex() {
		try {
			return this.syntheticMetricsSelection.getSelectedIndex();
		}
		catch (Exception ex) {
			return 0;
		}
	}
	
	public String getSelectedModelType() {
		return this.modelTypes[this.modelTypeSelection.getSelectedIndex()];
	}
	
	public ControlTab(PluginContext context, AnnotatedModel model, VisualizationPanel panel) {
		this.context = context;
		this.model = model;
		this.panel = panel;
		this.sliderChange = new SliderChange(context, panel);
		
		/*	JComboBox modelTypeSelection;
	JLabel modelTypeSelectionLabel;*/
		
		this.modelTypeSelectionLabel = new JLabel("Model: ");
		this.add(this.modelTypeSelectionLabel);
		this.modelTypeSelection = new JComboBox(modelTypes);
		this.modelTypeSelection.setSelectedIndex(0);
		this.add(this.modelTypeSelection);
		
		this.syntheticMetricsSelectionLabel = new JLabel("Metric:");
		this.add(this.syntheticMetricsSelectionLabel);
		this.syntheticMetricsSelection = new JComboBox(syntheticMetrics);
		syntheticMetricsSelection.setSelectedIndex(0);
		this.add(syntheticMetricsSelection);
		this.metricsActionListener = new MetricsActionListener(this);
		this.syntheticMetricsSelection.addActionListener(this.metricsActionListener);
		this.modelTypeSelection.addActionListener(this.metricsActionListener);
		
		this.actSlider = SlickerFactory.instance().createNiceDoubleSlider("% Activities", 0.0, 1.0, 0.2, Orientation.HORIZONTAL);
		this.edgesSlider = SlickerFactory.instance().createNiceDoubleSlider("% Paths", 0.0, 1.0, 0.2, Orientation.HORIZONTAL);
		
		this.actSlider.addChangeListener(this.sliderChange);
		this.edgesSlider.addChangeListener(this.sliderChange);
		
		this.add(this.actSlider);
		this.add(this.edgesSlider);
		
		this.resetFilters = new JButton("Reset filters");
		this.add(this.resetFilters);
		
		this.resetFiltersMouseListener = new ResetFiltersMouseListener(this);
		this.resetFilters.addMouseListener(this.resetFiltersMouseListener);
		
		this.initializeMenu();
		
		this.menuShow = new JButton("Select Object Types");
		this.add(this.menuShow);
		this.menuShowListener = new MenuShowMouseListener(this);
		this.menuShow.addMouseListener(this.menuShowListener);
		
		this.filterObjectTypes = new JButton("Filter Object Types");
		this.add(this.filterObjectTypes);
		this.filterObjectTypesButtonListener = new FilterObjectTypesButtonMouseListener(this);
		this.filterObjectTypes.addMouseListener(this.filterObjectTypesButtonListener);
		
		this.exportFilteredButton = new JButton("Export Filtered Log");
		this.add(this.exportFilteredButton);
		this.filteredLogMouseListener = new ExportFilteredLogMouseListener(this);
		this.exportFilteredButton.addMouseListener(this.filteredLogMouseListener);
	}
	
	public void changeModel(AnnotatedModel model) {
		this.model = model;
		
		this.menu = new JPopupMenu();
		this.initializeMenu();
	}
	
	public void initializeMenu() {
		this.menu = new JPopupMenu();
		for (String ot : this.model.ocel.objectTypes.keySet()) {
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(ot);
			menuItem.setSelected(true);
			this.menu.add(menuItem);
		}
	}
	
	public void exportOcel(OcelEventLog ocel) throws Exception {
		UIPluginContext context2 = (UIPluginContext) this.context;
		
		String actualAction = " ";
		PackageDescriptor pack = new PackageDescriptor(actualAction, actualAction, OS.ALL, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, true, true, new ArrayList<String>(), new ArrayList<String>());
		
		PluginDescriptor descriptor = null;
		
		try {
			descriptor = new PluginDescriptorImpl2(FilteredLogExporterSS.class, context.getClass(), pack);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Object[] objects = new Object[1];
		objects[0] = ocel;
		
		context.invokePlugin(descriptor, 0, objects);
		
	    JOptionPane.showMessageDialog(new JFrame(), "Exported OCEL!", "Dialog",
	            JOptionPane.INFORMATION_MESSAGE);
	}
}

class ExportFilteredLogMouseListener implements MouseListener {
	ControlTab tab;
	
	public ExportFilteredLogMouseListener(ControlTab tab) {
		this.tab = tab;
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		try {
			this.tab.exportOcel(this.tab.model.ocel);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class FilterObjectTypesButtonMouseListener implements MouseListener {
	ControlTab tab;
	
	public FilterObjectTypesButtonMouseListener(ControlTab tab) {
		this.tab = tab;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		Set<String> selectedObjectTypes = new HashSet<String>();
		for (MenuElement menuElement : this.tab.menu.getSubElements()) {
			JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)menuElement;
			if (menuItem.isSelected()) {
				selectedObjectTypes.add(menuItem.getLabel());
			}
		}
		AnnotatedModel filtered = this.tab.panel.model.filterOnObjectTypes(selectedObjectTypes);
		this.tab.panel.changeModel(filtered);
		this.tab.panel.visualizationTab.doRepresentationWork();
		this.tab.panel.visualizationTab.addGraphToView();
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

class MenuShowMouseListener implements MouseListener {
	ControlTab tab;
	
	public MenuShowMouseListener(ControlTab tab) {
		this.tab = tab;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("show menu!!");
		tab.menu.show(tab.menuShow, 0, tab.menuShow.getHeight());
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

class MetricsActionListener implements ActionListener {
	ControlTab controlTab;
	
	public MetricsActionListener(ControlTab controlTab) {
		this.controlTab = controlTab;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		this.controlTab.panel.visualizationTab.doRepresentationWork();
		this.controlTab.panel.visualizationTab.addGraphToView();
	}
}

class StatisticsTab extends JPanel {
	PluginContext context;
	AnnotatedModel model;
	VisualizationPanel panel;
	JLabel noEventsPrefix;
	JLabel noEvents;
	JLabel noUniqueObjectsPrefix;
	JLabel noUniqueObjects;
	JLabel noObjectTypesPrefix;
	JLabel noObjectTypes;
	
	public StatisticsTab(PluginContext context, AnnotatedModel model, VisualizationPanel panel) {
		this.context = context;
		this.model = model;
		this.panel = panel;
		this.noEventsPrefix = new JLabel(" Events: ");
		this.noEvents = new JLabel("0");
		this.noUniqueObjectsPrefix = new JLabel(" Unique Objects: ");
		this.noUniqueObjects = new JLabel("0");
		this.noObjectTypesPrefix = new JLabel(" Object Types: ");
		this.noObjectTypes = new JLabel("0");
		
		this.add(this.noEventsPrefix);
		this.add(this.noEvents);
		this.add(this.noUniqueObjectsPrefix);
		this.add(this.noUniqueObjects);
		this.add(this.noObjectTypesPrefix);
		this.add(this.noObjectTypes);
		
		this.fillStatistics();
	}
	
	public void fillStatistics() {
		this.noEvents.setText(String.format("%d", this.model.ocel.events.size()));
		this.noUniqueObjects.setText(String.format("%d", this.model.ocel.objects.size()));
		this.noObjectTypes.setText(String.format("%d", this.model.ocel.objectTypes.size()));
	}
	
	public void changeModel(AnnotatedModel model) {
		this.model = model;
		this.fillStatistics();
	}
}

class ResetFiltersMouseListener implements MouseListener {
	ControlTab tab;
	
	public ResetFiltersMouseListener(ControlTab tab) {
		this.tab = tab;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		this.tab.panel.changeModel(this.tab.panel.model.original);
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

class ActivityFilteringTab extends JPanel {
	PluginContext context;
	AnnotatedModel model;
	VisualizationPanel panel;
	public String activity;
	public OcelObjectType objectType;
	
	JLabel label;
	JButton filterRelatedObjectsButton;
	ActivityFilteringRelatedObjectsMouseListener relatedObjectsListener;
	
	JButton startActivitiesFilter;
	JButton endActivitiesFilter;
	
	StartActivitiesFilterMouseListener startActivitiesFilterMouseListener;
	EndActivitiesFilterMouseListener endActivitiesFilterMouseListener;
	
	JButton filterNotRelatedObjectsFilter;
	ActivityFilteringNotRelatedObjectsMouseListener notRelatedObjectsListener;
	
	JButton showRelatedObjects;
	ShowRelatedObjectsMouseListener showRelatedObjectsMouseListener;
	
	JButton filterNumberRelatedObjects;
	FilterNumberRelatedObjectsMouseListener filterNumberRelatedObjectsMouseListener;
	
	public ActivityFilteringTab(PluginContext context, AnnotatedModel model, VisualizationPanel panel) {
		this.context = context;
		this.model = model;
		this.panel = panel;
		this.activity = null;
		this.objectType = null;
		
		this.label = new JLabel("Selected activity: ");
		this.add(label);
		
		this.filterRelatedObjectsButton = new JButton("Filter on Related Objects");
		this.add(this.filterRelatedObjectsButton);
		this.filterRelatedObjectsButton.setEnabled(false);
		
		this.relatedObjectsListener = new ActivityFilteringRelatedObjectsMouseListener(this);
		this.filterRelatedObjectsButton.addMouseListener(this.relatedObjectsListener);
		
		this.startActivitiesFilter = new JButton("Filter NOT Starting With");
		this.startActivitiesFilter.setEnabled(false);
		this.add(this.startActivitiesFilter);
		this.endActivitiesFilter = new JButton("Filter NOT Ending With");
		this.endActivitiesFilter.setEnabled(false);
		this.add(this.endActivitiesFilter);
		
		this.startActivitiesFilterMouseListener = new StartActivitiesFilterMouseListener(this);
		this.startActivitiesFilter.addMouseListener(this.startActivitiesFilterMouseListener);
		
		this.endActivitiesFilterMouseListener = new EndActivitiesFilterMouseListener(this);
		this.endActivitiesFilter.addMouseListener(this.endActivitiesFilterMouseListener);
		
		this.filterNotRelatedObjectsFilter = new JButton("Filter on NON Related Objects");
		this.filterNotRelatedObjectsFilter.setEnabled(false);
		this.add(this.filterNotRelatedObjectsFilter);
		this.notRelatedObjectsListener = new ActivityFilteringNotRelatedObjectsMouseListener(this);
		this.filterNotRelatedObjectsFilter.addMouseListener(this.notRelatedObjectsListener);
		
		this.showRelatedObjects = new JButton("Show Related Objects");
		this.showRelatedObjects.setEnabled(false);
		this.add(this.showRelatedObjects);
		this.showRelatedObjectsMouseListener = new ShowRelatedObjectsMouseListener(this);
		this.showRelatedObjects.addMouseListener(this.showRelatedObjectsMouseListener);
		
		this.filterNumberRelatedObjects = new JButton("Filter Number Rel.Obj.");
		this.filterNumberRelatedObjects.setEnabled(false);
		this.add(this.filterNumberRelatedObjects);
		this.filterNumberRelatedObjectsMouseListener = new FilterNumberRelatedObjectsMouseListener(this);
		this.filterNumberRelatedObjects.addMouseListener(this.filterNumberRelatedObjectsMouseListener);
	}
	
	public void setActivityAndObjectType(String activity, OcelObjectType objectType) {
		this.activity = activity;
		this.objectType = objectType;
		this.setLabel();
		
		if (this.activity != null) {
			this.filterRelatedObjectsButton.setEnabled(true);
			
			if (this.objectType != null) {
				this.startActivitiesFilter.setEnabled(true);
				this.endActivitiesFilter.setEnabled(true);
				this.filterNotRelatedObjectsFilter.setEnabled(true);
				this.showRelatedObjects.setEnabled(true);
				this.filterNumberRelatedObjects.setEnabled(true);
			}
			else {
				this.startActivitiesFilter.setEnabled(false);
				this.endActivitiesFilter.setEnabled(false);
				this.filterNotRelatedObjectsFilter.setEnabled(false);
				this.showRelatedObjects.setEnabled(false);
				this.filterNumberRelatedObjects.setEnabled(false);
			}
		}
	}
	
	public void setLabel() {
		if (this.objectType != null) {
			this.label.setText("Selected activity: "+this.activity+" ("+this.objectType.name+")");
		}
		else {
			this.label.setText("Selected activity: "+this.activity);
		}
	}
	
	public void changeModel(AnnotatedModel model) {
		this.model = model;
	}
}

class FilterNumberRelatedObjectsMouseListener implements MouseListener {
	ActivityFilteringTab aft;

	public FilterNumberRelatedObjectsMouseListener(ActivityFilteringTab aft) {
		this.aft = aft;
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if (this.aft.objectType != null) {
			Integer minObj = Integer.parseInt(JOptionPane.showInputDialog("Insert the minimum number of related objects: "));
			Integer maxObj = Integer.parseInt(JOptionPane.showInputDialog("Insert the maximum number of related objects: "));
			AnnotatedModel filtered = this.aft.model.filterOnNumberRelatedObjects(this.aft.objectType, minObj, maxObj);
			this.aft.panel.changeModel(filtered);
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class ShowRelatedObjectsMouseListener implements MouseListener {
	ActivityFilteringTab aft;

	public ShowRelatedObjectsMouseListener(ActivityFilteringTab aft) {
		this.aft = aft;
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if (this.aft.activity != null && this.aft.objectType != null) {
			String toDisplay = this.aft.panel.model.getStringRelatedObjectsTypeActivity(this.aft.activity, this.aft.objectType);
			JOptionPane.showMessageDialog(null, toDisplay);
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

class StartActivitiesFilterMouseListener implements MouseListener {
	ActivityFilteringTab aft;

	public StartActivitiesFilterMouseListener(ActivityFilteringTab aft) {
		this.aft = aft;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if (aft.activity != null) {
			if (aft.objectType != null) {
				Set<OcelObject> positive = this.aft.model.objectsHavingStartActivity(aft.activity, aft.objectType, false);
				Set<OcelObject> negative = this.aft.model.objectsHavingStartActivity(aft.activity, aft.objectType, true);
				AnnotatedModel filtered = this.aft.model.filterOnNotRelatedObjects(positive, negative);
				this.aft.panel.changeModel(filtered);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class EndActivitiesFilterMouseListener implements MouseListener {
	ActivityFilteringTab aft;

	public EndActivitiesFilterMouseListener(ActivityFilteringTab aft) {
		this.aft = aft;
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if (aft.activity != null) {
			if (aft.objectType != null) {
				Set<OcelObject> positive = this.aft.model.objectsHavingEndActivity(aft.activity, aft.objectType, false);
				Set<OcelObject> negative = this.aft.model.objectsHavingEndActivity(aft.activity, aft.objectType, true);
				AnnotatedModel filtered = this.aft.model.filterOnNotRelatedObjects(positive, negative);
				this.aft.panel.changeModel(filtered);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class ActivityFilteringRelatedObjectsMouseListener implements MouseListener {
	ActivityFilteringTab aft;
	
	public ActivityFilteringRelatedObjectsMouseListener(ActivityFilteringTab aft) {
		this.aft = aft;
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if (aft.activity != null) {
			if (aft.objectType != null) {
				Set<OcelObject> relatedObjects = this.aft.model.relatedObjectsActivityOt(aft.activity, aft.objectType, true);
				AnnotatedModel filtered = this.aft.model.filterOnRelatedObjects(relatedObjects);
				this.aft.panel.changeModel(filtered);
			}
			else {
				Set<OcelObject> relatedObjects = this.aft.model.relatedObjectsActivity(aft.activity);
				AnnotatedModel filtered = this.aft.model.filterOnRelatedObjects(relatedObjects);
				this.aft.panel.changeModel(filtered);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class ActivityFilteringNotRelatedObjectsMouseListener implements MouseListener {
	ActivityFilteringTab aft;
	
	public ActivityFilteringNotRelatedObjectsMouseListener(ActivityFilteringTab aft) {
		this.aft = aft;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if (aft.activity != null) {
			if (aft.objectType != null) {
				Set<OcelObject> positive = this.aft.model.relatedObjectsActivityOt(aft.activity, aft.objectType, false);
				Set<OcelObject> negative = this.aft.model.relatedObjectsActivityOt(aft.activity, aft.objectType, true);
				AnnotatedModel filtered = this.aft.model.filterOnNotRelatedObjects(positive, negative);
				this.aft.panel.changeModel(filtered);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class EdgeFilteringTab extends JPanel {
	PluginContext context;
	AnnotatedModel model;
	VisualizationPanel panel;
	ModelEdge edge;
	
	JLabel label;
	JButton filterRelatedObjectsButton;
	EdgesFilteringRelatedObjectsMouseListener relatedObjectsListener;
	
	JButton filterNotRelatedObjectsButton;
	EdgesFilteringNotRelatedObjectsMouseListener notRelatedObjectsListener;
	
	public EdgeFilteringTab(PluginContext context, AnnotatedModel model, VisualizationPanel panel) {
		this.context = context;
		this.model = model;
		this.panel = panel;
		this.edge = null;
		
		this.label = new JLabel("Selected edge: ");
		this.add(label);
		
		this.filterRelatedObjectsButton = new JButton("Filter on Related Objects");
		this.filterRelatedObjectsButton.setEnabled(false);
		this.add(this.filterRelatedObjectsButton);
		
		this.relatedObjectsListener = new EdgesFilteringRelatedObjectsMouseListener(this);
		this.filterRelatedObjectsButton.addMouseListener(this.relatedObjectsListener);
		
		this.filterNotRelatedObjectsButton = new JButton("Filter on NON Related Objects");
		this.filterNotRelatedObjectsButton.setEnabled(false);
		this.add(this.filterNotRelatedObjectsButton);
		this.notRelatedObjectsListener = new EdgesFilteringNotRelatedObjectsMouseListener(this);
		this.filterNotRelatedObjectsButton.addMouseListener(this.notRelatedObjectsListener);
	}
	
	public void setEdge(ModelEdge edge) {
		this.edge = edge;
		this.setLabel();
		this.filterRelatedObjectsButton.setEnabled(true);
		this.filterNotRelatedObjectsButton.setEnabled(true);
	}
	
	public void setLabel() {
		this.label.setText("Selected edge: "+edge.sourceActivity+"->"+edge.targetActivity+" ("+edge.objectType.name+")");
	}
	
	public void changeModel(AnnotatedModel model) {
		this.model = model;
	}
}

class EdgesFilteringRelatedObjectsMouseListener implements MouseListener {
	EdgeFilteringTab eft;
	
	public EdgesFilteringRelatedObjectsMouseListener(EdgeFilteringTab eft) {
		this.eft = eft;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		ModelEdge edge = this.eft.edge;
		Set<OcelObject> edgeObjects = this.eft.model.relatedObjectsEdge(edge);
		AnnotatedModel filtered = this.eft.model.filterOnRelatedObjects(edgeObjects);
		this.eft.panel.changeModel(filtered);
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class EdgesFilteringNotRelatedObjectsMouseListener implements MouseListener {
	EdgeFilteringTab eft;
	
	public EdgesFilteringNotRelatedObjectsMouseListener(EdgeFilteringTab eft) {
		this.eft = eft;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		ModelEdge edge = this.eft.edge;
		Set<OcelObject> positive = this.eft.model.notRelatedObjectsEdge(edge);
		Set<OcelObject> negative = this.eft.model.relatedObjectsEdge(edge);
		AnnotatedModel filtered = this.eft.model.filterOnNotRelatedObjects(positive, negative);
		this.eft.panel.changeModel(filtered);
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class VisualizationTab extends JPanel {
	PluginContext context;
	AnnotatedModel model;
	VisualizationPanel panel;
	GraphMouseListener graphMouseListener;
	
	public mxGraph graph;
	public ExtendedGraphComponent graphComponent;
	JScrollPane scrollPane;
	
	Map<String, Object> activityIndipendentString;
	Map<ActivityOtIndipendent, Object> activityIndipendent;
	Map<Object, ActivityOtIndipendent> invActivityIndipendent;
	Map<Object, ActivityOtDependent> invActivityOtDependent;
		
	Map<ModelEdge, Object> edges;
	Map<Object, ModelEdge> invEdges;
	
	public Set<String> expandedActivities;
	public Set<ModelEdge> expandedModelEdges;

	public VisualizationTab(PluginContext context, AnnotatedModel model, VisualizationPanel panel) {
		this.context = context;
		this.model = model;
		this.panel = panel;
		this.graphMouseListener = new GraphMouseListener(this);
		
		this.expandedActivities = new HashSet<String>();
		this.expandedModelEdges = new HashSet<ModelEdge>();
		
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
		this.activityIndipendentString = new HashMap<String, Object>();
		this.activityIndipendent = new HashMap<ActivityOtIndipendent, Object>();
		this.invActivityIndipendent = new HashMap<Object, ActivityOtIndipendent>();
		this.invActivityOtDependent = new HashMap<Object, ActivityOtDependent>();
		this.edges = new HashMap<ModelEdge, Object>();
		this.invEdges = new HashMap<Object, ModelEdge>();
		
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
		
		int MIN_ALLOWED_ACT_COUNT = (int)(this.model.MAX_INDIPENDENT_ACT_COUNT * (1.0 - this.panel.controlTab.getPercAct()));
		int MIN_ALLOWED_EDGE_COUNT = (int)(this.model.MAX_EDGE_COUNT * (1.0 - this.panel.controlTab.getPercEdges()));

		for (String act : model.indipendentNodeMeasures.keySet()) {
			ActivityOtIndipendent activity = model.indipendentNodeMeasures.get(act);
			if (activity.satisfy(this.panel.controlTab.getSelectedIndex(), MIN_ALLOWED_ACT_COUNT)) {
				int width;
				int height;
				String label;
				if (this.expandedActivities.contains(act)) {
					width = 275;
					height = 250;
					label = activity.toString();
				}
				else {
					width = 275;
					height = 60;
					label = activity.toReducedString(this.panel.controlTab.getSelectedIndex());
				}
				
				int cc = 125 + 125*(this.model.MAX_INDIPENDENT_ACT_COUNT - activity.getValue(this.panel.controlTab.getSelectedIndex()))/(this.model.MAX_INDIPENDENT_ACT_COUNT - this.model.MIN_INDIPENDENT_ACT_COUNT);
				String hex = String.format("#%02x%02x%02x", cc, cc, cc);
				
				Object activityObject = graph.insertVertex(parent, activity.activity, label, 150, 150, width, height, "fontSize=18;fillColor="+hex);
				activityIndipendent.put(activity, activityObject);
				invActivityIndipendent.put(activityObject, activity);
				activityIndipendentString.put(activity.activity, activityObject);
				
				if (this.expandedActivities.contains(act)) {
					for (String ot : this.model.dependentNodeMeasures.get(act).keySet()) {
						ActivityOtDependent detailObj = this.model.dependentNodeMeasures.get(act).get(ot);
						if (detailObj.satisfy(this.panel.controlTab.getSelectedIndex(), MIN_ALLOWED_ACT_COUNT)) {
							String this_color = getColorFromString(detailObj.objectType.name);
							Object intermediateNode = graph.insertVertex(parent, detailObj.toString(), detailObj.toString(), 150, 150, 275, 250, "fontSize=13;shape="+mxConstants.SHAPE_HEXAGON+";fillColor="+this_color+";fontColor=white");
							Object arc1 = graph.insertEdge(parent, null, "", intermediateNode, activityObject, "fontSize=16;strokeColor="+this_color+";fontColor="+this_color);
							this.invActivityOtDependent.put(intermediateNode, detailObj);
						}
					}
				}
			}
		}
		
		if (this.panel.controlTab.getSelectedModelType().equals("OC-Petri Nets")) {
			Map<PetrinetNode, Object> petriNetPlacesToObjects = new HashMap<PetrinetNode, Object>();
			Map<Object, PetrinetNode> invPetriNetPlacesToObjects = new HashMap<Object, PetrinetNode>();
			Map<PetrinetNode, Object> petriNetTransToObjects = new HashMap<PetrinetNode, Object>();
			Map<Object, PetrinetNode> invPetriNetTransToObjects = new HashMap<Object, PetrinetNode>();
			Set<String> tbrActivities = new HashSet<String>();
			Map<String, ActivityOtIndipendent> actNameToActObj = new HashMap<String, ActivityOtIndipendent>();
			for (ActivityOtIndipendent act : activityIndipendent.keySet()) {
				tbrActivities.add(act.activity);
				actNameToActObj.put(act.activity, act);
			}
			ModelWithTbrResults modelWithTbrResults = new ModelWithTbrResults(this.model, tbrActivities);
			for (OcelObjectType ot : modelWithTbrResults.replayViews.keySet()) {
				String this_color = getColorFromString(ot.name);
				ReplayView tv = modelWithTbrResults.replayViews.get(ot);
				for (Place place : tv.net.getPlaces()) {
					String placeLabel = "p="+tv.tbrResults.totalProducedPerPlace.get(place);
					placeLabel += ";m="+tv.tbrResults.totalMissingPerPlace.get(place);
					placeLabel += ";\nc="+tv.tbrResults.totalConsumedPerPlace.get(place);
					placeLabel += ";r="+tv.tbrResults.totalRemainingPerPlace.get(place);
					int placeSizeX = 90;
					int placeSizeY = 90;
					if (tv.im.contains(place)) {
						placeLabel = ot.name;
						placeSizeX = 160;
						placeSizeY = 40;
					}
					Object placeNode = this.graph.insertVertex(parent, "netPlace@@"+place.getId().toString(), placeLabel, 150, 150, placeSizeX, placeSizeY, "fontSize=11;shape=ellipse;fillColor="+this_color+";fontColor=white");
					petriNetPlacesToObjects.put(place, placeNode);
					invPetriNetPlacesToObjects.put(placeNode, place);
				}
				for (Transition tr : tv.net.getTransitions()) {
					Object transNode = null;
					if (tr.isInvisible()) {
						transNode = this.graph.insertVertex(parent, "netTrans@@"+tr.getId().toString(), "tau", 150, 150, 90, 60, "fillColor="+this_color+";fontColor=white;fontSize=11");
					}
					else {
						transNode = null;
						for (ActivityOtIndipendent aa : this.activityIndipendent.keySet()) {
							if (aa.activity.equals(tr.getLabel())) {
								transNode = this.activityIndipendent.get(aa);
							}
						}
					}
					petriNetTransToObjects.put(tr, transNode);
					invPetriNetTransToObjects.put(transNode, tr);
				}
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : tv.tbrResults.arcExecutions.keySet()) {
					Object source = null;
					Object target = null;
					boolean isDouble = false;
					if (petriNetPlacesToObjects.containsKey(arc.getSource())) {
						source = petriNetPlacesToObjects.get(arc.getSource());
						target = petriNetTransToObjects.get(arc.getTarget());
						Transition transition = (Transition)arc.getTarget();
						if (!(transition.isInvisible())) {
							String label = transition.getLabel();
							ActivityOtDependent nodeMeasures = this.model.dependentNodeMeasures.get(label).get(ot.name);
							int numEvents = nodeMeasures.getValue(0);
							int numObjects = nodeMeasures.getValue(1);
							if (numObjects > numEvents) {
								isDouble = true;
							}
						}
					}
					else {
						source = petriNetTransToObjects.get(arc.getSource());
						target = petriNetPlacesToObjects.get(arc.getTarget());
						Transition transition = (Transition)arc.getSource();
						if (!(transition.isInvisible())) {
							String label = transition.getLabel();
							ActivityOtDependent nodeMeasures = this.model.dependentNodeMeasures.get(label).get(ot.name);
							int numEvents = nodeMeasures.getValue(0);
							int numObjects = nodeMeasures.getValue(1);
							if (numObjects > numEvents) {
								isDouble = true;
							}						
						}
					}
					String edgeLabel = "TO="+tv.tbrResults.arcExecutions.get(arc);
					String strokeWidth = "1";
					if (isDouble) {
						strokeWidth = "3";
					}
					graph.insertEdge(parent, arc.getLocalID().toString(), edgeLabel, source, target, "fontSize=10;strokeColor="+this_color+";fontColor="+this_color+";strokeWidth="+strokeWidth);
				}
			}
			
		}
		else if (this.panel.controlTab.getSelectedModelType().equals("OC-DFG")) {
			for (ModelEdge edge : model.edgesMeasures.keySet()) {
				String act1 = edge.sourceActivity;
				String act2 = edge.targetActivity;
				
				EdgesMeasures edgeMeasure = model.edgesMeasures.get(edge);
				
				if (activityIndipendentString.containsKey(act1) && activityIndipendentString.containsKey(act2)) {
					if (edgeMeasure.satisfy(this.panel.controlTab.getSelectedIndex(), MIN_ALLOWED_EDGE_COUNT)) {
						Object obj1 = activityIndipendentString.get(act1);
						Object obj2 = activityIndipendentString.get(act2);
						String this_color = getColorFromString(edge.objectType.name);
						
						if (this.expandedModelEdges.contains(edge)) {
							/*Object intermediateNode = graph.insertVertex(parent, "", edgeMeasure.toString(), 150, 150, 275, 250, "fontSize=18;shape="+mxConstants.SHAPE_DOUBLE_ELLIPSE+";fillColor="+this_color+";fontColor=white");
							Object arc1 = graph.insertEdge(parent, null, "", obj1, intermediateNode, "fontSize=16;strokeColor="+this_color+";fontColor="+this_color);
							Object arc2 = graph.insertEdge(parent, null, "", intermediateNode, obj2, "fontSize=16;strokeColor="+this_color+";fontColor="+this_color);
							edges.put(edge, intermediateNode);
							invEdges.put(intermediateNode, edge);*/
							int value = edgeMeasure.getValue(this.panel.controlTab.getSelectedIndex());
							int penwidth = 1 + (int)Math.floor(Math.log1p(value)/2.0);
							Object arc = graph.insertEdge(parent, null, edgeMeasure.toIntermediateString(), obj1, obj2, "fontSize=16;strokeColor="+this_color+";fontColor="+this_color+";strokeWidth="+penwidth);
							edges.put(edge, arc);
							invEdges.put(arc, edge);
						}
						else {
							int value = edgeMeasure.getValue(this.panel.controlTab.getSelectedIndex());
							int penwidth = 1 + (int)Math.floor(Math.log1p(value)/2.0);
							Object arc = graph.insertEdge(parent, null, edgeMeasure.toReducedString(this.panel.controlTab.getSelectedIndex()), obj1, obj2, "fontSize=16;strokeColor="+this_color+";fontColor="+this_color+";strokeWidth="+penwidth);
							edges.put(edge, arc);
							invEdges.put(arc, edge);
						}
					}
				}
			}
			
			for (OcelObjectType ot : model.startActivities.keySet()) {
				boolean is_ok = false;
				for (String act : model.startActivities.get(ot).endpoints.keySet()) {
					if (activityIndipendentString.containsKey(act)) {
						Endpoint activity = model.startActivities.get(ot).endpoints.get(act);
						if (activity.satisfy(this.panel.controlTab.getSelectedIndex(), MIN_ALLOWED_EDGE_COUNT)) {
							is_ok = true;
							break;
						}
					}
				}
				if (is_ok) {
					String this_color = getColorFromString(ot.name);
					Object saNode = graph.insertVertex(parent, "", ot.name, 150, 150, 275, 60, "shape=ellipse;fillColor="+this_color+";fontColor=white");
					for (String act : model.startActivities.get(ot).endpoints.keySet()) {
						if (activityIndipendentString.containsKey(act)) {
							Endpoint activity = model.startActivities.get(ot).endpoints.get(act);
							if (activity.satisfy(this.panel.controlTab.getSelectedIndex(), MIN_ALLOWED_EDGE_COUNT)) {
								int value = activity.getValue();
								int penwidth = 1 + (int)Math.floor(Math.log1p(value)/2.0);
								Object arc = graph.insertEdge(parent, null, activity.toReducedString(this.panel.controlTab.getSelectedIndex()), saNode, activityIndipendentString.get(act), "fontSize=16;strokeColor="+this_color+";fontColor="+this_color+";strokeWidth="+penwidth);
							}
						}
					}
				}
			}
			
			for (OcelObjectType ot : model.endActivities.keySet()) {
				boolean is_ok = false;
				for (String act : model.endActivities.get(ot).endpoints.keySet()) {
					if (activityIndipendentString.containsKey(act)) {
						Endpoint activity = model.endActivities.get(ot).endpoints.get(act);
						if (activity.satisfy(this.panel.controlTab.getSelectedIndex(), MIN_ALLOWED_EDGE_COUNT)) {
							is_ok = true;
							break;
						}
					}
				}
				if (is_ok) {
					String this_color = getColorFromString(ot.name);
					Object eaNode = graph.insertVertex(parent, "", "", 150, 150, 60, 60, "shape=ellipse;fillColor="+this_color);
					for (String act : model.endActivities.get(ot).endpoints.keySet()) {
						if (activityIndipendentString.containsKey(act)) {
							Endpoint activity = model.endActivities.get(ot).endpoints.get(act);
							if (activity.satisfy(this.panel.controlTab.getSelectedIndex(), MIN_ALLOWED_EDGE_COUNT)) {
								int value = activity.getValue();
								int penwidth = 1 + (int)Math.floor(Math.log1p(value)/2.0);
								Object arc = graph.insertEdge(parent, null, activity.toReducedString(this.panel.controlTab.getSelectedIndex()), activityIndipendentString.get(act), eaNode, "fontSize=16;strokeColor="+this_color+";fontColor="+this_color+";strokeWidth="+penwidth);
							}
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
		this.graphComponent = new ExtendedGraphComponent(this.panel, this.graph);
		
		this.scrollPane = new JScrollPane(this.graphComponent);
		
		int x = 1880;
		int y = 700;
		
		this.scrollPane.setPreferredSize(new Dimension(x, y));
		
		this.scrollPane.getViewport().setMinimumSize(new Dimension(x, y));
		this.scrollPane.getViewport().setPreferredSize(new Dimension(x, y));
		
		this.scrollPane.updateUI();
		this.add(this.scrollPane);
		this.updateUI();
		
		this.graphComponent.getGraphControl().addMouseListener(graphMouseListener);
	}
	
	public void changeModel(AnnotatedModel model) {
		this.model = model;
	}
}

class ExtendedGraphComponent extends mxGraphComponent {
	VisualizationPanel panel;
	mxGraph graph;
	
	public ExtendedGraphComponent(VisualizationPanel panel, mxGraph graph) {
		super(graph);
		this.panel = panel;
		this.graph = graph;
	}
	
	@Override
	public String getToolTipText(MouseEvent e) {
		return "";
	}
}

class SliderChange implements ChangeListener {
	PluginContext context;
	VisualizationPanel panel;
	
	public SliderChange(PluginContext context, VisualizationPanel panel) {
		this.context = context;
		this.panel = panel;
	}
	
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		this.panel.visualizationTab.doRepresentationWork();
		this.panel.visualizationTab.addGraphToView();
	}
}

class GraphMouseListener implements MouseListener {
	VisualizationTab tab;
	
	public GraphMouseListener(VisualizationTab tab) {
		this.tab = tab;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		Object cell = this.tab.graphComponent.getCellAt(e.getX(), e.getY());
		
		if (this.tab.invEdges.containsKey(cell)) {
			ModelEdge edge = this.tab.invEdges.get(cell);
			System.out.println(edge.toString());
			if (!this.tab.expandedModelEdges.contains(edge)) {
				this.tab.expandedModelEdges.add(edge);
				this.tab.doRepresentationWork();
				this.tab.addGraphToView();
			}
			else {
				this.tab.expandedModelEdges.remove(edge);
				this.tab.doRepresentationWork();
				this.tab.addGraphToView();
			}
			this.tab.panel.edgeFilteringTab.setEdge(edge);
		}
		else if (this.tab.invActivityIndipendent.containsKey(cell)) {
			ActivityOtIndipendent act0 = this.tab.invActivityIndipendent.get(cell);
			System.out.println(act0.toString());
			String act = act0.activity;
			if (!this.tab.expandedActivities.contains(act)) {
				this.tab.expandedActivities.add(act);
				this.tab.doRepresentationWork();
				this.tab.addGraphToView();
			}
			else {
				this.tab.expandedActivities.remove(act);
				this.tab.doRepresentationWork();
				this.tab.addGraphToView();
			}
			this.tab.panel.activityFilteringTab.setActivityAndObjectType(act, null);
		}
		else if (this.tab.invActivityOtDependent.containsKey(cell)) {
			ActivityOtDependent actOt = this.tab.invActivityOtDependent.get(cell);
			System.out.println(actOt.toString());
			this.tab.panel.activityFilteringTab.setActivityAndObjectType(actOt.activity, actOt.objectType);
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
