import org.processmining.ocel.Importer;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestOcel {
	public static void main(String[] args) {
		OcelEventLog log = Importer.importFromFile("C:\\log.xmlocel");
		AnnotatedModel model = new AnnotatedModel(log);
	}
}
