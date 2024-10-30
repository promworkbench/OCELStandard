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
	public static void importExport(String inputPath, String outputPath) {
		File file = new File(inputPath);
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
		ocel.printSummaryStatistics();
		
		OCEL2JSONExporter exporter = new OCEL2JSONExporter(ocel);

		// Export to a file output stream
		try (OutputStream outputStream = new FileOutputStream(outputPath)) {
		    exporter.exportLogToStream(outputStream);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		System.out.println("ciao2");
		
	}
	
	public static void main(String[] args) throws Exception {
		importExport("C:\\ContainerLogistics.json", "C:\\Users\\berti\\output_ocel.json");
	
		importExport("C:\\Users\\berti\\output_ocel.json", "C:\\Users\\berti\\output_ocel2.json");
	}
}
