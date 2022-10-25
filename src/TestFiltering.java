import java.io.IOException;

import org.processmining.ocel.Importer;
import org.processmining.ocel.filtering.FilterOnObjectTypes;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestFiltering {
	public static void main(String[] args) throws IOException {
		OcelEventLog log = Importer.importFromFile("C:\\example_log.jsonocel");
		System.out.println(FilterOnObjectTypes.objectTypesCount(log));
		log = FilterOnObjectTypes.applyFrequency(log, 4);
		System.out.println(FilterOnObjectTypes.objectTypesCount(log));
	}
}
