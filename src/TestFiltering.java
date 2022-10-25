import java.io.IOException;

import org.processmining.ocel.Importer;
import org.processmining.ocel.filtering.FilterActivitiesList;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestFiltering {
	public static void main(String[] args) throws IOException {
		OcelEventLog log = Importer.importFromFile("C:\\example_log.jsonocel");
		log = FilterActivitiesList.filterActivitiesList(log, "Create Order>>>YES\nConfirm Order>>>NO\nInvoice Sent>>>YES");
		System.out.println(FilterActivitiesList.getActiString(log));
	}
}
