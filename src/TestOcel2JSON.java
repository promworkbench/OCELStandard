import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.ocel.exporters.OCEL2JSONExporter;
import org.processmining.ocel.importers.OCEL2JSONImporter;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestOcel2JSON {
	public static void main(String[] args) throws Exception {
		File file = new File("C:\\ocel20_example.jsonocel");
		InputStream is0 = null;
		try {
			is0 = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OCEL2JSONImporter importer = new OCEL2JSONImporter();
		OcelEventLog ocel = importer.doImportFromStream(is0);
		System.out.println("ciao");
		
		OCEL2JSONExporter exporter = new OCEL2JSONExporter(ocel);

		// Export to a file output stream
		try (OutputStream outputStream = new FileOutputStream("C:\\Users\\berti\\output_ocel.json")) {
		    exporter.exportLogToStream(outputStream);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		System.out.println("ciao2");
	}
}
