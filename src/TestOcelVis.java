import javax.swing.JFrame;

import org.processmining.ocel.Importer;
import org.processmining.ocel.discoveryvisualization.VisualizationPanel;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestOcelVis {
	public static void main(String[] args) {
		OcelEventLog log = Importer.importFromFile("C:\\log.xmlocel");
		VisualizationPanel panel = new VisualizationPanel(null, log);
		final JFrame frame = new JFrame("CONSOLE");
		frame.add(panel);
        frame.setSize(1000, 1000);
        frame.pack();
        frame.setVisible(true);
	}
}
