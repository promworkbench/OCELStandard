import org.processmining.ocel.Importer;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.filtering.DummyFilter;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestOcel {
	public static void main(String[] args) {
		OcelEventLog log = Importer.importFromFile("C:\\log.xmlocel");
		AnnotatedModel model = new AnnotatedModel(log);
		System.out.println(log.events.size());
		System.out.println(log.objects.size());
		System.out.println(log.objectTypes.size());
		OcelEventLog filtered = DummyFilter.apply(log);
		System.out.println("");
		System.out.println(log.events.size());
		System.out.println(log.objects.size());
		System.out.println(log.objectTypes.size());
		System.out.println("");
		System.out.println(log.events.size());
		System.out.println(log.objects.size());
		System.out.println(log.objectTypes.size());
	}
}
