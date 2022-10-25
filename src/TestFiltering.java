import java.io.IOException;

import org.processmining.ocel.Importer;
import org.processmining.ocel.filtering.FilterOnObjectTypes;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestFiltering {
	public static void main(String[] args) throws IOException {
		OcelEventLog log = Importer.importFromFile("C:\\example_log.jsonocel");
		System.out.println(FilterOnObjectTypes.objTypesTextForInput(log));
		log = FilterOnObjectTypes.filterOnProvidedTextInput(log, "order>>>YES");
		System.out.println("AAAAAAAAA");
		System.out.println(FilterOnObjectTypes.objTypesTextForInput(log));
	}
}
