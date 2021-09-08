import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.processmining.ocel.Importer;
import org.processmining.ocel.discovery.AnnotatedModel;
import org.processmining.ocel.html.HTMLContainer;
import org.processmining.ocel.html.LifecycleDuration;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestOcel {
	public static void main(String[] args) throws IOException {
		OcelEventLog log = Importer.importFromFile("C:\\log.xmlocel");
		AnnotatedModel model = new AnnotatedModel(log);
		/*System.out.println(log.events.size());
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
		System.out.println(log.objectTypes.size());*/
		//HTMLContainer ret = EventsList.generateTable(model);
		//HTMLContainer ret = ObjectsList.generateTable(model, "DOCTYPE_Order");
		HTMLContainer ret = LifecycleDuration.generateTable(model);
		BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/berti/prova.html"));
	    writer.write(ret.content);
	    writer.flush();
	    writer.close();
	}
}
