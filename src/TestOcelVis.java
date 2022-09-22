import javax.swing.JFrame;

import org.processmining.ocel.Importer;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.discoveryvisualization.VisualizationPanel;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestOcelVis {
	public static void main(String[] args) {
		OcelEventLog log = Importer.importFromFile("C:\\obj-centr-log.xmlocel");
		AnnotatedModel model = new AnnotatedModel(log);
		VisualizationPanel panel = new VisualizationPanel(null, model);
		final JFrame frame = new JFrame("CONSOLE");
		frame.add(panel);
        frame.setSize(1920, 1080);
        frame.pack();
        frame.setVisible(true);
	}
}
