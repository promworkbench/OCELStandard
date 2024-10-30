import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.ocel.exporters.OCEL2SQLiteExporter;
import org.processmining.ocel.exporters.OCEL2XMLExporter;
import org.processmining.ocel.importers.OCEL2SQLImporter;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestOcel2SQLite {
	public static void importExport(String inputPath, String outputPath) throws Exception {
		File file = new File(inputPath);
		InputStream is0 = null;
		try {
			is0 = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OCEL2SQLImporter ocelImporter = new OCEL2SQLImporter();
		OcelEventLog ocel = ocelImporter.doImportFromStream(is0);
		ocel.printSummaryStatistics();
		
		OCEL2SQLiteExporter ocelExporter = new OCEL2SQLiteExporter(ocel);
		OCEL2XMLExporter xmlExporter = new OCEL2XMLExporter(ocel);
		
		try (OutputStream outputStream = new FileOutputStream(outputPath)) {
			//xmlExporter.exportLogToStream(outputStream);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		System.out.println("ciao2!");
	}
	
	public static void main(String[] args) throws Exception {
		importExport("C:\\ContainerLogistics.sqlite", "C:\\Users\\berti\\ocel20_1.xml");
		
		//importExport("C:\\Users\\berti\\ocel20_1.sqlite", "C:\\Users\\berti\\ocel20_2.sqlite");
	}
}
