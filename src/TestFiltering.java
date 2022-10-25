import java.io.IOException;

import org.processmining.ocel.Importer;
import org.processmining.ocel.filtering.FilterActOtList;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestFiltering {
	public static void main(String[] args) throws IOException {
		OcelEventLog log = Importer.importFromFile("C:\\example_log.jsonocel");
		//log = FilterActivitiesList.filterActivitiesList(log, "Create Order>>>YES\nConfirm Order>>>NO\nInvoice Sent>>>YES");
		String filterString = FilterActOtList.getActOtStatisticsStri(log);
		filterString = "element\n" + 
				">>>Remove Item>>>YES\n" + 
				">>>Item out of Stock>>>YES\n" + 
				">>>Create Delivery>>>YES\n" + 
				">>>Create Order>>>YES\n" + 
				">>>Add Item to Order>>>YES\n" + 
				">>>Item back in Stock>>>YES";
		log = FilterActOtList.filterActOtList(log, filterString);
		filterString = FilterActOtList.getActOtStatisticsStri(log);
		System.out.println(filterString);
	}
}
